package io.bclub.exception;

import android.support.annotation.StringRes;

public class LocationActionException extends Exception {

    @StringRes
    public final int messageResId;

    public LocationActionException(@StringRes int messageResId) {
        this.messageResId = messageResId;
   }
}
