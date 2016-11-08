package io.bclub.controller.rx;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.bclub.R;
import io.bclub.exception.GooglePlayServicesException;
import io.bclub.exception.LocationActionException;
import io.bclub.util.LocationAdapter;
import rx.Observable;
import rx.Subscriber;

@SuppressWarnings("MissingPermission")
public class GetLastKnownLocationOnSubscribe implements Observable.OnSubscribe<Location> {

    private static final int MAX_ATTEMPTS = 3;

    Context context;
    GoogleApiClient googleApiClient;

    LocationManager locationManager;

    Handler handler = new Handler(Looper.getMainLooper());

    final Object lock = new Object();

    Location updatedLocation;

    boolean throwErrorIfNotAvailable = false;

    WeakReference<Subscriber<? super Location>> subscriberWeakReference;

    final LocationListener googleLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

            updatedLocation = location;

            synchronized (lock) {
                lock.notifyAll();
            }
        }
    };

    final LocationAdapter legacyLocationListener = new LocationAdapter() {
        @Override
        public void onLocationChanged(Location location) {
            updatedLocation = location;

            synchronized (lock) {
                lock.notifyAll();
            }
        }
    };

    public GetLastKnownLocationOnSubscribe(Context context, boolean throwErrorIfNotAvailable) {
        this.context = context.getApplicationContext();
        this.throwErrorIfNotAvailable = throwErrorIfNotAvailable;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void call(Subscriber<? super Location> subscriber) {
        int googleApiAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (!hasLocationPermission()) {
            emitNull();
            return;
        }

        subscriberWeakReference = new WeakReference<>(subscriber);

        if (ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED == googleApiAvailability) {
            tryEmitError(new GooglePlayServicesException(googleApiAvailability, R.string.update_google_play_services));
            return;
        }

        if (googleApiAvailability != ConnectionResult.SUCCESS) {
            callWithLegacyApi(subscriber);
        } else {
            callWithGoogleApi(subscriber);
        }
    }

    boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    void callWithLegacyApi(Subscriber<? super Location> subscriber) {
//        if (tryEmitLastLegacyLocation(subscriber)) {
//            disconnectGoogleApiClient();
//            return;
//        }

        requestSingleLegacyUpdateLocation();
    }

    void callWithGoogleApi(Subscriber<? super Location> subscriber) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();

        ConnectionResult connectionResult = googleApiClient.blockingConnect(3, TimeUnit.SECONDS);

        if (subscriber.isUnsubscribed()) {
            disconnectGoogleApiClient();
            return;
        }

        if (!connectionResult.isSuccess()) {
            if (connectionResult.getErrorCode() == ConnectionResult.TIMEOUT) {
                callWithLegacyApi(subscriber);
                return;
            }

            if (throwErrorIfNotAvailable) {
                tryEmitError(new GooglePlayServicesException(connectionResult, R.string.problem_with_google_play_services));
            } else {
                emitNull();
            }

            disconnectGoogleApiClient();

            return;
        }

        if (tryEmitLastGoogleApiLocation(subscriber)) {
            disconnectGoogleApiClient();
            return;
        }

        if (!requestSingleGoogleApiLocationUpdate()) {
            disconnectGoogleApiClient();
        }
    }

    void emitNull() {
        if (subscriberWeakReference == null) {
            return;
        }

        Subscriber<? super Location> subscriber = subscriberWeakReference.get();

        if (subscriber != null) {
            subscriber.onNext(null);
            subscriber.onCompleted();
        }
    }

    boolean tryEmitLastGoogleApiLocation(Subscriber<? super Location> subscriber) {
        if (subscriber.isUnsubscribed()) {
            return true;
        }

        LocationAvailability availability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (availability != null && availability.isLocationAvailable() && location != null) {
            tryEmitLocation(location);

            return true;
        }

        return false;
    }

    boolean tryEmitLastLegacyLocation(Subscriber<? super Location> subscriber) {
        if (subscriber.isUnsubscribed()) {
            return true;
        }

        Location location = locationManager.getLastKnownLocation(getBestEffortProvider());

        if (location != null) {
            tryEmitLocation(location);

            return true;
        }

        return false;
    }

    String getBestEffortProvider() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }

        return LocationManager.PASSIVE_PROVIDER;
    }

    boolean requestSingleGoogleApiLocationUpdate() {
        LocationSettingsResult settings = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, getLocationSettingsRequest())
                .await(1, TimeUnit.SECONDS);

        Status status = settings.getStatus();

        if (!status.isSuccess() || !settings.getLocationSettingsStates().isLocationUsable()) {
            if (status.hasResolution() && throwErrorIfNotAvailable) {
                tryEmitError(new GooglePlayServicesException(status, R.string.location_not_available));
            } else {
                emitNull();
            }

            return false;
        }

        // Using Main Thread just to call FusedLocationApi.requestLocationUpdates...
        handler.post(() -> LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, buildSingleLocationRequest(), googleLocationListener));

        try {
            synchronized (lock) {
                lock.wait(TimeUnit.SECONDS.toMillis(5L));
            }
        } catch (InterruptedException ignored) { }

        if (updatedLocation == null) {
            if (throwErrorIfNotAvailable) {
                tryEmitError(new LocationActionException(R.string.location_not_available));
            } else {
                emitNull();
            }

            return false;
        }

        tryEmitLocation(updatedLocation);

        return true;
    }

    boolean requestSingleLegacyUpdateLocation() {
        locationManager.requestSingleUpdate(getBestEffortProvider(), legacyLocationListener, handler.getLooper());

        try {
            synchronized (lock) {
                lock.wait(TimeUnit.SECONDS.toMillis(5L));
            }
        } catch (InterruptedException ignored) { }

        if (updatedLocation == null) {
            if (throwErrorIfNotAvailable) {
                tryEmitError(new LocationActionException(R.string.location_not_available));
            } else {
                emitNull();
            }

            return false;
        }

        tryEmitLocation(updatedLocation);

        return true;
    }

    LocationSettingsRequest getLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(buildSingleLocationRequest());

        return builder.build();
    }

    void tryEmitLocation(Location location) {
        if (subscriberWeakReference != null && subscriberWeakReference.get() != null) {
            Subscriber<? super Location> subscriber = subscriberWeakReference.get();

            subscriber.onNext(location);
            subscriber.onCompleted();
        }
    }

    void tryEmitError(Throwable throwable) {
        if (subscriberWeakReference != null && subscriberWeakReference.get() != null) {
            Subscriber<? super Location> subscriber = subscriberWeakReference.get();

            subscriber.onError(throwable);
            subscriber.onCompleted();
        }
    }

    LocationRequest buildSingleLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setNumUpdates(1);
        locationRequest.setInterval(2000L);
        locationRequest.setFastestInterval(1000L);
        locationRequest.setMaxWaitTime(4000L);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    void disconnectGoogleApiClient() {
        if (googleApiClient != null && (googleApiClient.isConnected() || googleApiClient.isConnecting())) {
            googleApiClient.disconnect();
        }
    }

    public static Observable<Location> create(Context context, final boolean throwErrorIfNotAvailable) {
        return Observable.create(new GetLastKnownLocationOnSubscribe(context, throwErrorIfNotAvailable))
                .retry((attempt, throwable) -> throwable instanceof LocationActionException && attempt < MAX_ATTEMPTS);
    }
}
