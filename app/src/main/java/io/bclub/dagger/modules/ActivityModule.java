package io.bclub.dagger.modules;

import dagger.Module;
import dagger.Provides;
import io.bclub.activity.BaseActivity;
import io.bclub.dagger.ActivityScope;

@Module
public class ActivityModule {

    BaseActivity activity;

    public ActivityModule(BaseActivity activity) {
        this.activity = activity;
    }

    @Provides @ActivityScope
    public BaseActivity providesBaseActivity() {
        return activity;
    }
}
