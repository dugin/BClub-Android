package io.bclub.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import io.bclub.R;
import io.bclub.adapter.PromotionsFilterAdapter;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.bus.PublishItem;
import io.bclub.controller.ApiController;
import io.bclub.controller.rx.GetPromotionsOnSubscribe;
import io.bclub.exception.GooglePlayServicesException;
import io.bclub.model.City;
import io.bclub.model.EstablishmentCategory;
import io.bclub.model.EstablishmentPromotion;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.Constants;
import io.bclub.widget.CheckboxGroup;
import io.bclub.widget.CityEditView;
import io.bclub.widget.DebounceTextWatcher;
import io.bclub.widget.WeekDaysView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class FilterActivity extends BaseActivity {

    private static final String QUERY_EXTRA = "QUERY_EXTRA";
    private static final String CATEGORIES_EXTRA = "CATEGORIES_EXTRA";
    private static final String CITY_EXTRA = "CITY_EXTRA";
    private static final String SELECTED_PERCENTS_EXTRA = "SELECTED_PERCENTS_EXTRA";
    private static final String WEEK_DAYS_EXTRA = "WEEK_DAYS_EXTRA";
    private static final String ADAPTER_EXTRA = "ADAPTER_EXTRA";
    private static final String PAGE_EXTRA = "PAGE_EXTRA";
    private static final String HAS_MORE_EXTRA = "HAS_MORE_EXTRA";

    @Inject
    ApiController apiController;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.city_edit_view)
    CityEditView cityEditView;

    int[] percents;
    ArrayList<EstablishmentCategory> categories;
    City selectedCity;
    boolean[] weekDays = {true, true, true, true, true, true, true};

    String query;

    List<Integer> selectedPercents;

    View emptyView;

    ViewGroup filtersContainer;

    CheckboxGroup categoriesContainer;

    CheckboxGroup discountContainer;

    EditText etSearch;

    WeekDaysView weekDaysView;

    PromotionsFilterAdapter adapter;

    Subscription lastSubscription;

    CityEditView.OnCitySelectedListener listener = city -> {
        cityEditView.cancelEdit();

        if (selectedCity != null && selectedCity.equals(city)) {
            return;
        }

        selectedCity = city;
        fetchPromotions();
    };

    DebounceTextWatcher.OnTextChangeListener textListener = text -> {
        String str = text.toString().trim();

        if (str.equals(query)) {
            return;
        }

        fetchPromotions();
    };

    WeekDaysView.OnDaySelectListener dayListener = (day, selected) -> GenericPublishSubject.PUBLISH_SUBJECT
            .onNext(PublishItem.of(GenericPublishSubject.FILTER_DAY_OF_WEEK_CLICKED_TYPE, weekDaysView.getWeekDays()));

    int page = 0;
    boolean hasMore = true;

    boolean firstTime;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<Integer> selectedPercentIndexes = new ArrayList<>(percents.length);

        if (discountContainer != null) {
            for (int i = 0, size = percents.length; i < size; ++i) {
                if (((CheckBox) discountContainer.getChildAt(i)).isChecked()) {
                    selectedPercentIndexes.add(i);
                }
            }
        }

        outState.putParcelableArrayList(CATEGORIES_EXTRA, categories);
        outState.putIntegerArrayList(SELECTED_PERCENTS_EXTRA, selectedPercentIndexes);
        outState.putParcelable(CITY_EXTRA, selectedCity);

        if (weekDaysView != null) {
            outState.putBooleanArray(WEEK_DAYS_EXTRA, weekDaysView.getWeekDays());
        }

        if (adapter != null) {
            outState.putParcelable(ADAPTER_EXTRA, adapter.onSaveInstanceState());
        }

        if (etSearch != null) {
            outState.putString(QUERY_EXTRA, etSearch.getText().toString());
        }

        outState.putBoolean(HAS_MORE_EXTRA, hasMore);
        outState.putInt(PAGE_EXTRA, page);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        listener = null;
        textListener = null;
        dayListener = null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstTime = savedInstanceState == null;

        setContentView(R.layout.activity_filter);
        setupToolbarAlpha(getString(R.string.filter));
        showAppBarArrow();

        activityComponent.inject(this);

        restoreState(savedInstanceState);
        setupAdapter(savedInstanceState);
        setupHeader();

        setupRecyclerView();
        setupContentMargin();

        setupPromotionItemClickBus();
        setupFilterBehavior();
        setupPercents();

        fetchCategoriesIfNeeded();
        fetchPromotions();

        cityEditView.setOnCitySelectedListener(listener);

        if (selectedCity != null) {
            cityEditView.setCity(selectedCity);
        }
    }

    void setupContentMargin() {
        cityEditView.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        cityEditView.getViewTreeObserver().removeOnPreDrawListener(this);

                        ((ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams()).topMargin = cityEditView.getLabelHeight();

                        recyclerView.scrollToPosition(0);

                        return true;
                    }
                });
    }

    void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(PAGE_EXTRA, page);
            hasMore = savedInstanceState.getBoolean(HAS_MORE_EXTRA, hasMore);
            categories = savedInstanceState.getParcelableArrayList(CATEGORIES_EXTRA);
            query = savedInstanceState.getString(QUERY_EXTRA);

            selectedPercents = savedInstanceState.getIntegerArrayList(SELECTED_PERCENTS_EXTRA);
            weekDays = savedInstanceState.getBooleanArray(WEEK_DAYS_EXTRA);
        } else {
            selectedCity = apiController.getStoredCity();
        }
    }

    void setupHeader() {
        View header = getLayoutInflater().inflate(R.layout.include_filter_header, recyclerView, false);

        categoriesContainer = (CheckboxGroup) header.findViewById(R.id.rg_categories);
        discountContainer = (CheckboxGroup) header.findViewById(R.id.rg_discounts);
        filtersContainer = (ViewGroup) header.findViewById(R.id.filters_container);
        weekDaysView = (WeekDaysView) header.findViewById(R.id.workdays_view);
        emptyView = header.findViewById(R.id.promotions_empty_view);
        etSearch = (EditText) header.findViewById(R.id.et_search);

        View child = categoriesContainer.getChildAt(0);

        if (child instanceof ProgressBar) {
            ((ProgressBar) child).getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }

        categoriesContainer.setSubjectEventType(GenericPublishSubject.FILTER_CATEGORY_CLICKED_TYPE);
        discountContainer.setSubjectEventType(GenericPublishSubject.FILTER_DISCOUNT_CLICKED_TYPE);

        weekDaysView.setWeekDays(weekDays);
        weekDaysView.setOnDaySelectListener(dayListener);

        if (query != null) {
            etSearch.setText(query);
        }

        etSearch.addTextChangedListener(new DebounceTextWatcher(textListener));

        adapter.addHeader(header);

        etSearch.post(() -> {
            etSearch.setFocusable(true);
            etSearch.setFocusableInTouchMode(true);
        });
    }

    void setupAdapter(Bundle savedInstanceState) {
        adapter = new PromotionsFilterAdapter(this);

        if (savedInstanceState != null) {
            adapter.onRestoreState(savedInstanceState.getParcelable(ADAPTER_EXTRA));
        }
    }

    void setupRecyclerView() {
        recyclerView.setAdapter(adapter);
    }

    void setupFilterBehavior() {
        Subscription subscription = GenericPublishSubject.PUBLISH_SUBJECT
                .filter(publishItem -> publishItem.type == GenericPublishSubject.FILTER_CATEGORY_CLICKED_TYPE
                        || publishItem.type == GenericPublishSubject.FILTER_DISCOUNT_CLICKED_TYPE
                        || publishItem.type == GenericPublishSubject.FILTER_DAY_OF_WEEK_CLICKED_TYPE)
                .onBackpressureDrop()
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(publishItem -> {
                    fetchPromotions();
                });

        addSubscription(subscription);
    }

    void setupPercents() {
        percents = getResources().getIntArray(R.array.filter_percents);
        View child = discountContainer.getChildAt(0);

        if (child instanceof ProgressBar) {
            discountContainer.removeView(child);
        }

        LayoutInflater layoutInflater = getLayoutInflater();

        for (int i = 0, size = percents.length; i < size; ++i) {
            CheckBox category = (CheckBox) layoutInflater.inflate(R.layout.list_item_filter_select, discountContainer, false);

            category.setId(i);
            category.setText(String.format(Constants.PT_BR, "%d%%", percents[i]));

            if ((selectedPercents != null && selectedPercents.contains(i)) || firstTime) {
                category.setChecked(true);
            }

            discountContainer.addView(category);
        }
    }

    void setupPromotionItemClickBus() {
        Subscription subscription = GenericPublishSubject.PUBLISH_SUBJECT
                .filter(publishItem -> publishItem.type == GenericPublishSubject.FILTER_PROMOTION_CLICKED_TYPE)
                .subscribe(publishItem -> {
                    Intent intent = new Intent(FilterActivity.this, PromotionDetailActivity.class);
                    EstablishmentPromotion promotion = (EstablishmentPromotion) publishItem.object;

                    intent.putExtra(PromotionDetailActivity.PROMOTION_EXTRA, promotion);
                    intent.putExtra(PARENT_EXTRA, MainActivity.class.getName());

                    startActivity(intent);
                });

        addSubscription(subscription);
    }

    void fetchCategoriesIfNeeded() {
        if (categories == null) {
            Subscription subscription = apiController.getEstablishmentCategories()
                    .compose(SingleSchedulerTransformer.ofIOToMainThread())
                    .subscribe(response -> {
                        this.categories = response;
                        bindCategories();
                    }, this::snack);

            addSubscription(subscription);
        } else {
            bindCategories();
        }
    }

    void bindCategories() {
        View child = categoriesContainer.getChildAt(0);

        if (child instanceof ProgressBar) {
            categoriesContainer.removeView(child);
        }

        LayoutInflater layoutInflater = getLayoutInflater();

        for (int i = 0, size = categories.size(); i < size; ++i) {
            EstablishmentCategory obj = categories.get(i);
            CheckBox category = (CheckBox) layoutInflater.inflate(R.layout.list_item_filter_select, categoriesContainer, false);

            if (firstTime) {
                obj.checked = true;
            }

            category.setId(i);
            category.setText(obj.name);

            category.setChecked(obj.checked);

            categoriesContainer.addView(category);
        }
    }

    void fetchPromotions() {
        if (!requestPermission(Constants.RC_REQUEST_LOCATION_FIND_PERSONS, Pair.create(R.string.location_rationale_message, Manifest.permission.ACCESS_FINE_LOCATION))) {
            return;
        }

        adapter.setLoading(true);
        adapter.clear();

        if (lastSubscription != null) {
            lastSubscription.unsubscribe();
        }

        List<EstablishmentCategory> selectedCategories = getSelectedCategories();
        List<Float> selectedPercents = getSelectedDiscounts();
        String selectedCityId = selectedCity == null ? null : selectedCity.objectId;

        weekDays = weekDaysView.getWeekDays();

        query = etSearch.getText().toString().trim();

        if (query.isEmpty()) {
            query = null;
        }

        recyclerView.smoothScrollToPosition(1);

        lastSubscription = apiController.getPromotions(page, new GetPromotionsOnSubscribe.Filters(selectedCityId, selectedCategories, selectedPercents, weekDays, query), false)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe((response -> {
                    adapter.setLoading(false);

                    adapter.addAll(response.promotions);
                    hasMore = response.hasMore;

                    if (hasMore) {
                        page++;
                    }

                    toggleEmptyView();
                }), (error -> {
                    adapter.setLoading(false);

                    toggleEmptyView();

                    if (error instanceof GooglePlayServicesException) {
                        startGooglePlayServicesResolution((GooglePlayServicesException) error, Constants.RC_GPS_ERROR_RESOLUTION);
                    } else {
                        snack(error);
                    }
                }));

        addSubscription(lastSubscription);
    }

    List<EstablishmentCategory> getSelectedCategories() {
        List<EstablishmentCategory> selectedCategories = new ArrayList<>(categoriesContainer.getChildCount());

        for (int i = 0, size = categoriesContainer.getChildCount(); i < size; ++i) {
            View child = categoriesContainer.getChildAt(i);

            if (!(child instanceof CheckBox)) {
                continue;
            }

            EstablishmentCategory category = categories.get(i);

            if (((CheckBox) child).isChecked()) {
                selectedCategories.add(category);
                category.checked = true;
            } else {
                category.checked = true;
            }
        }

        return selectedCategories;
    }

    List<Float> getSelectedDiscounts() {
        List<Float> selectedPercents = new ArrayList<>(discountContainer.getChildCount());

        for (int i = 0, size = discountContainer.getChildCount(); i < size; ++i) {
            if (((CheckBox) discountContainer.getChildAt(i)).isChecked()) {
                selectedPercents.add(((float) percents[i]) / 100f);
            }
        }

        return selectedPercents;
    }

    void toggleEmptyView() {
        // Ignoring Header
        int count = adapter.getCount();

        if (count == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
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

        fetchPromotions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Constants.RC_GPS_ERROR_RESOLUTION == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                fetchPromotions();
            } else {
                //noinspection Range
                snack(R.string.location_not_available, Snackbar.LENGTH_LONG);
            }
        }
    }

    @Override
    protected String getScreenName() {
        return "Filters Screen";
    }
}
