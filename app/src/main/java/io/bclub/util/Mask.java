package io.bclub.util;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Source: https://andremrezende.wordpress.com/tag/android-mask-mascara-edittext-java-layout-cpf-cnpj/
 */
public abstract class Mask {

    @StringDef({PHONE_9_MASK, PHONE_8_MASK, CPF_MASK, ZIP_CODE_PT_BR, MONTH_YEAR, CREDIT_CARD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MaskType {}

    public static final String PHONE_9_MASK = "(##) #####-####";
    public static final String PHONE_8_MASK =  "(##) ####-####";
    public static final String CPF_MASK     =  "###.###.###-##";
    public static final String ZIP_CODE_PT_BR = "#####-###";

    public static final String MONTH_YEAR = "##/##";

    public static final String CREDIT_CARD = "#### #### #### ####";

    @NonNull
    public static String unmask(String s) {
        return s.replaceAll("[\\./\\(\\) \\-\\+]", "");
    }

    @NonNull
    public static TextWatcher insert(@MaskType final String mask, final EditText editText) {
        MaskTextWatcher textWatcher = new MaskTextWatcher(editText, mask);

        editText.addTextChangedListener(textWatcher);

        return textWatcher;
    }

    @NonNull
    public static TextWatcher insertPhoneMask(final EditText editText) {
        MaskTextWatcher textWatcher = new MaskTextWatcher(editText, null) {
            @Override
            protected String getMask(String unmaskedValue) {
                if (unmaskedValue.length() < 11) {
                    return PHONE_8_MASK;
                }

                return PHONE_9_MASK;
            }
        };

        editText.addTextChangedListener(textWatcher);

        return textWatcher;
    }

    public static String insertPhoneMask(String phone) {
        int length = phone.length();

        if (length == 8)
            phone = String.format("%s-%s", phone.substring(0, 4), phone.substring(4, phone.length()));
        else if (length == 9)
            phone = String.format("%s-%s", phone.substring(0, 5), phone.substring(5, phone.length()));
        else if (length == 10)
            phone = String.format("%s %s-%s", phone.substring(0, 2), phone.substring(2, 6), phone.substring(6, phone.length()));
        else if (length == 11)
            phone = String.format("%s %s-%s", phone.substring(0, 2), phone.substring(2, 7), phone.substring(7, phone.length()));

        return phone;
    }

    private static class MaskTextWatcher extends SimpleTextWatcher {

        WeakReference<EditText> weakEditText;
        String mask, oldValue = "";

        boolean isUpdating;

        public MaskTextWatcher(EditText editText) {
            weakEditText = new WeakReference<>(editText);
        }

        public MaskTextWatcher(EditText editText, String mask) {
            this(editText);
            this.mask = mask;
        }

        protected String getMask(String unmaskedValue) {
            return mask;
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String unmaskedString = Mask.unmask(s.toString());
            StringBuilder maskedString = new StringBuilder("");
            String mask = getMask(unmaskedString);

            EditText editText = weakEditText.get();

            // EditText was GC'ed
            if (editText == null) {
                return;
            }

            if (isUpdating) {
                oldValue = unmaskedString;
                isUpdating = false;

                return;
            }

            int i = 0;

            for (char m : mask.toCharArray()) {
                if (m != '#' && i < unmaskedString.length()) {
                    maskedString.append(m);
                    continue;
                }

                try {
                    maskedString.append(unmaskedString.charAt(i));
                } catch (Exception e) {
                    break;
                }

                i++;
            }

            isUpdating = true;

            editText.setText(maskedString);
            editText.setSelection(maskedString.length());
        }
    }

    public static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) { }
    }
}