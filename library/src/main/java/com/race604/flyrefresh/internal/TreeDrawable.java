package com.race604.flyrefresh.internal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

/**
 * Created by jing on 15-5-22.
 */
public class TreeDrawable extends Drawable {

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 200;
    private float mSizeFactor = 1;

    private Paint mTrunkPaint = new Paint();
    private Paint mBranchPaint = new Paint();
    private Path mTrunk = new Path();
    private Path mBranch = new Path();

    public TreeDrawable() {
        super();

        mTrunkPaint.setAntiAlias(true);
        mTrunkPaint.setStrokeWidth(0.1f);
        mTrunkPaint.setColor(Color.DKGRAY);

        mBranchPaint.setAntiAlias(true);
        mBranchPaint.setColor(Color.GREEN);

        updateTree();
    }

    public void updateTree() {
        final float level = getLevel() / 1000f;
        float width = DEFAULT_WIDTH * mSizeFactor;
        float height = DEFAULT_HEIGHT * mSizeFactor;

        float trunkWidth = width * 0.1f;
        float trunkHeight = height * 0.6f;
        float trunkMove = 25 * mSizeFactor * level;
        float midLevel = -trunkMove * 0.5f;

        float startX = (width - trunkWidth) / 2;
        float startY = height;
        float midX = startX + midLevel;
        float midY = startY - trunkHeight * 0.8f;
        float endX = width/2 + trunkMove;
        float endY = startY - trunkHeight;
        mTrunk.reset();
        mTrunk.moveTo(startX, startY);
        mTrunk.quadTo(midX, midY, endX, endY);
        mTrunk.quadTo(midX + trunkWidth, midY, startX + trunkWidth, startY);
        mTrunk.close();

        float branchWidth = width * 0.4f;
        float branchHeight = width * 0.7f;
        float branchMove = 45 * mSizeFactor * level;
        float topX = width / 2 + branchMove;

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
        canvas.drawPath(mTrunk, mTrunkPaint);
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
}
