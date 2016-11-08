package io.bclub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.backendless.Subscription;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import javax.inject.Inject;

import butterknife.BindView;
import io.bclub.R;
import io.bclub.adapter.TutorialPagerAdapter;
import io.bclub.controller.PreferencesManager;

public class TutorialActivity extends BaseActivity implements View.OnClickListener {

    @Inject
    PreferencesManager preferencesManager;

    @BindView(R.id.vp_tutorial)
    ViewPager viewPager;

    private TutorialPagerAdapter adapter;

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent.inject(this);

        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        if (preferencesManager.isLoggedIn() && getCallingActivity() == null) {
            finishTutorial();
            return;
        }

        setContentView(R.layout.activity_tutorial);

        adapter = new TutorialPagerAdapter(this);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        adapter.setOnPageInstantiatedListener(new TutorialPagerAdapter.OnPageInstantiatedListener() {
            @Override
            public void onJoined() {
                goRegister();
            }

            @Override
            public void onEnter() {
                finishTutorial();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_enter) {
            finishTutorial();
        }
        if (v.getId() == R.id.btn_joined) {
            goRegister();
        }
    }

    void goRegister() {
        if (getCallingActivity() == null) {
            Intent intent = new Intent(this, PlanActivity.class);
            startActivity(intent);
        }

    }
    void finishTutorial() {
        if (getCallingActivity() == null) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if( subscription != null )
            subscription.cancelSubscription();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        AppEventsLogger.activateApp(this);
        if( subscription != null )
            subscription.resumeSubscription();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if( subscription != null )
            subscription.pauseSubscription();
        AppEventsLogger.deactivateApp(this);
    }
}
