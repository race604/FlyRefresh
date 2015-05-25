package com.race604.flyrefresh.internal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

/**
 * Created by Jing on 15/5/24.
 */
public class MountSenceDrawable extends Drawable {

    private static final int COLOR_BACKGROUND = Color.argb(255, 126, 206, 201);
    private static final int COLOR_MOUNTAIN_1 = Color.argb(255, 134, 218, 215);
    private static final int COLOR_MOUNTAIN_2 = Color.argb(255, 60, 146, 156);
    private static final int COLOR_MOUNTAIN_3 = Color.argb(255, 62, 95, 115);

    private static final int WIDTH = 226;
    private static final int HEIGHT = 167;

    private Paint mMountPaint = new Paint();

    private Path mMount1 = new Path();
    private Path mMount2 = new Path();
    private Path mMount3 = new Path();

    private float mMoveFactor = 0;

    public MountSenceDrawable() {
        super();

        mMountPaint.setAntiAlias(true);
        mMountPaint.setStyle(Paint.Style.FILL);
        updateMountainPath();
    }

    private void updateMountainPath() {

        int offset1 = (int) (7 * mMoveFactor);
        mMount1.reset();
        mMount1.moveTo(0, 89 + offset1);
        mMount1.lineTo(52, 69 + offset1);
        mMount1.lineTo(138, 97 + offset1);
        mMount1.lineTo(214, 67 + offset1);
        mMount1.lineTo(WIDTH, 75 + offset1);
        mMount1.lineTo(WIDTH, HEIGHT);
        mMount1.lineTo(0, HEIGHT);
        mMount1.close();

        int offset2 = (int) (13 * mMoveFactor);
        mMount2.reset();
        mMount2.moveTo(0, 96 + offset2);
        mMount2.lineTo(64, 84 + offset2);
        mMount2.lineTo(165, 113 + offset2);
        mMount2.lineTo(209, 81 + offset2);
        mMount2.lineTo(WIDTH, 93 + offset2);
        mMount2.lineTo(WIDTH, HEIGHT);
        mMount2.lineTo(0, HEIGHT);
        mMount2.close();

        int offset3 = (int) (23 * mMoveFactor);
        mMount3.reset();
        mMount3.moveTo(0, 106 + offset3);
        mMount3.cubicTo(29, 99 + offset3, 185, 90 + offset3, WIDTH, 97 + offset3);
        mMount3.lineTo(WIDTH, HEIGHT);
        mMount3.lineTo(0, HEIGHT);
        mMount3.close();
    }

    public void setMoveFactor(float factor) {
        mMoveFactor = factor;

        updateMountainPath();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(COLOR_BACKGROUND);

        mMountPaint.setColor(COLOR_MOUNTAIN_1);
        canvas.drawPath(mMount1, mMountPaint);

        mMountPaint.setColor(COLOR_MOUNTAIN_2);
        canvas.drawPath(mMount2, mMountPaint);

        mMountPaint.setColor(COLOR_MOUNTAIN_3);
        canvas.drawPath(mMount3, mMountPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public int getIntrinsicHeight() {
        return HEIGHT;
    }

    @Override
    public int getIntrinsicWidth() {
        return WIDTH;
    }
}
