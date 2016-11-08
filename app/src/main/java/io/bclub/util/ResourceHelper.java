package io.bclub.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public abstract class ResourceHelper {

    private ResourceHelper() { }

    public static String getStringResByName(Context context, String name, String defaultValue) {
        if (name == null) {
            return defaultValue;
        }

        int identifier = getResId(context, "string", name);

        if (identifier == 0) {
            return defaultValue;
        }

        return context.getString(identifier);
    }

    public static int resolveColorAttr(Context context, int attr, int defaultColor) {
        TypedArray a = resolveTypedArray(attr, context);
        int value = a.getColor(0, defaultColor);

        a.recycle();

        return value;
    }

    public static Drawable resolveDrawableAttr(Context context, int attr) {
        TypedArray a = resolveTypedArray(attr, context);
        Drawable value = a.getDrawable(0);

        a.recycle();

        return value;
    }

    private static TypedArray resolveTypedArray(int attrId, Context context) {
        TypedValue typedValue = new TypedValue();
        int[] attr = new int[] {attrId};

        return context.getTheme().obtainStyledAttributes(typedValue.data, attr);
    }

    public static int getResId(Context context, String defType, String name) {
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }
}
