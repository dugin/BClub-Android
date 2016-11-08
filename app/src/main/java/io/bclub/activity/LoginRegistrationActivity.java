package io.bclub.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import io.bclub.model.User;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.CPF;
import io.bclub.util.Constants;
import io.bclub.util.Mask;
import rx.Subscription;

public class LoginRegistrationActivity extends BaseActivity {

    @Inject
    ApiController apiController;

    @BindView(R.id.et_cpf)
    EditText etCpf;

    @BindView(R.id.et_email)
    EditText etEmail;

    @BindView(R.id.tv_cpf_error)
    TextView tvCpfError;

    @BindView(R.id.tv_email_error)
    TextView tvEmailError;

    @BindView(R.id.btn_login)
    Button btnLogin;

    @BindView(R.id.btn_plans)
    Button btnPlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_registration);

        activityComponent.inject(this);

        setupToolbarAlpha(getString(R.string.registration));
        showAppBarArrow();

        Mask.insert(Mask.CPF_MASK, etCpf);

        btnLogin.setEnabled(canSubmit());
    }

    @OnEditorAction(R.id.et_cpf)
    boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO && canSubmit()) {
            onLoginClicked();
            return true;
        }

        return false;
    }

    @OnClick(R.id.btn_login)
    void onLoginClicked() {
        String cpf = Mask.unmask(etCpf.getText().toString().trim());
        String email = etEmail.getText().toString().trim();

        boolean valid = true;

        if (cpf.isEmpty() || !CPF.isValid(cpf)) {
            tvCpfError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (valid) {
            login(cpf, email);
        }
    }

    private void login(String cpf, String email) {
        showLoading();

        Subscription subscription = apiController.login(cpf, email)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(user -> {
                    hideLoading();



                    goToUserDetail(user);
                    finish();
                }, error -> {
                    hideLoading();

                    if (error instanceof ActionException && ((ActionException) error).messageResId == R.string.must_confirm_email) {
                        showConfirmationDialog(email, cpf);
                    } else {
                        snack(error);
                    }
                });

        addSubscription(subscription);
    }

    void showConfirmationDialog(String email, String cpf) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_email_title)
                .setMessage(R.string.confirm_email_summary)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setNeutralButton(R.string.confirm_email_neutral, (dialog, which) -> resendConfirmationEmail(email, cpf))
                .show();
    }

    void resendConfirmationEmail(String email, String cpf) {
        showLoading();

        Subscription subscription = apiController.resendConfirmationEmail(email, cpf)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(result -> {
                    hideLoading();
                    snack(R.string.confirmation_email_sent, Snackbar.LENGTH_LONG);
                }, error -> {
                    hideLoading();
                    snack(error);
                });

        addSubscription(subscription);
    }

    void goToUserDetail(User user) {
        Intent intent = new Intent(this, UserDetailActivity.class);

        intent.putExtra(PARENT_EXTRA, LoginRegistrationActivity.class.getName());
        intent.putExtra(Constants.USER_EXTRA, user);

        startActivity(intent);
    }

    @OnClick(R.id.btn_plans)
    void onPlansClicked() {
        Intent intent = new Intent(this, PlanActivity.class);

        intent.putExtra(PARENT_EXTRA, LoginRegistrationActivity.class);

        startActivity(intent);
    }

    @OnTextChanged(R.id.et_cpf)
    void onCpfTextChanged(CharSequence text) {
        tvCpfError.setVisibility(View.GONE);

        btnLogin.setEnabled(canSubmit());
    }

    @OnTextChanged(R.id.et_email)
    void onEmailTextChanged(CharSequence text) {
        tvEmailError.setVisibility(View.GONE);

        btnLogin.setEnabled(canSubmit());
    }

    boolean canSubmit() {
        return etCpf.length() > 0
                && etEmail.length() > 0;
    }
}
