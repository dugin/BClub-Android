package io.bclub.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public abstract class DisplayHelper {

    private DisplayHelper() { }

    public static int dpToPixels(Context context, float dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics);
    }

    public static int dpToPixelsAtMaxDensity(float dpValue) {
        return (int) (dpValue * (640 / DisplayMetrics.DENSITY_MEDIUM));
    }

    public static int spToPixel(Context context, float sp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();

        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }

        return result;
    }

    @NonNull
    public static Point scaleWithAspectRatio(double sourceWidth, double sourceHeight, double destWidth, double destHeight) {
        double scaleHeight = destHeight / sourceHeight;
        double scaleWidth = destWidth / sourceWidth;
        double scale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;

        return new Point((int) (sourceWidth * scale), (int) (sourceHeight * scale));
    }

    public static Point getWindowSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();

        Point p = new Point();
        display.getSize(p);

        return p;
    }

    public static void showSoftKeyboard(Context context, EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
    }

    public static void hideSoftKeyboard(@NonNull Context context, @Nullable View rootView) {
        if (rootView == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = rootView.findFocus();

        imm.hideSoftInputFromWindow((focusedView == null ? rootView : focusedView).getApplicationWindowToken(), 0);
        imm.hideSoftInputFromWindow((focusedView == null ? rootView : focusedView).getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}
