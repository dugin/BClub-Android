package io.bclub.exception;

public class NetworkConnectivityException extends Exception {
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public static final NetworkConnectivityException INSTANCE = new NetworkConnectivityException();
}
