package io.bclub.dagger;

import javax.inject.Singleton;

import dagger.Component;
import io.bclub.application.Application;
import io.bclub.dagger.modules.ActivityModule;
import io.bclub.dagger.modules.ApiModule;
import io.bclub.dagger.modules.AppModule;

@Singleton
@Component(modules = {AppModule.class, ApiModule.class})
public interface AppComponent {
    void inject(Application app);

    ActivityComponent plus(ActivityModule activityModule);
}
