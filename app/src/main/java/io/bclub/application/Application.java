package io.bclub.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.gms.analytics.ExceptionReporter;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.bclub.BuildConfig;
import io.bclub.R;
import io.bclub.activity.BaseActivity;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.bus.PublishItem;
import io.bclub.dagger.AppComponent;
import io.bclub.dagger.DaggerAppComponent;
import io.bclub.dagger.Injector;
import io.bclub.dagger.modules.AppModule;
import io.bclub.model.backendless.BackendlessAddress;
import io.bclub.model.backendless.BackendlessCity;
import io.bclub.model.backendless.BackendlessEstablishment;
import io.bclub.model.backendless.BackendlessEstablishmentCategory;
import io.bclub.model.backendless.BackendlessEstablishmentPromotion;
import io.bclub.model.backendless.BackendlessNeighborhood;
import io.bclub.model.backendless.BackendlessPromotion;
import io.bclub.model.backendless.BackendlessTelephone;
import io.bclub.model.backendless.BackendlessVoucher;
import io.bclub.receiver.NetworkBroadcastReceiver;
import io.bclub.util.Constants;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Application extends android.app.Application implements Thread.UncaughtExceptionHandler {

    public static final String TAG = BuildConfig.APPLICATION_ID;

    public static final String BACKENDLESS_APP_VERSION = BuildConfig.BACKENDLESS_APP_VERSION;
    public static final String BACKENDLESS_ANDROID_KEY = BuildConfig.BACKENDLESS_ANDROID_KEY;
    public static final String BACKENDLESS_APP_ID = BuildConfig.BACKENDLESS_APP_ID;
    public static final String GOOGLE_PROJECT_NUMBER = BuildConfig.GOOGLE_PROJECT_NUMBER;

    Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    @Inject
    NetworkBroadcastReceiver networkBroadcastReceiver;

    AppComponent appComponent;

    boolean networkConnected;
    boolean wifiConnected;

    RefWatcher refWatcher;

    WeakReference<BaseActivity> foregroundActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        RxJavaPlugins.getInstance()
                .registerErrorHandler(new RxJavaErrorHandler() {
                    @Override
                    public void handleError(Throwable e) {
                        AppLog.e(e);
                    }
                });

        setupCrashReporting();
        setupBackendless();
        setupLeakCanary();

        createComponent();

        registerReceiver(networkBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        networkConnected = networkBroadcastReceiver.isNetworkConnected();
        wifiConnected = networkBroadcastReceiver.isWifiConnected();

        applyLocaleToContext(this);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        applyLocaleToContext(this);
    }

    public static void applyLocaleToContext(Context context) {
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();

        conf.locale = Constants.PT_BR;

        res.updateConfiguration(conf, res.getDisplayMetrics());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        unregisterReceiver(networkBroadcastReceiver);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesAppComponentService(name)) {
            return appComponent;
        }

        return super.getSystemService(name);
    }

    private void createComponent() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        appComponent.inject(this);
    }

    public void onForegroundActivityResume(BaseActivity activity) {
        foregroundActivity = new WeakReference<>(activity);
    }

    public void onForegroundActivityDestroy(BaseActivity activity) {
        if (foregroundActivity != null) {
            if (foregroundActivity.get() == activity) {
                foregroundActivity.clear();
            }
        }
    }

    void setupLeakCanary() {
        refWatcher = LeakCanary.install(this);
    }

    private void setupBackendless() {
        Backendless.initApp(this, BACKENDLESS_APP_ID, BACKENDLESS_ANDROID_KEY, BACKENDLESS_APP_VERSION);

        Backendless.Messaging.registerDevice(Application.GOOGLE_PROJECT_NUMBER, new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Log.d("", "");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d("", "");
            }
        });

        Backendless.Persistence.mapTableToClass("Addresses", BackendlessAddress.class);
        Backendless.Persistence.mapTableToClass("Cities", BackendlessCity.class);
        Backendless.Persistence.mapTableToClass("Establishments", BackendlessEstablishment.class);
        Backendless.Persistence.mapTableToClass("EstablishmentCategories", BackendlessEstablishmentCategory.class);
        Backendless.Persistence.mapTableToClass("Neighborhoods", BackendlessNeighborhood.class);
        Backendless.Persistence.mapTableToClass("Promotions", BackendlessPromotion.class);
        Backendless.Persistence.mapTableToClass("EstablishmentPromotions", BackendlessEstablishmentPromotion.class);
        Backendless.Persistence.mapTableToClass("Telephones", BackendlessTelephone.class);
        Backendless.Persistence.mapTableToClass("Vouchers", BackendlessVoucher.class);
    }

    private void setupCrashReporting() {
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        if (defaultUncaughtExceptionHandler instanceof ExceptionReporter) {
            ExceptionReporter exceptionReporter = (ExceptionReporter) defaultUncaughtExceptionHandler;
            exceptionReporter.setExceptionParser((thread, throwable) -> "Thread: " + thread + "\nException:\n" + getStackTrace(throwable));
        }

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString(); // stack trace as a string
    }

    public void setNetworkConnected(boolean networkConnected, boolean wifiConnected) {
        if (this.networkConnected != networkConnected) {
            GenericPublishSubject.PUBLISH_SUBJECT.onNext(new PublishItem<>(GenericPublishSubject.CONNECTIVITY_CHANGE_TYPE, networkConnected));
        }

        this.networkConnected = networkConnected;
        this.wifiConnected = wifiConnected;
    }

    public boolean isNetworkConnected() {
        return networkConnected;
    }

    public boolean isWifiConnected() {
        return wifiConnected;
    }

    public void watch(Object obj) {
        refWatcher.watch(obj);
    }

    public BaseActivity getForegroundActivity() {
        return foregroundActivity == null ? null : foregroundActivity.get();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "Erro nao esperado...", ex);

        if (defaultUncaughtExceptionHandler != null) {
            defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
        }
    }
}
