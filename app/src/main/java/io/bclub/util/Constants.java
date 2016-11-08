package io.bclub.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class Constants {
    public static final Locale PT_BR = new Locale("pt", "BR");
    public static final SimpleDateFormat DAY_MOTH_YEAR = new SimpleDateFormat("dd/MM/yyyy", PT_BR);

    public static final int RC_REQUEST_LOCATION_FIND_PERSONS = 231;
    public static final int RC_GPS_ERROR_RESOLUTION = 15;

    public static final String PLAN_EXTRA = "PLAN";
    public static final String VOUCHER_EXTRA = "VOUCHER";
    public static final String EMAIL_EXTRA = "EMAIL";

    public static final String USER_EXTRA = "USER";
    public static final String USER_INFO_EXTRA = "USER_INFO";

    public static final DecimalFormat DECIMAL_FORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Constants.PT_BR);

    static {
        DecimalFormatSymbols symbols = DECIMAL_FORMAT.getDecimalFormatSymbols();

        symbols.setDecimalSeparator(',');

        DECIMAL_FORMAT.setDecimalFormatSymbols(symbols);
        DECIMAL_FORMAT.setMaximumFractionDigits(2);
        DECIMAL_FORMAT.setMinimumFractionDigits(1);

        DAY_MOTH_YEAR.setLenient(false);
    }

    private Constants() { }
}
