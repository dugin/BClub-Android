package io.bclub.exception;

import android.support.annotation.StringRes;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

public class GooglePlayServicesException extends Exception {

    public ConnectionResult connectionResult;

    public Status status;

    public Integer googleApiAvailability;

    @StringRes
    public final int messageResId;

    public GooglePlayServicesException(@StringRes int messageResId) {
        this.messageResId = messageResId;
    }

    public GooglePlayServicesException(int googleApiAvailability, @StringRes int messageResId) {
        this.messageResId = messageResId;
        this.googleApiAvailability = googleApiAvailability;
    }

    public GooglePlayServicesException(ConnectionResult connectionResult, @StringRes int messageResId) {
        this.connectionResult = connectionResult;
        this.messageResId = messageResId;
    }

    public GooglePlayServicesException(Status status, @StringRes int messageResId) {
        this.status = status;
        this.messageResId = messageResId;
    }

    public boolean hasResolution() {
        return (status != null && status.hasResolution())
                || (connectionResult != null && connectionResult.hasResolution())
                || (googleApiAvailability != null && googleApiAvailability == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED);
    }
}
