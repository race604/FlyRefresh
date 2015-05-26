package com.race604.flyrefresh.internal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.FloatMath;
import android.view.animation.Interpolator;

/**
 * Created by Jing on 15/5/24.
 */
public class MountSenceDrawable extends Drawable {

    private static final int COLOR_BACKGROUND = Color.argb(255, 126, 206, 201);
    private static final int COLOR_MOUNTAIN_1 = Color.argb(255, 134, 218, 215);
    private static final int COLOR_MOUNTAIN_2 = Color.argb(255, 60, 146, 156);
    private static final int COLOR_MOUNTAIN_3 = Color.argb(255, 62, 95, 115);
    private static final int COLOR_TREE_1_BRANCH = Color.parseColor("#1F7177");
    private static final int COLOR_TREE_1_BTRUNK = Color.parseColor("#0C3E48");
    private static final int COLOR_TREE_2_BRANCH = Color.parseColor("#34888F");
    private static final int COLOR_TREE_2_BTRUNK = Color.parseColor("#1B6169");
    private static final int COLOR_TREE_3_BRANCH = Color.parseColor("#57B1AE");
    private static final int COLOR_TREE_3_BTRUNK = Color.parseColor("#62A4AD");

    private static final int WIDTH = 240;
    private static final int HEIGHT = 180;

    private static final int TREE_WIDTH = 100;
    private static final int TREE_HEIGHT = 200;

    private Paint mMountPaint = new Paint();
    private Paint mTrunkPaint = new Paint();
    private Paint mBranchPaint = new Paint();
    private Paint mBoarderPaint = new Paint();

    private Path mMount1 = new Path();
    private Path mMount2 = new Path();
    private Path mMount3 = new Path();
    private Path mTrunk = new Path();
    private Path mBranch = new Path();

    private float mScale = 5f;
    private float mMoveFactor = 0;
    private Matrix mTransMatrix = new Matrix();

    public MountSenceDrawable() {
        super();

        mMountPaint.setAntiAlias(true);
        mMountPaint.setStyle(Paint.Style.FILL);

        mTrunkPaint.setAntiAlias(true);
        mBranchPaint.setAntiAlias(true);
        mBoarderPaint.setAntiAlias(true);
        mBoarderPaint.setStyle(Paint.Style.STROKE);
        mBoarderPaint.setStrokeWidth(2);
        mBoarderPaint.setStrokeJoin(Paint.Join.ROUND);

        updateMountainPath();
        updateTreePath();
    }

    private void updateMountainPath() {

        mTransMatrix.reset();
        mTransMatrix.setScale(mScale, mScale);

        int offset1 = (int) (10 * mMoveFactor);
        mMount1.reset();
        mMount1.moveTo(0, 95 + offset1);
        mMount1.lineTo(55, 74 + offset1);
        mMount1.lineTo(146, 104 + offset1);
        mMount1.lineTo(227, 72 + offset1);
        mMount1.lineTo(WIDTH, 80 + offset1);
        mMount1.lineTo(WIDTH, HEIGHT);
        mMount1.lineTo(0, HEIGHT);
        mMount1.close();
        mMount1.transform(mTransMatrix);

        int offset2 = (int) (20 * mMoveFactor);
        mMount2.reset();
        mMount2.moveTo(0, 103 + offset2);
        mMount2.lineTo(67, 90 + offset2);
        mMount2.lineTo(165, 115 + offset2);
        mMount2.lineTo(221, 87 + offset2);
        mMount2.lineTo(WIDTH, 100 + offset2);
        mMount2.lineTo(WIDTH, HEIGHT);
        mMount2.lineTo(0, HEIGHT);
        mMount2.close();
        mMount2.transform(mTransMatrix);

        int offset3 = (int) (30 * mMoveFactor);
        mMount3.reset();
        mMount3.moveTo(0, 114 + offset3);
        mMount3.cubicTo(30, 106 + offset3, 196, 97 + offset3, WIDTH, 104 + offset3);
        mMount3.lineTo(WIDTH, HEIGHT);
        mMount3.lineTo(0, HEIGHT);
        mMount3.close();
        mMount3.transform(mTransMatrix);
    }

    private void updateTreePath() {
        final Interpolator interpolator = PathInterpolatorCompat.create(0.8f, -0.5f * mMoveFactor);

        final float width = TREE_WIDTH;
        final float height = TREE_HEIGHT;

        final float maxMove = width * 0.3f * mMoveFactor;
        final float trunkSize = width * 0.05f;
        final float branchSize = width * 0.2f;
        final float x0 = width / 2;
        final float y0 = height;

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

            y += dy;
            p += dp;
        }

        mTrunk.reset();
        mTrunk.moveTo(x0 - trunkSize, y0);
        int max = (int) (N * 0.7f);
        int max1 = (int) (max * 0.5f);
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

        mBranch.moveTo(xx[min] - branchSize, yy[min]);
        mBranch.addArc(new RectF(xx[min] - branchSize, yy[min] - branchSize, xx[min] + branchSize, yy[min] + branchSize), 0f, 180f);
        for (int i = min; i <= N; i++) {
            float f = (i - min) / diff;
            mBranch.lineTo(xx[i] - branchSize + f * f * branchSize, yy[i]);
        }
        for (int i = N; i >= min; i--) {
            float f = (i - min) / diff;
            mBranch.lineTo(xx[i] + branchSize - f * f * branchSize, yy[i]);
        }

    }

    public void setMoveFactor(float factor) {
        mMoveFactor = factor;

        updateMountainPath();
        updateTreePath();
    }

    private void drawTree(Canvas canvas, float scale, float baseX, float baseY,
                          int colorTrunk, int colorBranch) {
        canvas.save();

        final float dx = baseX - TREE_WIDTH * scale / 2;
        final float dy = baseY - TREE_HEIGHT * scale;
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);

        mBranchPaint.setColor(colorBranch);
        canvas.drawPath(mBranch, mBranchPaint);
        mTrunkPaint.setColor(colorTrunk);
        canvas.drawPath(mTrunk, mTrunkPaint);
        mBoarderPaint.setColor(colorTrunk);
        canvas.drawPath(mBranch, mBoarderPaint);

        canvas.restore();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(COLOR_BACKGROUND);

        mMountPaint.setColor(COLOR_MOUNTAIN_1);
        canvas.drawPath(mMount1, mMountPaint);

        drawTree(canvas, 0.12f * mScale, 55 * mScale, (95 + 20 * mMoveFactor) * mScale,
                COLOR_TREE_3_BTRUNK, COLOR_TREE_3_BRANCH);
        drawTree(canvas, 0.1f * mScale, 35 * mScale, (100 + 20 * mMoveFactor) * mScale,
                COLOR_TREE_3_BTRUNK, COLOR_TREE_3_BRANCH);
        mMountPaint.setColor(COLOR_MOUNTAIN_2);
        canvas.drawPath(mMount2, mMountPaint);

        drawTree(canvas, 0.2f * mScale, 160 * mScale, (110 + 30 * mMoveFactor) * mScale,
                COLOR_TREE_1_BTRUNK, COLOR_TREE_1_BRANCH);

        drawTree(canvas, 0.14f * mScale, 180 * mScale, (110 + 30 * mMoveFactor) * mScale,
                COLOR_TREE_2_BTRUNK ,COLOR_TREE_2_BRANCH);

        drawTree(canvas, 0.16f * mScale, 140 * mScale, (110 + 30 * mMoveFactor) * mScale,
                COLOR_TREE_2_BTRUNK ,COLOR_TREE_2_BRANCH);

        mMountPaint.setColor(COLOR_MOUNTAIN_3);
        canvas.drawPath(mMount3, mMountPaint);

    }

    @Override
    public void setAlpha(int alpha) {
        mMountPaint.setAlpha(alpha);
        mTrunkPaint.setAlpha(alpha);
        mBranchPaint.setAlpha(alpha);
        mBoarderPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mMountPaint.setColorFilter(cf);
        mTrunkPaint.setColorFilter(cf);
        mBranchPaint.setColorFilter(cf);
        mBoarderPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) (HEIGHT * mScale);
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) (WIDTH * mScale);
    }
}
