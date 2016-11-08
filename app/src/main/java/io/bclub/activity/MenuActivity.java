package io.bclub.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsService;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.bclub.BuildConfig;
import io.bclub.R;
import io.bclub.controller.ApiController;
import io.bclub.customtabs.CustomTabActivityHelper;
import io.bclub.util.Constants;
import io.bclub.util.IntentHelper;

public class MenuActivity extends BaseActivity {

    private static String[] EMAIL = {BuildConfig.CONTACT_EMAIL};

    @Inject
    ApiController apiController;

    @BindView(R.id.btn_register)
    Button btnRegister;

    CustomTabActivityHelper helper;

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (helper != null) {
            helper.unbindCustomTabsService(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        activityComponent.inject(this);

        showAppBarArrow();
        setupToolbarAlpha(getString(R.string.menu));

        preWarmUrls();
        fetchUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserInfo();
    }

    void fetchUserInfo() {
        if (apiController != null && btnRegister != null && apiController.isLoggedIn()) {
            btnRegister.setText(R.string.view_account);
        }
    }

    void preWarmUrls() {
        if (Build.VERSION.SDK_INT < 16) {
            return;
        }

        try {
            helper = new CustomTabActivityHelper();
            helper.bindCustomTabsService(this);

            helper.setConnectionCallback(new CustomTabActivityHelper.ConnectionCallback() {
                @Override
                public void onCustomTabsConnected() {
                    Uri uri = Uri.parse(BuildConfig.WEBSITE);
                    List<Bundle> others = new ArrayList<>(3);

                    others.add(fromUri(BuildConfig.WEBSITE.concat("/faq.html")));
                    others.add(fromUri(BuildConfig.WEBSITE.concat("/tos_pp.html")));
                    others.add(fromUri(BuildConfig.WEBSITE.concat("/tos_pp.html")));

                    helper.mayLaunchUrl(uri, null, others);
                }

                @Override
                public void onCustomTabsDisconnected() {
                }
            });
        } catch (Exception ignored) { }
    }

    Bundle fromUri(String url) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));

        return bundle;
    }

    @OnClick(R.id.btn_register)
    void onRegisterClicked() {
        Intent intent;

        if (apiController.isLoggedIn()) {
            intent = new Intent(this, UserDetailActivity.class);
        } else {
            intent = new Intent(this, LoginRegistrationActivity.class);
        }

        intent.putExtra(PARENT_EXTRA, MenuActivity.class.getName());

        startActivity(intent);
    }

    @OnClick(R.id.tv_how_it_work)
    void onHowItWorkClicked() {
        Intent intent = new Intent(this, TutorialActivity.class);

        intent.putExtra(PARENT_EXTRA, MenuActivity.class.getName());

        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.tv_faq)
    void onFAQClicked() {
        launchIntentIfPossible(IntentHelper.view(BuildConfig.WEBSITE.concat("/faq.html")));
    }

    @OnClick(R.id.tv_email)
    void onEmailClicked() {
        Intent intent = IntentHelper.sendEmail(this, EMAIL, getString(R.string.contact));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    @OnClick(R.id.tv_website)
    void onWebsiteClicked() {
        launchIntentIfPossible(IntentHelper.view(BuildConfig.WEBSITE));
    }

    @OnClick(R.id.tv_pp)
    void onPrivacyPolicyClicked() {
        launchIntentIfPossible(IntentHelper.view(BuildConfig.WEBSITE.concat("/tos_pp.html")));
    }

    @OnClick(R.id.tv_tos)
    void onTOSClicked() {
        launchIntentIfPossible(IntentHelper.view(BuildConfig.WEBSITE.concat("/tos_pp.html")));
    }
}
