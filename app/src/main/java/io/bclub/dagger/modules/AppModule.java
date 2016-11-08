package io.bclub.dagger.modules;

import android.content.Context;
import android.net.ConnectivityManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.bclub.application.Application;
import io.bclub.controller.PreferencesManager;
import io.bclub.receiver.NetworkBroadcastReceiver;

@Module
public class AppModule {

    private static final String SHARED_PREFERENCES_NAME = "SharedPreferences";

    private Application app;

    public AppModule(Application app) {
        this.app = app;
    }

    @Provides @Singleton
    public Application providesApp() {
        return app;
    }

    @Provides @Singleton
    public PreferencesManager providesPreferenceManager() {
        return new PreferencesManager(app.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE));
    }

    @Provides @Singleton
    public ConnectivityManager providesConnectivityManager() {
        return (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Provides @Singleton
    public NetworkBroadcastReceiver providesBroadcastReceiver(ConnectivityManager connectivityManager) {
        return new NetworkBroadcastReceiver(app, connectivityManager);
    }
}
