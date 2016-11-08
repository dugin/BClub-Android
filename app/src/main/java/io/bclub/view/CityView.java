package io.bclub.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.bclub.R;
import io.bclub.model.City;

public class CityView extends LinearLayout implements AbstractView<City> {

    @BindView(R.id.text_view)
    TextView textView;

    @BindView(R.id.divider)
    View divider;

    City city;

    public CityView(Context context) {
        super(context);
    }

    public CityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CityView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public void bind(City city) {
        this.city = city;
        textView.setText(city.name);
    }

    @Override
    public City get() {
        return city;
    }

    public void hideDivider() {
        divider.setVisibility(View.GONE);
    }

    public void showDivider() {
        divider.setVisibility(View.VISIBLE);
    }
}
