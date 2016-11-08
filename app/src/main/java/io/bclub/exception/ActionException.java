package io.bclub.exception;

import android.support.annotation.StringRes;

public class ActionException extends Exception {

    @StringRes
    public final int messageResId;

    public ActionException(@StringRes int messageResId) {
        this.messageResId = messageResId;
    }
}
