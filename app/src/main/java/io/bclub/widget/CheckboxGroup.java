package io.bclub.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import io.bclub.bus.GenericPublishSubject;
import io.bclub.bus.PublishItem;

public class CheckboxGroup extends LinearLayout {

    int subjectEventType;

    public CheckboxGroup(Context context) {
        super(context);
    }

    public CheckboxGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckboxGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckboxGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSubjectEventType(int subjectEventType) {
        this.subjectEventType = subjectEventType;
    }

    public void notifyChecked(int id) {
        for (int i = 0, size = getChildCount(); i < size; ++i) {
            if (getChildAt(i).getId() == id) {
                GenericPublishSubject.PUBLISH_SUBJECT
                        .onNext(PublishItem.of(subjectEventType, i));

                break;
            }
        }
    }
}
