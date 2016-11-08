package io.bclub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import io.bclub.R;
import io.bclub.controller.ApiController;
import io.bclub.exception.ActionException;
import io.bclub.model.Plan;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.Constants;
import rx.Subscription;

public class RegistrationActivity extends BaseActivity {

    @Inject
    ApiController apiController;

    @BindView(R.id.et_email)
    EditText etEmail;

    @BindView(R.id.tv_email_error)
    TextView tvEmailError;

    @BindView(R.id.et_voucher)
    EditText etVoucher;

    @BindView(R.id.tv_voucher_error)
    TextView tvVoucherError;

    @BindView(R.id.btn_register)
    Button btnRegister;

    @BindView(R.id.tv_summary)
    TextView tvSummary;

    @BindView(R.id.tv_subtitle)
    TextView tvSubtitle;

    @Plan
    String plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(Constants.PLAN_EXTRA)) {
            finish();
            return;
        }

        //noinspection WrongConstant
        plan = getIntent().getStringExtra(Constants.PLAN_EXTRA);

        setContentView(R.layout.activity_registration);

        activityComponent.inject(this);

        setupToolbarAlpha(getString(R.string.registration));
        showAppBarArrow();

        setupVoucherLayout();

        btnRegister.setEnabled(etEmail.length() > 0);
    }

    void setupVoucherLayout() {
        if (!Plan.VOUCHER.equals(plan)) {
            etVoucher.setVisibility(View.GONE);
        } else {
            tvSubtitle.setText(R.string.voucher_email_subtitle);
            tvSummary.setVisibility(View.VISIBLE);
        }
    }

    @OnEditorAction(R.id.et_email)
    boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO && textView.length() > 0) {
            onRegisterClicked();
            return true;
        }

        return false;
    }

    @OnTextChanged(R.id.et_email)
    void onTextChanged(CharSequence text) {
        tvEmailError.setVisibility(View.GONE);
        btnRegister.setEnabled(text.length() > 0);
    }

    @OnClick(R.id.btn_register)
    void onRegisterClicked() {
        String email = etEmail.getText().toString().trim();
        String voucher = etVoucher.getText().toString().trim();

        boolean valid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (Plan.VOUCHER.equals(plan)) {
            if (voucher.isEmpty()) {
                tvVoucherError.setVisibility(View.VISIBLE);
                valid = false;
            }
        } else {
            voucher = null;
        }

        if (valid) {
            verifyEmail(email, voucher);
        }
    }

    void verifyEmail(String email, String voucher) {
        showLoading();

        Subscription subscription = apiController.validateEmailAndVoucher(email, voucher)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(result -> {
                    hideLoading();

                    if (result) {
                        goToNextStep(email, voucher);
                    } else {
                        tvEmailError.setVisibility(View.VISIBLE);
                    }
                }, error -> {
                    hideLoading();

                    if (error instanceof ActionException) {
                        if (((ActionException) error).messageResId == R.string.invalid_voucher) {
                            tvVoucherError.setVisibility(View.VISIBLE);
                            return;
                        }
                    }

                    snack(error);
                });

        addSubscription(subscription);
    }

    void goToNextStep(String email, String voucher) {
        Intent intent = new Intent(this, UserInformationActivity.class);

        intent.putExtra(PARENT_EXTRA, RegistrationActivity.class.getName());

        intent.putExtra(Constants.VOUCHER_EXTRA, voucher);
        intent.putExtra(Constants.EMAIL_EXTRA, email);
        intent.putExtra(Constants.PLAN_EXTRA, plan);

        startActivity(intent);
    }
}
