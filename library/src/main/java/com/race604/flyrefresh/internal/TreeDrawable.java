package com.race604.flyrefresh.internal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.FloatMath;
import android.view.animation.Interpolator;

/**
 * Created by jing on 15-5-22.
 */
public class TreeDrawable extends Drawable {

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 200;
    private float mSizeFactor = 1;
    private float mBendScale = 0f;

    private Paint mTrunkPaint = new Paint();
    private Paint mBranchPaint = new Paint();
    private Path mTrunk = new Path();
    private Path mBranch = new Path();

    private Path mBaseLine = new Path();
    private Paint mBaseLinePaint = new Paint();

    public TreeDrawable() {
        super();

        mTrunkPaint.setAntiAlias(true);
        mTrunkPaint.setStrokeWidth(0.1f);
        mTrunkPaint.setColor(Color.DKGRAY);

        mBranchPaint.setAntiAlias(true);
        mBranchPaint.setColor(Color.GREEN);

        mBaseLinePaint.setAntiAlias(true);
        mBaseLinePaint.setColor(Color.RED);
        mBaseLinePaint.setStrokeWidth(0.5f);
        mBaseLinePaint.setStyle(Paint.Style.STROKE);
        updateTree();
    }

    public void setBendScale(float scale) {
        mBendScale = scale;
        updateTree();
    }

    public void updateTree() {

        final Interpolator interpolator = PathInterpolatorCompat.create(0.8f, -0.6f * mBendScale);

        final float width = DEFAULT_WIDTH * mSizeFactor;
        final float height = DEFAULT_HEIGHT * mSizeFactor;

        final float maxMove = width * 0.45f * mBendScale;
        final float trunkSize = width * 0.05f;
        final float branchSize = width * 0.2f;
        final float x0 = width / 2;
        final float y0 = height;

        mBaseLine.reset();
        mBaseLine.moveTo(x0, y0);
        final int N = 50;
        final float dp = 1f / N;
        final float dy = -dp * height;
        float y = y0;
        float p = 0;
        float[] xx = new float[N + 1];
        float[] yy = new float[N + 1];
        for (int i = 0; i <= N; i++) {
            xx[i] = interpolator.getInterpolation(p) * maxMove + x0;
            yy[i] = y;
            mBaseLine.lineTo(xx[i], yy[i]);

            y += dy;
            p += dp;
        }

        mTrunk.reset();
        mTrunk.moveTo(x0 - trunkSize, y0);
        int max = (int) (N * 0.6f);
        int max1 = (int) (max * 0.6f);
        float diff = max - max1;
        for (int i = 0; i < max; i++) {
            if (i < max1) {
                mTrunk.lineTo(xx[i] - trunkSize, yy[i]);
            } else {
                mTrunk.lineTo(xx[i] - trunkSize * (max - i) / diff, yy[i]);
            }
        }

        for (int i = max - 1; i >= 0; i--) {
            if (i < max1) {
                mTrunk.lineTo(xx[i] + trunkSize, yy[i]);
            } else {
                mTrunk.lineTo(xx[i] + trunkSize * (max - i) / diff, yy[i]);
            }
        }
        mTrunk.close();

        mBranch.reset();
        int min = (int) (N * 0.4f);
        diff = N - min;

        mBranch.addArc(new RectF(xx[min] - branchSize, yy[min] - branchSize, xx[min] + branchSize, yy[min] + branchSize), 0f, 180f);
        for (int i = min; i <= N; i++) {
            mBranch.lineTo(xx[i] + branchSize - ((i - min) / diff) * branchSize, yy[i]);
        }
        for (int i = N; i >= min; i--) {
            mBranch.lineTo(xx[i] - branchSize + ((i - min) / diff) * branchSize, yy[i]);
        }

        mBranch.close();
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) (DEFAULT_WIDTH * mSizeFactor);
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) (DEFAULT_HEIGHT * mSizeFactor);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(mBranch, mBranchPaint);
        canvas.drawPath(mTrunk, mTrunkPaint);

        canvas.drawPath(mBaseLine, mBaseLinePaint);
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

    private static float getQuadPoint(float x0, float y0, float x1, float y1,
                                      float x2, float y2, float x) {
        float t;
        float a = (x0 - 2 * x1 + x2);
        float b = 2 * (x1 - x0);
        float c = x1 - x;
        if (a < 1e-10) {
            t = (x1 - x) / (2 * (x1 - x0));
        } else {
            t = (FloatMath.sqrt(b * b - 4 * a * c) + b) / (2 * a);
        }

        float t1 = 1 - t;
        return t1 * t1 * y0 + 2 * t * t1 * y1 + t * t * y2;
    }

}
