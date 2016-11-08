package io.bclub.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

public class CircleDrawable extends Drawable {

    Paint mPaint;

    public CircleDrawable(@ColorInt int color) {
        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float width = bounds.width();
        float height = bounds.height();
        float radius = ((width > height) ? height : width) / 2f;

        float cx = width / 2f;
        float cy = height / 2f;

        canvas.drawCircle(cx, cy, radius, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }
}
