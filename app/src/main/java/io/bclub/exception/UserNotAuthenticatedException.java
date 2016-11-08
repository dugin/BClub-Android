package io.bclub.exception;

public class UserNotAuthenticatedException extends Exception {
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public static final UserNotAuthenticatedException INSTANCE = new UserNotAuthenticatedException();
}
