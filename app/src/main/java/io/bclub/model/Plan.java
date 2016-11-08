package io.bclub.model;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({Plan.ANNUAL, Plan.SIX_MONTH, Plan.MONTHLY, Plan.VOUCHER})
public @interface Plan {
    String ANNUAL = "BCLUB12";
    String SIX_MONTH = "BCLUB6";
    String MONTHLY = "BCLUB1";
    String VOUCHER = "voucher";
}
