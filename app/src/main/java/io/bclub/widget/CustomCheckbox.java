package io.bclub.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class CustomCheckbox extends CheckBox {

    public CustomCheckbox(Context context) {
        super(context);
    }

    public CustomCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomCheckbox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        if (getParent() instanceof CheckboxGroup) {
            ((CheckboxGroup) getParent()).notifyChecked(getId());
        }
    }
}
