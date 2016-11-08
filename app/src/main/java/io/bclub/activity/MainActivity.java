package io.bclub.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import javax.inject.Inject;

import butterknife.BindView;
import io.bclub.R;
import io.bclub.adapter.PromotionsAdapter;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.controller.ApiController;
import io.bclub.dialog.SplashScreenDialog;
import io.bclub.exception.GooglePlayServicesException;
import io.bclub.model.City;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.Constants;
import io.bclub.util.DisplayHelper;
import io.bclub.util.SpacingItemDecoration;
import io.bclub.widget.CityEditView;
import rx.Subscription;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String ADAPTER_EXTRA = "ADAPTER_EXTRA";
    private static final String PAGE_EXTRA = "PAGE_EXTRA";
    private static final String HAS_MORE_EXTRA = "HAS_MORE_EXTRA";

    private com.backendless.Subscription subscription;

    @Inject
    ApiController apiController;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.promotions_empty_view)
    View emptyView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.city_edit_view)
    CityEditView cityEditView;

    PromotionsAdapter adapter;

    City city;

    SplashScreenDialog splashScreenDialog;

    CityEditView.OnCitySelectedListener listener = city -> {
        cityEditView.cancelEdit();

        if (this.city != null && this.city.equals(city)) {
            return;
        }

        apiController.storeCity(city);

        this.city = city;

        fetchPromotions(true);
    };

    int page = 0;
    boolean hasMore = true;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(PAGE_EXTRA, page);
        outState.putBoolean(HAS_MORE_EXTRA, hasMore);
        outState.putParcelable(ADAPTER_EXTRA, adapter.onSaveInstanceState());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
        if( subscription != null )
            subscription.cancelSubscription();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(this, FilterActivity.class);

            intent.putExtra(PARENT_EXTRA, MainActivity.class.getName());

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setNavigationAsMenu(true);
        setContentView(R.layout.activity_main);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);

        activityComponent.inject(this);

        restoreState(savedInstanceState);
        setupAdapter(savedInstanceState);
        setupRecyclerView();
        setupSwipeRefresh();
        setupContentMargin();

        setupItemClickBus();

        fetchPromotionsIfNeeded();

        city = apiController.getStoredCity();

        cityEditView.setOnCitySelectedListener(listener);
    }

    void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(PAGE_EXTRA, page);
            hasMore = savedInstanceState.getBoolean(HAS_MORE_EXTRA, hasMore);
        }
    }

    void setupAdapter(Bundle savedInstanceState) {
        adapter = new PromotionsAdapter(this);

        if (savedInstanceState != null) {
            adapter.onRestoreState(savedInstanceState.getParcelable(ADAPTER_EXTRA));
        }
    }

    void setupRecyclerView() {
        int spacing = DisplayHelper.dpToPixels(this, 8);

        recyclerView.addItemDecoration(new SpacingItemDecoration(spacing, 0));
        recyclerView.setAdapter(adapter);
    }

    void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    void setupContentMargin() {
        cityEditView.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        cityEditView.getViewTreeObserver().removeOnPreDrawListener(this);

                        ((ViewGroup.MarginLayoutParams) swipeRefreshLayout.getLayoutParams()).topMargin = cityEditView.getLabelHeight();

                        return true;
                    }
                });
    }

    void setupItemClickBus() {
        Subscription subscription = GenericPublishSubject.PUBLISH_SUBJECT
                .filter(publishItem -> publishItem.type == GenericPublishSubject.PROMOTION_CLICKED_TYPE)
                .subscribe(publishItem -> {
                    Intent intent = new Intent(MainActivity.this, PromotionDetailActivity.class);
                    EstablishmentPromotion promotion = (EstablishmentPromotion) publishItem.object;

                    intent.putExtra(PromotionDetailActivity.PROMOTION_EXTRA, promotion);
                    intent.putExtra(PARENT_EXTRA, MainActivity.class.getName());

                    startActivity(intent);
                });

        addSubscription(subscription);
    }

    void fetchPromotionsIfNeeded() {
        if (adapter.isEmpty()) {
            splashScreenDialog = new SplashScreenDialog();
            splashScreenDialog.show(getFragmentManager(), "SPLASH_SCREEN");

            fetchPromotions(false);
        }
    }

    void fetchPromotions(final boolean refreshing) {
        if (!requestPermission(Constants.RC_REQUEST_LOCATION_FIND_PERSONS, Pair.create(R.string.location_rationale_message, Manifest.permission.ACCESS_FINE_LOCATION))) {
            return;
        }

        if (refreshing) {
            swipeRefreshLayout.setRefreshing(true);
        } else {
            adapter.setLoading(true);
        }

        Subscription subscription = apiController.getPromotions(page)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe((response -> {
                    adapter.setLoading(false);

                    swipeRefreshLayout.setEnabled(false);
                    swipeRefreshLayout.setRefreshing(false);

                    if (refreshing) {
                        adapter.clear();
                    }

                    adapter.addAll(response.promotions);
                    hasMore = response.hasMore;

                    if (hasMore) {
                        page++;
                    }

                    toggleEmptyView();

                    updateUiIfNeeded();
                }), (error -> {
                    adapter.setLoading(false);

                    swipeRefreshLayout.setRefreshing(false);
                    swipeRefreshLayout.setEnabled(true);

                    toggleEmptyView();
                    updateUiIfNeeded();

                    if (error instanceof GooglePlayServicesException) {
                        startGooglePlayServicesResolution((GooglePlayServicesException) error, Constants.RC_GPS_ERROR_RESOLUTION);
                    } else {
                        snack(error);
                    }
                }));

        addSubscription(subscription);
    }

    void updateUiIfNeeded() {
        if (splashScreenDialog != null && splashScreenDialog.isVisible()) {
            splashScreenDialog.dismiss();
        }

        if (city == null) {
            city = apiController.getStoredCity();

            if (city != null) {
                cityEditView.setCity(city);
            }
        }
    }

    void toggleEmptyView() {
        if (adapter.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (cityEditView.isEditing()) {
            cityEditView.cancelEdit();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != Constants.RC_REQUEST_LOCATION_FIND_PERSONS) {
            return;
        }

        fetchPromotions(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Constants.RC_GPS_ERROR_RESOLUTION == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                fetchPromotionsIfNeeded();
            } else {
                snack(R.string.location_not_available, Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onRefresh() {
        fetchPromotions(true);
    }

    @Override
    protected void onMenuClicked() {
        Intent intent = new Intent(this, MenuActivity.class);

        intent.putExtra(PARENT_EXTRA, MainActivity.class.getName());

        startActivity(intent);
    }

    @Override
    protected String getScreenName() {
        return "Promotions List Screen";
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if( subscription != null )
            subscription.resumeSubscription();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if( subscription != null )
            subscription.pauseSubscription();
    }
}
