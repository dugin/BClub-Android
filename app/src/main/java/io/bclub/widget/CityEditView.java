package io.bclub.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import io.bclub.BuildConfig;
import io.bclub.R;
import io.bclub.activity.BaseActivity;
import io.bclub.adapter.CitySearchAdapter;
import io.bclub.bus.GenericPublishSubject;
import io.bclub.controller.ApiController;
import io.bclub.controller.model.CitySearchResponse;
import io.bclub.dagger.Injector;
import io.bclub.model.City;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.DisplayHelper;
import io.bclub.util.EmailClickListener;
import rx.Subscription;

public class CityEditView extends FrameLayout {

    @Inject
    ApiController apiController;

    @Inject
    BaseActivity activity;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.et_city)
    EditText etCity;

    @BindView(R.id.tv_city)
    TextView tvCity;

    @BindView(R.id.tv_city_empty_view)
    View emptyView;

    @BindView(R.id.list_container)
    ViewGroup listContainer;

    @BindView(R.id.city_container)
    ViewGroup cityContainer;

    @BindView(R.id.btn_suggest)
    Button btnSuggest;

    String query;

    OnCitySelectedListener listener;

    EmailClickListener emailClickListener;

    DebounceTextWatcher.OnTextChangeListener textListener = text -> {
        String str = text.toString().trim();

        if (str.equals(query) || str.isEmpty()) {
            return;
        }

        previousResponse = null;
        doSearch(str);
    };

    CitySearchResponse previousResponse;

    CitySearchAdapter adapter;

    Subscription subscription;
    Subscription busSubscription;

    City city;

    boolean editing;

    boolean hasMore;

    boolean alignLeft = false;

    public CityEditView(Context context) {
        super(context);
    }

    public CityEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public CityEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CityEditView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    void init(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        if (isInEditMode()) {
            return;
        }

        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.CityEditView, defStyleAttr, defStyleRes);

            alignLeft = attributes.getBoolean(R.styleable.CityEditView_label_left_align, false);

            attributes.recycle();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parent = super.onSaveInstanceState();
        return new SavedState(parent, query, previousResponse, editing, hasMore);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;

        query = savedState.query;
        previousResponse = savedState.response;
        editing = savedState.editing;
        hasMore = savedState.hasMore;

        super.onRestoreInstanceState(savedState.parentState);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (subscription != null) {
            subscription.unsubscribe();
        }

        busSubscription.unsubscribe();

        subscription = null;
        textListener = null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!isInEditMode()) {
            setupBus();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        inflate(getContext(), R.layout.content_city_edit_view, this);

        if (!isInEditMode()) {
            Injector.obtainActivityComponent(getContext()).inject(this);

            city = apiController.getStoredCity();

            ButterKnife.bind(this);

            setupAdapter();
            setupRecyclerView();
            setupLabel();
            setupBus();

            etCity.addTextChangedListener(new DebounceTextWatcher(textListener));

            btnSuggest.setOnClickListener(emailClickListener = new EmailClickListener(activity, BuildConfig.CONTACT_EMAIL, null));

            if (alignLeft) {
                ((LayoutParams) tvCity.getLayoutParams()).gravity = Gravity.START;
            }
        }
    }

    void setupAdapter() {
        adapter = new CitySearchAdapter(getContext());
    }

    void setupRecyclerView() {
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new LoadMoreScrollListener(this, adapter));
    }

    void setupLabel() {
        if (city == null) {
            tvCity.setText(getContext().getString(R.string.all_cities));
        } else {
            tvCity.setText(city.name);
        }
    }

    void setupBus() {
        if (busSubscription != null && !busSubscription.isUnsubscribed()) {
            busSubscription.unsubscribe();
        }

        busSubscription = GenericPublishSubject.PUBLISH_SUBJECT
                .filter(item -> item.type == GenericPublishSubject.CITY_CLICKED_TYPE)
                .subscribe(item -> {
                    this.city = (City) item.object;

                    setupLabel();

                    DisplayHelper.hideSoftKeyboard(getContext(), this);

                    if (listener != null) {
                        listener.onCitySelected(city);
                    }
                });
    }

    @OnClick(R.id.city_container)
    void onCityClicked() {
        tvCity.setVisibility(View.GONE);
        etCity.setVisibility(View.VISIBLE);

        cityContainer.setClickable(false);

        listContainer.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        etCity.requestFocus();
        DisplayHelper.showSoftKeyboard(getContext(), etCity);

        editing = true;
    }

    @OnEditorAction(R.id.et_city)
    boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            previousResponse = null;
            doSearch(textView.getText().toString());

            return true;
        }

        return false;
    }

    void doSearch(String query) {
        this.query = query;

        if (subscription != null) {
            subscription.unsubscribe();
        }

        if (previousResponse == null) {
            adapter.clear();
        }

        adapter.setLoading(true);

        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        emailClickListener.setSubject(getContext().getString(R.string.suggest_establish_for, query));

        this.subscription = apiController.searchCity(query, previousResponse)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(response -> {
                    previousResponse = response;

                    hasMore = !response.list.isEmpty();

                    adapter.setLoading(false);

                    adapter.addAll(response.list);

                    toggleEmptyView();
                }, error -> {
                    adapter.setLoading(false);
                    activity.toast(error);
                });
    }

    private void toggleEmptyView() {
        if (adapter.getItemCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadMore() {
        doSearch(query);
    }

    public boolean isEditing() {
        return editing;
    }

    public void cancelEdit() {
        tvCity.setVisibility(View.VISIBLE);
        etCity.setVisibility(View.GONE);

        listContainer.setVisibility(View.GONE);

        etCity.setText(null);
        adapter.clear();

        cityContainer.setClickable(true);

        editing = false;
    }

    public void setOnCitySelectedListener(OnCitySelectedListener listener) {
        this.listener = listener;
    }

    public int getLabelHeight() {
        return tvCity.getHeight();
    }

    public void setCity(City city) {
        this.city = city;
        setupLabel();
    }

    public interface OnCitySelectedListener {
        void onCitySelected(City city);
    }

    public void setCityLabelGravity(int gravity) {
        ((FrameLayout.LayoutParams) tvCity.getLayoutParams()).gravity = gravity;
        tvCity.requestLayout();
    }

    public static class LoadMoreScrollListener extends RecyclerView.OnScrollListener {

        private static final int MINIMUM_THRESHOLD = 2;

        WeakReference<CityEditView> weakReference;
        WeakReference<CitySearchAdapter> adapterReference;

        public LoadMoreScrollListener(CityEditView cityEditView, CitySearchAdapter adapter) {
            weakReference = new WeakReference<>(cityEditView);
            adapterReference = new WeakReference<>(adapter);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            CityEditView editView = weakReference.get();
            CitySearchAdapter adapter = adapterReference.get();

            if (editView == null || adapter == null) {
                return;
            }

            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            int totalItemCount = layoutManager.getItemCount();
            int lastVisible = layoutManager.findLastVisibleItemPosition();

            boolean mustLoadMore = totalItemCount <= (lastVisible + MINIMUM_THRESHOLD);

            if (mustLoadMore && editView.hasMore) {
                editView.loadMore();
            }
        }
    }

    public static class SavedState implements Parcelable {
        Parcelable parentState;

        String query;
        CitySearchResponse response;
        boolean editing;
        boolean hasMore;

        public SavedState(Parcelable parentState, String query, CitySearchResponse response, boolean editing, boolean hasMore) {
            this.parentState = parentState;
            this.query = query;
            this.response = response;
            this.editing = editing;
            this.hasMore = hasMore;
        }

        protected SavedState(Parcel in) {
            ClassLoader cl = CitySearchResponse.class.getClassLoader();

            parentState = in.readParcelable(cl);
            response = in.readParcelable(cl);

            query = in.readString();
            editing = (boolean) in.readValue(cl);
            hasMore = (boolean) in.readValue(cl);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(response, i);
            parcel.writeParcelable(parentState, i);

            parcel.writeString(query);

            parcel.writeValue(editing);
            parcel.writeValue(hasMore);
        }
    }
}
