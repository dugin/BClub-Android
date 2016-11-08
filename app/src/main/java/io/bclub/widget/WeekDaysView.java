package io.bclub.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import io.bclub.R;
import io.bclub.util.DisplayHelper;
import io.bclub.util.ResourceHelper;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class WeekDaysView extends View {

    private static final int[] DAYS_RES_ID = {R.string.abbr_monday, R.string.abbr_tuesday, R.string.abbr_wednesday, R.string.abbr_thursday, R.string.abbr_friday, R.string.abbr_saturday, R.string.abbr_sunday};

    boolean[] weekDays = {false, false, false, false, false, false, false};
    int[] daysX = {0, 0, 0, 0, 0, 0, 0};

    int spacing;
    int cellWidth, cellHeight;

    int enabledTextColor;
    int textColor;

    int textSize;

    int backgroundColor;
    int enabledBackgroundColor;

    int pressedIndex = -1;

    TextPaint textPaint = new TextPaint();
    Paint backgroundPaint = new Paint();

    Drawable selectableItemBackground;

    Rect rect = new Rect();

    GestureDetector gestureDetector;

    OnDaySelectListener listener;

    GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            if (isOutsideView(x, y)) {
                return false;
            }

            int index = getDayIndex(x);

            weekDays[index] = ! weekDays[index];

            if (listener != null) {
                listener.onDaySelected(index, weekDays[index]);
            }

            return true;
        }
    };

    public WeekDaysView(Context context) {
        super(context);
    }

    public WeekDaysView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public WeekDaysView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WeekDaysView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    void init(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        if (isInEditMode()) {
            return;
        }

        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        if (attributeSet != null) {
            TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.WeekDaysView, defStyleAttr, defStyleRes);

            enabledBackgroundColor = attributes.getColor(R.styleable.WeekDaysView_enabledBackgroundColor, ContextCompat.getColor(context, R.color.accent));
            backgroundColor = attributes.getColor(R.styleable.WeekDaysView_backgroundColor, ContextCompat.getColor(context, R.color.workday_background_default));

            if (attributes.hasValue(R.styleable.WeekDaysView_enabledTextColor)) {
                enabledTextColor = attributes.getColor(R.styleable.WeekDaysView_enabledTextColor, Color.WHITE);
            } else {
                enabledTextColor = Color.WHITE;
            }

            if (attributes.hasValue(R.styleable.WeekDaysView_textColor)) {
                textColor = attributes.getColor(R.styleable.WeekDaysView_textColor, Color.GRAY);
            } else {
                textColor = Color.GRAY;
            }

            textSize = attributes.getDimensionPixelSize(R.styleable.WeekDaysView_textSize, DisplayHelper.spToPixel(context, 10));

            spacing = attributes.getDimensionPixelSize(R.styleable.WeekDaysView_spacing, DisplayHelper.dpToPixels(context, 2));

            attributes.recycle();
        }

        selectableItemBackground = ResourceHelper.resolveDrawableAttr(context, R.attr.selectableItemBackground);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(enabledTextColor);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);

        textPaint.setTypeface(Typeface.create(TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Bold.ttf"), Typeface.NORMAL));

        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        cellWidth = cellHeight = DisplayHelper.dpToPixels(context, 48);

        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    public void setOnDaySelectListener(OnDaySelectListener listener) {
        this.listener = listener;
    }

    public boolean[] getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(boolean[] weekDays) {
        if (weekDays.length < 7) {
            throw new IllegalArgumentException("Workdays must have 7 positions");
        }

        this.weekDays = weekDays;

        invalidate();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (isClickable()) {
            gestureDetector.onTouchEvent(event);
            updateSelector(event);

            return true;
        }

        return super.onTouchEvent(event);
    }

    void updateSelector(@NonNull MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
            if (isOutsideView(event.getX(), event.getY())) {
                setPressed(false);
                pressedIndex = -1;
            } else {
                setPressed(true);
                pressedIndex = getDayIndex(event.getX());
            }
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            setPressed(false);
            pressedIndex = -1;
        }

        selectableItemBackground.setState(getDrawableState());
        invalidate();
    }

    boolean isOutsideView(float x, float y) {
        return y < 0 && y > cellHeight;
    }

    int getDayIndex(float x) {
        for (int i = 0; i < daysX.length; ++i) {
            if (daysX[i] > x) {
                return i - 1;
            }
        }

        return daysX.length - 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;

        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        // Not matter, i want the maximum width
        if (Build.VERSION.SDK_INT >= 16) {
            width = Math.max(getMinimumWidth(), suggestedWidth);
        } else {
            width = suggestedWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            if (Build.VERSION.SDK_INT >= 16) {
                height = Math.max(getMinimumHeight(), suggestedHeight);
            } else {
                height = suggestedHeight;
            }
        } else {
            height = cellHeight;
        }

        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
                height = (suggestedWidth - (6 * spacing)) / 7;
            }

            int desiredWidth = (7 * height) + (6 * spacing);

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desiredWidth, width);
            } else {
                width = desiredWidth;
            }
        }

        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            int desiredHeight = (width - (6 * spacing)) / 7;

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(desiredHeight, height);
            } else {
                height = desiredHeight;
            }
        }

        //noinspection SuspiciousNameCombination
        cellWidth = height;
        cellHeight = height;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Context context = getContext();
        String text;

        int x = 0;

        int yAdjust = DisplayHelper.spToPixel(context, 2);
        int xAdjust = DisplayHelper.spToPixel(context, 2);

        for (int i = 0; i < 7; ++i) {
            if (weekDays != null && weekDays[i]) {
                backgroundPaint.setColor(enabledBackgroundColor);
                textPaint.setColor(enabledTextColor);
            } else {
                backgroundPaint.setColor(backgroundColor);
                textPaint.setColor(textColor);
            }

            text = context.getString(DAYS_RES_ID[i]).toUpperCase();

            measureText(text);

            int xText = x + (cellWidth / 2) + (rect.width() / 4) - xAdjust;
            int yText = (cellHeight / 2) + (rect.height() / 4) + yAdjust;

            canvas.drawRect(x, 0, x + cellWidth, cellHeight, backgroundPaint);
            canvas.drawText(text, xText, yText, textPaint);

            if (pressedIndex == i) {
                selectableItemBackground.setBounds(x, 0, x + cellWidth, cellHeight);
                selectableItemBackground.draw(canvas);
            }

            daysX[i] = x;

            x += (cellWidth + spacing);
        }
    }

    void measureText(String text) {
        textPaint.getTextBounds(text, 0, text.length(), rect);
    }

    public interface OnDaySelectListener {
        void onDaySelected(int day, boolean selected);
    }
}