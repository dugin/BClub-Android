package io.bclub.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.bclub.R;
import io.bclub.application.AppLog;
import io.bclub.application.Application;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.customtabs.CustomTabActivityHelper;
import io.bclub.dagger.ActivityComponent;
import io.bclub.dagger.Injector;
import io.bclub.dagger.modules.ActivityModule;
import io.bclub.exception.ActionException;
import io.bclub.exception.GooglePlayServicesException;
import io.bclub.exception.NetworkConnectivityException;
import io.bclub.exception.UserNotAuthenticatedException;
import io.bclub.util.AlphaForegroundColorSpan;
import io.bclub.util.ResourceHelper;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity {

    public static final String PARENT_EXTRA = "PARENT_EXTRA";

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private Unbinder baseUnbinder;

    @Inject
    Application app;

    @BindView(R.id.coordinator_layout)
    @Nullable
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.toolbar)
    @Nullable
    Toolbar toolbar;

    ActivityComponent activityComponent;

    AlertDialog loadingDialog;

    boolean stopped = false;

    boolean dialogVisible;

    boolean navigationAsMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application.applyLocaleToContext(this);

        activityComponent = Injector.obtainAppComponent(this).plus(new ActivityModule(this));
        activityComponent.inject(this);

        bindConnectivityPublishSubject();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Application.applyLocaleToContext(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        baseUnbinder = ButterKnife.bind(this);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            toolbar.setNavigationOnClickListener(v -> {
                if (navigationAsMenu) {
                    onMenuClicked();
                } else {
                    onSupportNavigateUp();
                }
            });
        }
    }

    public void showAppBarArrow() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getIntent().hasExtra(PARENT_EXTRA)) {
            supportFinishAfterTransition();
            return true;
        }

        Intent parentIntent = NavUtils.getParentActivityIntent(this);

        if (parentIntent == null) {
            supportFinishAfterTransition();
            return true;
        }

        if (NavUtils.shouldUpRecreateTask(this, parentIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(parentIntent)
                    .startActivities();

            supportFinishAfterTransition();
            return true;
        } else {
            startActivity(parentIntent);
            supportFinishAfterTransition();

            return true;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesActivityComponentService(name)) {
            return activityComponent;
        }

        return super.getSystemService(name);
    }

    void bindConnectivityPublishSubject() {
        Subscription subscription = GenericPublishSubject.PUBLISH_SUBJECT
                .filter(publishItem -> publishItem.type == GenericPublishSubject.CONNECTIVITY_CHANGE_TYPE)
                .subscribe(publishItem -> {
                    onConnectivityChanged((Boolean) (publishItem.object));
                });

        addSubscription(subscription);
    }

    void onConnectivityChanged(boolean connected) { }

    protected void setNavigationAsMenu(boolean navigationAsMenu) {
        this.navigationAsMenu = navigationAsMenu;
    }

    protected void addSubscription(@Nullable Subscription subscription) {
        if (subscription == null) {
            return;
        }

        getCompositeSubscription().add(subscription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }

        if (baseUnbinder != null) {
            baseUnbinder.unbind();
        }

        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(null);
        }

        app.onForegroundActivityDestroy(this);

        app.watch(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this)
                .reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        stopped = false;
        super.onResume();

        if (getScreenName() != null) {
            //app.getDefaultTracker().setScreenName(getScreenName());
            //app.getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
        }

        app.onForegroundActivityResume(this);
    }

    @Override
    protected void onStop() {
        stopped = true;

        GoogleAnalytics.getInstance(this)
                .reportActivityStop(this);

        super.onStop();
    }

    protected void snack(@StringRes int messageResId, @Snackbar.Duration int duration) {
        View view = coordinatorLayout == null ? findViewById(android.R.id.content) : coordinatorLayout;

        if (view != null) {
            Snackbar.make(view, messageResId, duration).show();
        }
    }

    protected void toast(@StringRes int messageResId, int duration) {
        Toast.makeText(this, messageResId, duration).show();
    }

    public void toast(Throwable error) {
        Toast.makeText(this, getErrorMessage(error), Toast.LENGTH_LONG).show();
    }

    void startGooglePlayServicesResolution(GooglePlayServicesException exception, int requestCode) {
        if (!exception.hasResolution()) {
            Toast.makeText(this, exception.messageResId, Toast.LENGTH_LONG).show();
            return;
        }

        if (exception.connectionResult != null) {
            try {
                exception.connectionResult.startResolutionForResult(this, requestCode);
                return;
            } catch (IntentSender.SendIntentException ignored) { }
        } else if(exception.status != null) {
            try {
                exception.status.startResolutionForResult(this, requestCode);
                return;
            } catch (IntentSender.SendIntentException ignored) { }
        } else if (exception.googleApiAvailability != null && exception.googleApiAvailability == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showGooglePlayServicesUpdateDialog();
            return;
        }

        Toast.makeText(this, exception.messageResId, Toast.LENGTH_LONG).show();
    }

    void showGooglePlayServicesUpdateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.common_google_play_services_update_title);
        builder.setMessage(getString(R.string.update_google_play_services));

        builder.setNegativeButton(getString(android.R.string.no), (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(getString(android.R.string.yes), (dialog, which) -> {
            dialog.dismiss();

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms")));
            }
        });

        builder.show();
    }

    public void snack(Throwable error) {
        snack(getErrorMessage(error), Snackbar.LENGTH_LONG);
    }

    @StringRes
    protected int getErrorMessage(Throwable error) {
        AppLog.e(error);

        if (isNetworkError(error)) {
            return R.string.no_connectivity;
        } else if (error instanceof UserNotAuthenticatedException) {
            return R.string.not_logged;
        } else if (error instanceof ActionException) {
            return ((ActionException) error).messageResId;
        } else {
            return R.string.unknown_error;
        }
    }

    boolean isNetworkError(Throwable t) {
        return t instanceof NetworkConnectivityException
                || t instanceof UnknownHostException
                || t instanceof SocketTimeoutException;
    }

    protected CompositeSubscription getCompositeSubscription() {
        if (compositeSubscription == null || compositeSubscription.isUnsubscribed()) {
            compositeSubscription = new CompositeSubscription();
        }

        return compositeSubscription;
    }

    public void showLoading() {
        hideLoading();
        dialogVisible = true;

        buildNewLoadingDialog().show();
    }

    public void hideLoading() {
        if (isDialogShowing()) {
            dialogVisible = false;

            try {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
            } catch (IllegalArgumentException ignored) { }
        }
    }

    boolean isDialogShowing() {
        return dialogVisible;
    }

    AlertDialog buildNewLoadingDialog() {
        loadingDialog = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create();

        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return loadingDialog;
    }

    public boolean isActive() {
        return !stopped;
    }

    @SafeVarargs
    final boolean requestPermission(int requestCode, Pair<Integer, String>... rationaleMessagePermissions) {
        ArrayList<Pair<Integer, String>> missingPermissions = new ArrayList<>();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < rationaleMessagePermissions.length; ++i) {
            if (ActivityCompat.checkSelfPermission(this, rationaleMessagePermissions[i].second) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(rationaleMessagePermissions[i]);
            }
        }

        if (missingPermissions.isEmpty()) {
            return true;
        }

        String dialogMessage = buildRationaleDialogMessage(missingPermissions);

        if (dialogMessage != null) {
            showRationalePermissionDialog(dialogMessage, requestCode, missingPermissions);
        } else {
            requestPermissions(requestCode, missingPermissions);
        }

        return false;
    }

    void showRationalePermissionDialog(String message, final int requestCode, final ArrayList<Pair<Integer, String>> permissions) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setTitle(R.string.we_need_some_permissions)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    requestPermissions(requestCode, permissions);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .show();
    }

    void requestPermissions(int requestCode, ArrayList<Pair<Integer, String>> desiredPermissions) {
        String[] permissions = new String[desiredPermissions.size()];

        for (int i = 0, size = desiredPermissions.size(); i < size; ++i) {
            permissions[i] = desiredPermissions.get(i).second;
        }

        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Nullable
    String buildRationaleDialogMessage(@NonNull ArrayList<Pair<Integer, String>> permissions) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0, size = permissions.size(); i < size; ++i) {
            Pair<Integer, String> pair = permissions.get(i);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, pair.second)) {
                sb.append(getString(pair.first));

                if ((i + 1) < size) {
                    sb.append("\n\n");
                }
            }
        }

        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return null;
        }
    }

    void setupToolbarAlpha(String title) {
        if (getSupportActionBar() != null) {
            SpannableString spannableString = new SpannableString(title);
            AlphaForegroundColorSpan alphaForegroundColorSpan = new AlphaForegroundColorSpan(ResourceHelper.resolveColorAttr(this, android.R.attr.textColorSecondary, Color.WHITE));

            alphaForegroundColorSpan.setAlpha(0.5f);

            spannableString.setSpan(alphaForegroundColorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            getSupportActionBar().setTitle(spannableString);
        }
    }

    void launchIntentIfPossible(Intent intent) {
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .enableUrlBarHiding()
                    .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
                    .setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .setToolbarColor(ContextCompat.getColor(this, R.color.primary))
                    .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.primary_dark))
                    .addDefaultShareMenuItem()
                    .build();

            CustomTabActivityHelper.openCustomTab(this, customTabsIntent, intent.getData(), (activity, uri) -> {
                openWebViewActivity(intent);
            });
        } catch (Exception ignored) {
            openWebViewActivity(intent);
        }
    }

    void openWebViewActivity(Intent intent) {
        Intent webActivityIntent = new Intent(this, WebViewActivity.class);

        webActivityIntent.putExtra(PARENT_EXTRA, MenuActivity.class.getName());

        webActivityIntent.putExtra(WebViewActivity.URI_EXTRA, intent.getData());

        startActivity(webActivityIntent);
    }

    void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.user_registered_title)
                .setMessage(R.string.user_registered_summary)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();

                    Intent intent = new Intent(this, LoginRegistrationActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    finish();
                }).show();
    }

    protected String getScreenName() {
        return null;
    }

    protected void onMenuClicked() { }
}
