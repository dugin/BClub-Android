package io.bclub.controller;

import io.bclub.application.Application;
import io.bclub.exception.NetworkConnectivityException;
import rx.Observable;
import rx.Single;
import rx.functions.Func0;

class BaseController {

    protected Application app;
    protected PreferencesManager preferencesManager;

    public BaseController(Application app, PreferencesManager preferencesManager) {
        this.app = app;
        this.preferencesManager = preferencesManager;
    }

    protected <T> Observable<T> checkConnectivity(final Observable<T> observable) {
        return Observable.defer(() -> {
            if (app == null || app.isNetworkConnected()) {
                return observable;
            }

            return Observable.error(NetworkConnectivityException.INSTANCE);
        });
    }

    protected <T> Single<T> checkConnectivity(final Single<T> observable) {
        return Single.defer((Func0<Single<T>>) () -> {
            if (app == null || app.isNetworkConnected()) {
                return observable;
            }

            return Single.error(NetworkConnectivityException.INSTANCE);
        });
    }
}
