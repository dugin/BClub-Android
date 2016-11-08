package io.bclub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.bclub.R;
import io.bclub.controller.ApiController;
import io.bclub.model.Plan;
import io.bclub.model.User;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.Constants;
import rx.Subscription;

public class UserDetailActivity extends BaseActivity {

    private static final String USER_EXTRA = "USER";

    @Inject
    ApiController apiController;

    @BindView(R.id.tv_activated_at)
    TextView tvActivatedAt;

    @BindView(R.id.tv_pricing)
    TextView tvPricing;

    @BindView(R.id.tv_valid_until)
    TextView tvValidUntil;

    @BindView(R.id.no_plan_container)
    ViewGroup noPlanContainer;

    @BindView(R.id.plan_info_container)
    ViewGroup planContainer;

    User user;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (user != null) {
            outState.putParcelable(USER_EXTRA, user);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityComponent.inject(this);

        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable(USER_EXTRA);
        }

        setContentView(R.layout.activity_user_detail);

        setupToolbarAlpha(getString(R.string.my_bclub));
        showAppBarArrow();

        fetchCurrentUserInfo();
    }

    void fetchCurrentUserInfo() {
        if (user == null) {
            showLoading();

            Subscription subscription = apiController.getCurrentUser()
                    .compose(SingleSchedulerTransformer.ofIOToMainThread())
                    .subscribe(user -> {
                        hideLoading();
                        this.user = user;

                        bindUser();
                    }, error -> {
                        hideLoading();
                        snack(error);
                    });

            addSubscription(subscription);
        } else {
            bindUser();
        }
    }

    void bindUser() {
        if (user.mustSubscribePlan()) {
            noPlanContainer.setVisibility(View.VISIBLE);
            planContainer.setVisibility(View.GONE);
        } else {
            noPlanContainer.setVisibility(View.GONE);
            planContainer.setVisibility(View.VISIBLE);

            bindPlanData();
        }
    }

    void bindPlanData() {
        if (user.validUntil != null) {
            tvValidUntil.setText(Constants.DAY_MOTH_YEAR.format(user.validUntil));
        }

        if (user.subscriptionDate != null) {
            tvActivatedAt.setText(Constants.DAY_MOTH_YEAR.format(user.subscriptionDate));
        }

        if (user.plan != null) {

            DecimalFormat df = (DecimalFormat) Constants.DECIMAL_FORMAT.clone();

            df.setMinimumFractionDigits(2);

            switch (user.plan) {
                case Plan.VOUCHER:
                    if (user.voucher != null) {
                        tvPricing.setText(getString(R.string.voucher).concat(": ").concat(user.voucher));
                    }

                    break;
                case Plan.MONTHLY:
                    tvPricing.setText(String.format(Constants.PT_BR, "1x %s %s", getString(R.string.of), df.format(15.00)));
                    break;
                case Plan.SIX_MONTH:
                    tvPricing.setText(String.format(Constants.PT_BR, "6x %s %s", getString(R.string.of), df.format(12.00)));
                    break;
                case Plan.ANNUAL:
                    tvPricing.setText(String.format(Constants.PT_BR, "12x %s %s ", getString(R.string.of), df.format(9.00)));
                    break;
            }
        }
    }

    @OnClick(R.id.btn_payment)
    void onPaymentClicked() {
        Intent intent = new Intent(this, PlanActivity.class);

        intent.putExtra(PARENT_EXTRA, UserDetailActivity.class.getName());
        intent.putExtra(Constants.USER_EXTRA, user);

        startActivity(intent);
    }
}
