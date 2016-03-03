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
    private boolean mIsInTouch = false;
    private float mScroll = 0;
    private int mMaxScroll = 0;
    private int mMinScroll = 0;

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

        mScroll = 0;
        mMaxScroll = mHeight - mMinHegiht;
        mMinScroll = mHeight - mMaxHegiht;
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

    public int getScroll() {
        return (int) mScroll;
    }

    public int getMaxScroll() {
        return mMaxScroll;
    }

    public int getMinScroll() {
        return mMinScroll;
    }

    public int getCurPosition() {
        return (int) (mHeight - mScroll);
    }

    public boolean isInTouch() {
        return mIsInTouch;
    }

    /**
     * Check if can scroll down to show top
     * @return
     */
    public boolean canScrollDown() {
        return mScroll > mMinScroll;
    }

    /**
     * Check if can scroll up to show bottom
     * @return
     */
    public boolean canScrollUp() {
        return mScroll < mMaxScroll;
    }

    public int move(float deltaY) {
        float willTo;
        float consumed = deltaY;
        if (mScroll >= 0) {
            willTo = mScroll + deltaY;
            if (willTo < 0) {
                willTo = willTo * mResistance;
                if (willTo < mMinScroll) {
                    consumed -= (willTo - mMinScroll) / mResistance;
                    willTo = mMinScroll;
                }
            } else if (willTo > mMaxScroll) {
                consumed -= willTo - mMaxScroll;
                willTo = mMaxScroll;
            }
        } else {
            willTo = mScroll + deltaY * mResistance;
            if (willTo > 0) {
                willTo = willTo / mResistance;
                if (willTo > mMaxScroll) {
                    consumed -= willTo - mMaxScroll;
                    willTo = mMaxScroll;
                }
            } else if (willTo < mMinScroll) {
                consumed -= willTo - mMinScroll;
                willTo = mMinScroll;
            }
        }

        mScroll = willTo;
        return (int) consumed;
    }

    public boolean isOverHeight() {
        return mScroll < 0;
    }

    public float getMovePercentage() {
        return -mScroll / mOverDistance;
    }

    public boolean needSendRefresh() {
        return getMovePercentage() > 0.9f;
    }
}
