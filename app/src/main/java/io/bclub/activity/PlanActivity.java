package io.bclub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import butterknife.BindView;
import butterknife.OnClick;
import io.bclub.R;
import io.bclub.model.Plan;
import io.bclub.model.User;
import io.bclub.util.Constants;

public class PlanActivity extends BaseActivity {

    @BindView(R.id.cv_voucher)
    CardView cvVoucher;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plan);

        if (getIntent().hasExtra(Constants.USER_EXTRA)) {
            user = getIntent().getParcelableExtra(Constants.USER_EXTRA);
            cvVoucher.setVisibility(View.GONE);
        }

        setupToolbarAlpha(getString(R.string.registration));
        showAppBarArrow();
    }

    @OnClick(R.id.cv_annual)
    void onAnnualClicked() {
        goToUserRegistrationWithPlan(Plan.ANNUAL);
    }

    @OnClick(R.id.cv_six_monthly)
    void onSixMonthClicked() {
        goToUserRegistrationWithPlan(Plan.SIX_MONTH);
    }

    @OnClick(R.id.cv_monthly)
    void onMonthlyClicked() {
        goToUserRegistrationWithPlan(Plan.MONTHLY);
    }

    @OnClick(R.id.cv_voucher)
    void onVoucherClicked() {
        goToUserRegistrationWithPlan(Plan.VOUCHER);
    }

    void goToUserRegistrationWithPlan(@Plan String plan) {
        goToUserRegistrationWithPlan(plan, null);
    }

    void goToUserRegistrationWithPlan(@Plan String plan, String voucher) {
        if (user == null) {
            Intent intent = new Intent(this, RegistrationActivity.class);

            intent.putExtra(PARENT_EXTRA, PlanActivity.class.getName());
            intent.putExtra(Constants.VOUCHER_EXTRA, voucher);
            intent.putExtra(Constants.PLAN_EXTRA, plan);

            startActivity(intent);
        } else {
            Intent intent = new Intent(this, BillingInformationActivity.class);

            intent.putExtra(PARENT_EXTRA, PlanActivity.class.getName());
            intent.putExtra(Constants.PLAN_EXTRA, plan);
            intent.putExtra(Constants.USER_EXTRA, user);

            startActivity(intent);
        }
    }
}