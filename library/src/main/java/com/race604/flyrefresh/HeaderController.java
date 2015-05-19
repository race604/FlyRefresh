package com.race604.flyrefresh;

/**
 * Created by jing on 15-5-19.
 */
public class HeaderController {

    private int mHeight;
    private int mMaxHegiht;
    private int mMinHegiht;

    private int mOffsetX;
    private int mOffsetY;
    private boolean mIsInTouch = false;
    private int mTouchDownPos = -1;
    private int mTouchPos = -1;

    public HeaderController(int height, int maxHeight, int minHeight) {

        if (mMaxHegiht <= 0) {
            throw new IllegalArgumentException("maxHeight must > 0");
        }

        mHeight = Math.max(0, height);
        mMaxHegiht = Math.max(0, maxHeight);
        mMinHegiht = Math.max(0, minHeight);

        mOffsetY = mHeight;
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
        return mTouchDownPos != mTouchPos;
    }

    public void onTouchRelease() {

    }

    public void onTouchDown(float x, float y) {

    }

    public void onTouchMove(float x, float y) {

    }

}
