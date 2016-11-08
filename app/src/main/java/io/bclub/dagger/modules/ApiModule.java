package io.bclub.dagger.modules;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.bclub.application.Application;
import io.bclub.controller.ApiController;
import io.bclub.controller.PreferencesManager;
import io.bclub.controller.StripeController;

@Module
public class ApiModule {

    @Provides @Singleton
    public ApiController providesApiController(Application app, PreferencesManager preferencesManager) {
        return new ApiController(app, preferencesManager, new Gson());
    }

    @Provides @Singleton
    public StripeController providesStripeController(Application app, PreferencesManager preferencesManager) {
        return new StripeController(app, preferencesManager);
    }
}
