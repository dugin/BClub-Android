package io.bclub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.model.Card;

import java.text.ParseException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.bclub.R;
import io.bclub.controller.ApiController;
import io.bclub.controller.StripeController;
import io.bclub.model.User;
import io.bclub.model.UserInfo;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.Constants;
import io.bclub.util.Mask;
import rx.Subscription;

public class BillingInformationActivity extends BaseActivity {

    @Inject
    ApiController apiController;

    @Inject
    StripeController stripeController;

    @BindView(R.id.et_card_number)
    EditText etCardNumber;

    @BindView(R.id.tv_card_number_error)
    TextView tvCardNumberError;

    @BindView(R.id.et_complete_name)
    EditText etCompleteName;

    @BindView(R.id.tv_complete_name_error)
    TextView tvCompleteNameError;

    @BindView(R.id.et_card_validity)
    EditText etCardValidity;

    @BindView(R.id.tv_validity_error)
    TextView tvValidityError;

    @BindView(R.id.et_security_code)
    EditText etSecurityCode;

    @BindView(R.id.tv_security_code_error)
    TextView tvSecurityCodeError;

    @BindView(R.id.btn_register)
    Button btnRegister;

    UserInfo userInfo;

    User user;

    String plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (!intent.hasExtra(Constants.USER_INFO_EXTRA) && !intent.hasExtra(Constants.USER_EXTRA)) {
            finish();
            return;
        }

        plan = intent.getStringExtra(Constants.PLAN_EXTRA);

        user = intent.getParcelableExtra(Constants.USER_EXTRA);
        userInfo = intent.getParcelableExtra(Constants.USER_INFO_EXTRA);

        setContentView(R.layout.activity_billing_information);

        activityComponent.inject(this);

        setupToolbarAlpha(getString(R.string.registration));
        showAppBarArrow();

        setupMasks();
        btnRegister.setEnabled(canSubmit());
    }

    void setupMasks() {
        Mask.insert(Mask.CREDIT_CARD, etCardNumber);
        Mask.insert(Mask.MONTH_YEAR, etCardValidity);
    }

    @OnClick(R.id.btn_register)
    void onRegisterClicked() {
        String completeName = etCompleteName.getText().toString().trim();
        String cardNumber = Mask.unmask(etCardNumber.getText().toString()).trim();
        String validity = etCardValidity.getText().toString().trim();
        String securityCode = etSecurityCode.getText().toString().trim();

        boolean valid = true;

        if (completeName.isEmpty()) {
            tvCompleteNameError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (cardNumber.length() < 16) {
            tvCardNumberError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (validity.isEmpty()) {
            tvValidityError.setVisibility(View.VISIBLE);
        } else {
            try {
                Constants.DAY_MOTH_YEAR.parse("01/" + validity);
            } catch (ParseException e) {
                tvValidityError.setVisibility(View.VISIBLE);
                valid = false;
            }
        }

        if (securityCode.length() < 3) {
            tvSecurityCodeError.setVisibility(View.VISIBLE);
            valid = false;
        }

        Card card = getCard(cardNumber, validity, securityCode, completeName);

        if (card == null) {
            //noinspection Range
            snack(R.string.invalid_card_number, Snackbar.LENGTH_LONG);
            valid = false;
        }

        if (valid) {
            createUser(userInfo, card);
        }
    }

    void createUser(UserInfo userInfo, Card card) {
        showLoading();

        Subscription subscription = null;

        if (user == null && userInfo != null) {
            subscription = stripeController.getToken(card)
                    .flatMap(token -> apiController.createUser(userInfo, token).map(user -> Pair.create(user, token)))
                    .flatMap(pair -> apiController.subscribeUserToPlan(pair.first, pair.second))
                    .compose(SingleSchedulerTransformer.ofIOToMainThread())
                    .subscribe(user -> {
                        hideLoading();
                        showConfirmationDialog();
                    }, error -> {
                        hideLoading();
                        snack(error);
                    });
        } else if (user != null) {
            subscription = stripeController.getToken(card)
                    .flatMap(token -> apiController.addTokenAndPlan(user, token, plan).map(user -> token))
                    .flatMap(token -> apiController.subscribeUserToPlan(user, token))
                    .compose(SingleSchedulerTransformer.ofIOToMainThread())
                    .subscribe(result -> {
                        hideLoading();

                        toast(R.string.subscription_created_with_success, Toast.LENGTH_LONG);
                        backToUserDetail();
                    }, error -> {
                        hideLoading();
                        snack(error);
                    });
        }

        addSubscription(subscription);
    }

    void backToUserDetail() {
        Intent intent = new Intent(this, UserDetailActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

    Card getCard(String number, String validityStr, String securityCode, String name) {
        String[] monthYear = validityStr.split("/");

        int month = Integer.parseInt(monthYear[0]);
        int year = Integer.parseInt(monthYear[1]);

        return stripeController.getCard(number, month, year, securityCode, name);
    }

    @OnTextChanged(R.id.et_complete_name)
    void onCompleteNameTextChanged(CharSequence text) {
        tvCompleteNameError.setVisibility(View.GONE);
        onTextChanged();
    }

    @OnTextChanged(R.id.et_card_number)
    void onCardNumberTextChanged(CharSequence text) {
        tvCardNumberError.setVisibility(View.GONE);
        onTextChanged();
    }

    @OnTextChanged(R.id.et_card_validity)
    void onValidityTextChanged(CharSequence text) {
        tvValidityError.setVisibility(View.GONE);
        onTextChanged();
    }

    @OnTextChanged(R.id.et_security_code)
    void onSecurityCodeTextChanged(CharSequence text) {
        tvSecurityCodeError.setVisibility(View.GONE);
        onTextChanged();
    }

    void onTextChanged() {
        btnRegister.setEnabled(canSubmit());
    }

    boolean canSubmit() {
        return etCardNumber.length() > 0
                && etCompleteName.length() > 0
                && etSecurityCode.length() > 0
                && etCardValidity.length() > 0;
    }
}
