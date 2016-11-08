package io.bclub.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import io.bclub.R;
import io.bclub.controller.ApiController;
import io.bclub.dagger.Injector;
import io.bclub.rx.SingleSchedulerTransformer;
import io.bclub.util.DisplayHelper;
import rx.Subscription;

public class VoucherInputDialogFragment extends AppCompatDialogFragment {

    OnVoucherSelectedListener listener;

    @Inject
    ApiController apiController;

    @BindView(R.id.et_voucher)
    EditText etVoucher;

    @BindView(R.id.tv_voucher_error)
    TextView tvVoucherError;

    Subscription subscription;

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    public VoucherInputDialogFragment() {
        setCancelable(true);
        setShowsDialog(true);
    }

    public VoucherInputDialogFragment setOnValueSetListener(OnVoucherSelectedListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtainActivityComponent(getContext()).inject(this);
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_voucher, null);

        int spacing = DisplayHelper.dpToPixels(context, 18);

        builder.setTitle(R.string.enter_the_voucher);
        builder.setView(view, spacing, spacing, spacing, 0);

        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        ButterKnife.bind(this, view);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog alertDialog = (AlertDialog) getDialog();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> validateVoucher(etVoucher.getText().toString().trim()));
    }

    @OnTextChanged(R.id.et_voucher)
    void onVoucherTextChanged() {
        tvVoucherError.setVisibility(View.GONE);
    }

    @OnEditorAction(R.id.et_voucher)
    boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_GO && textView.length() > 0) {
            validateVoucher(etVoucher.getText().toString().trim());
            return true;
        }

        return false;
    }

    void validateVoucher(String voucher) {
        if (voucher.isEmpty()) {
            tvVoucherError.setVisibility(View.VISIBLE);
            return;
        }

        subscription = apiController.validateVoucher(voucher, null)
                .compose(SingleSchedulerTransformer.ofIOToMainThread())
                .subscribe(result -> {
                    if (listener != null) {
                        listener.onVoucherSelected(result);
                    }

                    dismiss();
                }, error -> tvVoucherError.setVisibility(View.VISIBLE));
    }

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(String voucher);
    }
}
