package io.bclub.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.bclub.BuildConfig;
import io.bclub.R;
import io.bclub.controller.ApiController;
import io.bclub.model.Plan;
import io.bclub.model.UserInfo;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.CPF;
import io.bclub.util.Constants;
import io.bclub.util.Mask;
import rx.Subscription;

public class UserInformationActivity extends BaseActivity {

    @Inject
    ApiController apiController;

    @BindView(R.id.et_name)
    EditText etName;

    @BindView(R.id.tv_name_error)
    TextView tvNameError;

    @BindView(R.id.et_surname)
    EditText etSurname;

    @BindView(R.id.tv_surname_error)
    TextView tvSurnameError;

    @BindView(R.id.et_cpf)
    EditText etCpf;

    @BindView(R.id.tv_cpf_error)
    TextView tvCpfError;

    @BindView(R.id.tv_birthdate)
    TextView tvBirthdate;

    @BindView(R.id.tv_birthdate_error)
    TextView tvBirthdateError;

    @BindView(R.id.et_telephone)
    EditText etTelephone;

    @BindView(R.id.tv_telephone_error)
    TextView tvTelephoneError;

    @BindView(R.id.tv_email)
    TextView tvEmail;

    @BindView(R.id.et_address)
    EditText etAddress;

    @BindView(R.id.tv_address_error)
    TextView tvAddressError;

    @BindView(R.id.et_complement)
    EditText etComplement;

    @BindView(R.id.tv_complement_error)
    TextView tvComplementError;

    @BindView(R.id.et_cep)
    EditText etCEP;

    @BindView(R.id.tv_cep_error)
    TextView tvCEPError;

    @BindView(R.id.et_state)
    EditText etState;

    @BindView(R.id.tv_state_error)
    TextView tvStateError;

    @BindView(R.id.et_city)
    EditText etCity;

    @BindView(R.id.tv_city_error)
    TextView tvCityError;

    @BindView(R.id.cb_tos)
    CheckBox cbTos;

    @BindView(R.id.tv_tos)
    TextView tvTos;

    @BindView(R.id.btn_register)
    Button btnRegister;

    @Plan
    String plan;

    String voucher;

    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (!intent.hasExtra(Constants.PLAN_EXTRA) || !intent.hasExtra(Constants.EMAIL_EXTRA)) {
            finish();
            return;
        }

        voucher =  intent.getStringExtra(Constants.VOUCHER_EXTRA);
        email = intent.getStringExtra(Constants.EMAIL_EXTRA);
        //noinspection WrongConstant
        plan =  intent.getStringExtra(Constants.PLAN_EXTRA);

        if (Plan.VOUCHER.equals(plan) && voucher == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_user_information);

        activityComponent.inject(this);

        setupToolbarAlpha(getString(R.string.registration));
        showAppBarArrow();

        setupTosCheckbox();
        setupMasks();
        setupDatePicker();

        tvEmail.setText(email);
        btnRegister.setEnabled(canSubmit());
    }

    @OnClick(R.id.tos_container)
    void onTosClicked() {
        cbTos.setChecked(!cbTos.isChecked());
    }

    @OnClick(R.id.tv_tos)
    void onTextTosClicked() {
        onTosClicked();
    }

    void setupTosCheckbox() {
        String firstPart = getString(R.string.read_and_accept);
        SpannableStringBuilder ssb = new SpannableStringBuilder(firstPart);

        ssb.append(getString(R.string.terms_and_conditions));
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.accent)), firstPart.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                launchIntentIfPossible(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WEBSITE.concat("/termos_servico.html"))));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, firstPart.length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTos.setMovementMethod(LinkMovementMethod.getInstance());
        tvTos.setText(ssb);
    }

    void setupMasks() {
        Mask.insertPhoneMask(etTelephone);
        Mask.insert(Mask.CPF_MASK, etCpf);
        Mask.insert(Mask.ZIP_CODE_PT_BR, etCEP);
    }

    void setupDatePicker() {
        tvBirthdate.setOnClickListener(v -> {
            String birthdate = tvBirthdate.getText().toString();
            Calendar calendar = Calendar.getInstance();

            int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);

            if (!birthdate.isEmpty()) {
                String[] split = birthdate.split("/");

                day = Integer.parseInt(split[0]);
                month = Integer.parseInt(split[1]) - 1;
                year = Integer.parseInt(split[2]);
            }

            new DatePickerDialog(this, this::onDateSet, year, month, day).show();
        });
    }

    void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String dayStr = ((dayOfMonth < 10) ? "0" : "").concat(Integer.toString(dayOfMonth));
        String monthStr = ((monthOfYear + 1) < 10 ? "0" : "").concat(Integer.toString(monthOfYear + 1));

        tvBirthdate.setText(String.format(Constants.PT_BR, "%s/%s/%d", dayStr, monthStr, year));
    }

    void onTextChanged(CharSequence text) {
        btnRegister.setEnabled(canSubmit());
    }

    @OnTextChanged(R.id.et_name)
    void onNameTextChanged(CharSequence text) {
        onTextChanged(text);
        tvNameError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_surname)
    void onSurnameTextChanged(CharSequence text) {
        onTextChanged(text);
        tvSurnameError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_cpf)
    void onCpfTextChanged(CharSequence text) {
        onTextChanged(text);
        tvCpfError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.tv_birthdate)
    void onBirthdateTextChanged(CharSequence text) {
        onTextChanged(text);
        tvBirthdateError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_telephone)
    void onTelephoneTextChanged(CharSequence text) {
        onTextChanged(text);
        tvTelephoneError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_address)
    void onAddressTextChanged(CharSequence text) {
        onTextChanged(text);
        tvAddressError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_complement)
    void onComplementTextChanged(CharSequence text) {
        onTextChanged(text);
        tvComplementError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_cep)
    void onCEPTextChanged(CharSequence text) {
        onTextChanged(text);
        tvCEPError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_state)
    void onStateTextChanged(CharSequence text) {
        onTextChanged(text);
        tvStateError.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.et_city)
    void onCityTextChanged(CharSequence text) {
        onTextChanged(text);
        tvCityError.setVisibility(View.GONE);
    }

    @OnCheckedChanged(R.id.cb_tos)
    void onTosCheckedChanged() {
        btnRegister.setEnabled(canSubmit());
    }

    @OnClick(R.id.btn_register)
    void onRegisterClicked() {
        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String cpf = Mask.unmask(etCpf.getText().toString()).trim();

        String birthdate = tvBirthdate.getText().toString().trim();

        String telephone = Mask.unmask(etTelephone.getText().toString()).trim();

        String address = etAddress.getText().toString().trim();
        String complement = etComplement.getText().toString().trim();
        String cep = Mask.unmask(etCEP.getText().toString().trim());
        String state = etState.getText().toString().trim();
        String city = etCity.getText().toString().trim();

        Date birthdateObject = null;

        boolean valid = true;

        if (name.isEmpty()) {
            tvNameError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (surname.isEmpty()) {
            tvSurnameError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (!CPF.isValid(cpf)) {
            tvCpfError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (birthdate.isEmpty()) {
            tvBirthdateError.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            try {
                birthdateObject = Constants.DAY_MOTH_YEAR.parse(birthdate);

                if (!birthdateObject.before(new Date())) {
                    valid = false;
                    tvBirthdateError.setVisibility(View.VISIBLE);
                }
            } catch (ParseException e) {
                tvBirthdateError.setVisibility(View.VISIBLE);
                valid = false;
            }
        }

        if (telephone.isEmpty() || telephone.length() < 8) {
            tvTelephoneError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (address.isEmpty()) {
            tvAddressError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (complement.isEmpty()) {
            tvComplementError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (cep.length() < 8) {
            tvCEPError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (state.isEmpty()) {
            tvStateError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (city.isEmpty()) {
            tvCityError.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (valid) {
            verifyCpf(new UserInfo(name, surname, email, cpf, birthdateObject, telephone, voucher, plan, address, complement, cep, city, state));
        }
    }

    void verifyCpf(final UserInfo userInfo) {
        showLoading();

        Subscription subscription = apiController.checkCpfExistence(userInfo.cpf)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(result -> {
                    hideLoading();

                    if (!result) {
                        if (voucher != null) {
                            useVoucher(userInfo);
                        } else {
                            goToNextStep(userInfo);
                        }
                    } else {
                        tvCpfError.setVisibility(View.VISIBLE);
                    }
                }, error -> {
                    hideLoading();
                    snack(error);
                });

        addSubscription(subscription);
    }

    void useVoucher(UserInfo userInfo) {
        showLoading();

        Subscription subscription = apiController.createUser(userInfo, null)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(user -> {
                    hideLoading();
                    showConfirmationDialog();
                }, error -> {
                    hideLoading();
                    snack(error);
                });

        addSubscription(subscription);
    }

    void goToNextStep(UserInfo userInfo) {
        Intent intent = new Intent(this, BillingInformationActivity.class);

        intent.putExtra(PARENT_EXTRA, UserInformationActivity.class.getName());
        intent.putExtra(Constants.USER_INFO_EXTRA, userInfo);

        startActivity(intent);
    }

    boolean canSubmit() {
        return etName.length() > 0
                && etSurname.length() > 0
                && etCpf.length() > 0
                && tvBirthdate.length() > 0
                && cbTos.isChecked()
                && etTelephone.length() > 0
                && etAddress.length() > 0
                && etComplement.length() > 0
                && etCEP.length() > 0
                && etState.length() > 0
                && etCity.length() > 0;
    }
}
