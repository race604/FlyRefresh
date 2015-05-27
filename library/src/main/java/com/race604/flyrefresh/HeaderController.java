package com.race604.flyrefresh;

/**
 * Created by jing on 15-5-19.
 */
public class HeaderController {

    private int mHeight;
    private int mMaxHegiht;
    private int mMinHegiht;
    private float mOverDistance;

    private float mResistance = 0.5f;
    private float mStartX;
    private float mStartY;
    private int mOffsetX;
    private int mOffsetY;
    private boolean mIsInTouch = false;
    private int mStartPos = -1;
    private int mCurrentPos = -1;

    public HeaderController(int height, int maxHeight, int minHeight) {

        if (maxHeight <= 0) {
            throw new IllegalArgumentException("maxHeight must > 0");
        }

        setSize(height, maxHeight, minHeight);
    }

    public void setSize(int height, int maxHeight, int minHeight) {
        mHeight = Math.max(0, height);
        mMaxHegiht = Math.max(0, maxHeight);
        mMinHegiht = Math.max(0, minHeight);
        mOverDistance = mMaxHegiht - mHeight;

        mCurrentPos = mStartPos = mHeight;
    }

    public int getMaxHeight() {
        return mMaxHegiht;
    }

    public int getMinHeight() {
        return mMinHegiht;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getCurrentPos() {
        return mCurrentPos;
    }

    public int getOffsetX() {
        return mOffsetX;
    }

    public int getOffsetY() {
        return mOffsetY;
    }

    public boolean isInTouch() {
        return mIsInTouch;
    }

    public boolean hasMoved() {
        return mStartPos != mCurrentPos;
    }
    public boolean canMoveUp() {
        return mCurrentPos > mMinHegiht;
    }

    public boolean canMoveDown() {
        return mCurrentPos < mMaxHegiht;
    }

    public void onTouchRelease() {
        mIsInTouch = false;
    }

    public void onTouchDown(float x, float y) {
        mIsInTouch = true;
        mStartX = x;
        mStartY = y;
        mOffsetX = mOffsetY = 0;
        startMove();
    }

    public void onTouchMove(float x, float y) {
        mOffsetX = (int) (x - mStartX);
        mOffsetY = (int) (y - mStartY);
    }

    public void startMove() {
        mStartPos = mCurrentPos;
    }

    public int willMove(float deltaY) {
        float willTo;
        if (mStartPos > mHeight) {
            willTo = mStartPos + deltaY * mResistance;
            if (willTo < mHeight) {
                willTo = mHeight + (willTo - mHeight) / mResistance;
            }
        } else {
            willTo = mStartPos + deltaY;
            if (willTo > mHeight) {
                willTo = mHeight + (willTo - mHeight) * mResistance;
            }
        }

        int offsetY = Math.max(mMinHegiht, Math.min(mMaxHegiht, (int)willTo));
        int move = offsetY - mCurrentPos;
        mCurrentPos = offsetY;
        return move;
    }

    public int moveTo(float pos) {
        int offsetY = Math.max(mMinHegiht, Math.min(mMaxHegiht, (int)pos));
        int delta = offsetY - mCurrentPos;
        mCurrentPos = offsetY;
        return delta;
    }

    public boolean isOverHeight() {
        return mCurrentPos > mHeight;
    }

    public float getMovePercentage() {
        if (mCurrentPos >= mHeight) {
            return (mCurrentPos - mHeight) / mOverDistance;
        } else {
            return (mCurrentPos - mHeight) / mOverDistance;
        }
    }

    public boolean needSendRefresh() {
        return getMovePercentage() > 0.9f;
    }
}
