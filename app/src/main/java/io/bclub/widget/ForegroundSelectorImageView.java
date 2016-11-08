package io.bclub.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import io.bclub.R;
import io.bclub.util.DisplayHelper;

public class ForegroundSelectorImageView extends ImageView {

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLOR_DRAWABLE_DIMENSION = 2;

    private int foregroundPadding;

    private Drawable foregroundDrawable;

    private Paint backgroundPaint, outerBackgroundPaint;

    private int circleBackgroundColor, circleBackgroundColorSelected;

    private final RectF drawableRect = new RectF();
    private final RectF borderRect = new RectF();

    private final Matrix shaderMatrix = new Matrix();

    private BitmapShader bitmapShader;
    private Paint bitmapPaint = new Paint();

    private Bitmap bitmap;

    private int bitmapWidth;
    private int bitmapHeight;

    private int circleStroke = 2;

    private boolean enabledBackgroundTransformation = false;

    public ForegroundSelectorImageView(Context context) {
        this(context, null);
    }

    public ForegroundSelectorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            configureAttributes(context, attrs);
        }
    }

    void configureAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ForegroundSelectorImageView);

        int foregroundColor = a.getColor(R.styleable.ForegroundSelectorImageView_foregroundColor, Color.TRANSPARENT);
        foregroundPadding = a.getDimensionPixelSize(R.styleable.ForegroundSelectorImageView_foregroundPadding, 0);

        circleBackgroundColor = a.getColor(R.styleable.ForegroundSelectorImageView_circleBackgroundColor, Color.TRANSPARENT);
        circleBackgroundColorSelected = a.getColor(R.styleable.ForegroundSelectorImageView_circleBackgroundColorSelected, circleBackgroundColor);

        if (a.hasValue(R.styleable.ForegroundSelectorImageView_foreground)) {
            foregroundDrawable = a.getDrawable(R.styleable.ForegroundSelectorImageView_foreground);
        } else {
            foregroundDrawable = new CircleDrawable(foregroundColor);
        }

        enabledBackgroundTransformation = a.getBoolean(R.styleable.ForegroundSelectorImageView_enabledBackgroundTransformation, false);

        circleStroke = DisplayHelper.dpToPixels(getContext(), 2);

        configureBackgroundPaint(circleBackgroundColor);

        a.recycle();
    }

    void configureBackgroundPaint(int circleBackgroundColor) {
        backgroundPaint = new Paint();

        backgroundPaint.setColor(circleBackgroundColor);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        outerBackgroundPaint = new Paint();

        outerBackgroundPaint.setColor(circleBackgroundColor);
        outerBackgroundPaint.setAntiAlias(true);
        outerBackgroundPaint.setStyle(Paint.Style.STROKE);
        outerBackgroundPaint.setStrokeWidth(circleStroke);
        outerBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    private void setup() {
        if (getWidth() == 0 && getHeight() == 0) {
            return;
        }

        if (bitmap == null) {
            invalidate();
            return;
        }

        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setShader(bitmapShader);

        bitmapHeight = bitmap.getHeight();
        bitmapWidth = bitmap.getWidth();

        borderRect.set(ViewCompat.getPaddingStart(this), getPaddingTop(), getWidth() - ViewCompat.getPaddingEnd(this), getHeight() - getPaddingBottom());

        drawableRect.set(borderRect);

        updateShaderMatrix();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();

        //noinspection SuspiciousNameCombination
        setMeasuredDimension(width, width);

        if (foregroundDrawable != null) {
            foregroundDrawable.setBounds(0, 0, width - 2 * foregroundPadding, width - 2 * foregroundPadding);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            backgroundPaint.setColor(circleBackgroundColorSelected);
            outerBackgroundPaint.setColor(circleBackgroundColorSelected);
        } else {
            backgroundPaint.setColor(circleBackgroundColor);
            outerBackgroundPaint.setColor(circleBackgroundColor);
        }

        super.setSelected(selected);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != View.VISIBLE) {
            return;
        }

        float width = getMeasuredWidth();
        float height = getMeasuredHeight();
        float radius = ((width > height) ? height : width) * 0.5F;

        float cx = width / 2f;
        float cy = height / 2f;

        if (isSelected() && enabledBackgroundTransformation) {
            canvas.drawCircle(cx, cy, radius - (radius * 0.15F), backgroundPaint);
            canvas.drawCircle(cx, cy, radius - (radius * 0.05F), outerBackgroundPaint);
        } else {
            canvas.drawCircle(cx, cy, radius, backgroundPaint);
        }

        if (bitmap != null) {
            canvas.drawCircle(width * 0.5F, height * 0.5F, radius, bitmapPaint);
        }

        if (isSelected() && foregroundDrawable != null) {
            int saveCount = canvas.getSaveCount();

            canvas.save();
            canvas.translate(foregroundPadding, foregroundPadding);

            foregroundDrawable.draw(canvas);

            canvas.restoreToCount(saveCount);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        bitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        bitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        bitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        bitmap = uri != null ? getBitmapFromDrawable(getDrawable()) : null;
        setup();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);

            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        shaderMatrix.set(null);

        if (bitmapWidth * drawableRect.height() > drawableRect.width() * bitmapHeight) {
            scale = drawableRect.height() / (float) bitmapHeight;
            dx = (drawableRect.width() - bitmapWidth * scale) * 0.5F;
        } else {
            scale = drawableRect.width() / (float) bitmapWidth;
            dy = (drawableRect.height() - bitmapHeight * scale) * 0.5F;
        }

        shaderMatrix.setScale(scale, scale);
        shaderMatrix.postTranslate((int) (dx + 0.5F) + drawableRect.left, (int) (dy + 0.5F) + drawableRect.top);

        bitmapShader.setLocalMatrix(shaderMatrix);
    }

    public void setCircleBackgroundColor(@ColorInt int circleBackgroundColor) {
        this.circleBackgroundColor = circleBackgroundColor;

        outerBackgroundPaint.setColor(circleBackgroundColor);
        backgroundPaint.setColor(circleBackgroundColor);
    }

    public void setCircleBackgroundColorSelected(@ColorInt int circleBackgroundColorSelected) {
        this.circleBackgroundColorSelected = circleBackgroundColorSelected;

        if (isSelected()) {
            outerBackgroundPaint.setColor(circleBackgroundColorSelected);
            backgroundPaint.setColor(circleBackgroundColorSelected);
        }
    }
}
