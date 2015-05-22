package com.race604.flyrefresh;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.race604.flyrefresh.internal.ElasticOutInterpolator;
import com.race604.utils.UIUtils;

/**
 * Created by Jing on 15/5/18.
 */
public class FlyRefreshLayout extends ViewGroup {

    private static final String TAG = FlyRefreshLayout.class.getCanonicalName();
    private static final boolean D = true;

    private final static int ACTION_BUTTON_CENTER = UIUtils.dpToPx(48);
    private final static int ACTION_ICON_SIZE = UIUtils.dpToPx(32);
    private final static int DEFAULT_EXPAND = UIUtils.dpToPx(40);
    private final static int DEFAULT_HEIGHT = 0;

    private int mHeaderHeight = DEFAULT_HEIGHT;
    private int mHeaderExpandHeight = DEFAULT_EXPAND;
    private int mHeaderShrinkHeight = DEFAULT_HEIGHT;
    private int mHeaderId = 0;
    private int mContentId = 0;

    private int mPagingTouchSlop;

    private MotionEvent mDownEvent;
    private MotionEvent mLastMoveEvent;
    private boolean mHasSendCancelEvent = false;
    private boolean mPreventForHorizontal = false;
    private boolean mDisableWhenHorizontalMove = false;

    private Drawable mActionDrawable;
    private FloatingActionButton mActionView;
    private ImageView mFlyView;
    private View mHeaderView;
    protected View mContent;
    protected HeaderController mHeaderController;
    private IScrollHandler mScrollHandler;

    private ScrollChecker mScrollChecker;
    private VelocityTracker mVelocityTracker;
    private ValueAnimator mBounceAnim;
    private int mMaxVelocity;
    private boolean mIsFlying = false;

    public FlyRefreshLayout(Context context) {
        super(context);
        init(context, null);
    }

    public FlyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FlyRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlyRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.FlyRefreshLayout);
            mHeaderHeight = arr.getDimensionPixelOffset(R.styleable.FlyRefreshLayout_frv_header_height,
                    DEFAULT_HEIGHT);
            mHeaderExpandHeight = arr.getDimensionPixelOffset(R.styleable.FlyRefreshLayout_frv_header_expand_height,
                    DEFAULT_EXPAND);
            mHeaderShrinkHeight = arr.getDimensionPixelOffset(R.styleable.FlyRefreshLayout_frv_header_shrink_height,
                    DEFAULT_HEIGHT);

            mHeaderId = arr.getResourceId(R.styleable.FlyRefreshLayout_frv_header, mHeaderId);
            mContentId = arr.getResourceId(R.styleable.FlyRefreshLayout_frv_content, mContentId);

            mActionDrawable = arr.getDrawable(R.styleable.FlyRefreshLayout_frv_action);

            arr.recycle();
        }

        mHeaderController = new HeaderController(mHeaderHeight, mHeaderExpandHeight, mHeaderShrinkHeight);
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
        mScrollHandler = new DefalutScrollHandler();

        mScrollChecker = new ScrollChecker();
        mMaxVelocity = conf.getScaledMaximumFlingVelocity();
    }

    public void setScrollHandler(IScrollHandler handler) {
        mScrollHandler = handler;
    }

    @Override
    protected void onFinishInflate() {
        final int childCount = getChildCount();
        if (childCount > 2) {
            throw new IllegalStateException("FlyRefreshLayout only can host 2 elements");
        } else if (childCount == 2) {
            if (mHeaderId != 0 && mHeaderView == null) {
                mHeaderView = findViewById(mHeaderId);
            }

            if (mContentId != 0 && mContent == null) {
                mContent = findViewById(mContentId);
            }

            // not specify header or content
            if (mContent == null || mHeaderView == null) {

                View child1 = getChildAt(0);
                View child2 = getChildAt(1);
                if (child1 instanceof IFlyPullable) {
                    mHeaderView = child1;
                    mContent = child2;
                } else if (child2 instanceof IFlyPullable) {
                    mHeaderView = child2;
                    mContent = child1;
                } else {
                    // both are not specified
                    if (mContent == null && mHeaderView == null) {
                        mHeaderView = child1;
                        mContent = child2;
                    }
                    // only one is specified
                    else {
                        if (mHeaderView == null) {
                            mHeaderView = mContent == child1 ? child2 : child1;
                        } else {
                            mContent = mHeaderView == child1 ? child2 : child1;
                        }
                    }
                }
            }
        } else if (childCount == 1) {
            mContent = getChildAt(0);
        }

        if (mActionDrawable != null) {
            final int bgColor = UIUtils.getThemeColorFromAttrOrRes(getContext(), R.attr.colorAccent, R.color.accent);
            final int pressedColor = UIUtils.darkerColor(bgColor, 0.8f);
            mActionView = new FloatingActionButton(getContext());
            //mActionView.setIconDrawable(mActionDrawable);
            mActionView.setColorNormal(bgColor);
            mActionView.setColorPressed(pressedColor);

            addView(mActionView);

            mFlyView = new ImageView(getContext());
            mFlyView.setImageDrawable(mActionDrawable);
            mFlyView.setScaleType(ImageView.ScaleType.FIT_XY);
            final int iconSize = UIUtils.dpToPx(32);
            addView(mFlyView, new LayoutParams(iconSize, iconSize));
        }

        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mContent != null) {
            measureChildWithMargins(mContent, widthMeasureSpec, 0, heightMeasureSpec, mHeaderShrinkHeight);
        }

        if (mActionView != null) {
            measureChild(mActionView, widthMeasureSpec, heightMeasureSpec);
            measureChild(mFlyView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren();
    }

    private void layoutChildren() {
        int offsetY = mHeaderController.getCurrentPos();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + offsetY - mHeaderHeight;
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left, top, right, bottom);
        }

        if (mContent != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + offsetY;
            final int right = left + mContent.getMeasuredWidth();
            final int bottom = top + mContent.getMeasuredHeight();
            mContent.layout(left, top, right, bottom);
        }

        if (mActionView != null) {
            final int center = ACTION_BUTTON_CENTER;
            int halfWidth = (mActionView.getMeasuredWidth() + 1) / 2;
            int halfHeight = (mActionView.getMeasuredHeight() + 1) / 2;

            final int adjustCenter = UIUtils.dpToPx(2);
            mActionView.layout(center - halfWidth , offsetY - halfHeight + adjustCenter,
                    center + halfWidth, offsetY + halfHeight + adjustCenter);

            halfWidth = (mFlyView.getMeasuredWidth() + 1) / 2;
            halfHeight = (mFlyView.getMeasuredHeight() + 1) / 2;
            mFlyView.layout(center - halfWidth, offsetY - halfHeight,
                    center + halfWidth, offsetY + halfHeight);
        }

    }

    private void sendCancelEvent() {
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(),
                last.getEventTime() + ViewConfiguration.getLongPressTimeout(),
                MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private float getInitVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
            return mVelocityTracker.getYVelocity();
        }
        return 0;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mContent == null || mHeaderView == null) {
            return super.dispatchTouchEvent(ev);
        }

        obtainVelocityTracker(ev);
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final float initVelocity = getInitVelocity();
                releaseVelocityTracker();

                if (mHeaderController.isInTouch()) {
                    mHeaderController.onTouchRelease();
                    onRelease((int) initVelocity);
                    if (mHeaderController.hasMoved()) {
                        sendCancelEvent();
                        return true;
                    }
                    return super.dispatchTouchEvent(ev);
                } else {
                    return super.dispatchTouchEvent(ev);
                }
            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                mDownEvent = ev;
                mHeaderController.onTouchDown(ev.getX(), ev.getY());

                if (mBounceAnim != null && mBounceAnim.isRunning()) {
                    mBounceAnim.cancel();
                }
                mScrollChecker.abortIfWorking();

                mPreventForHorizontal = false;
                super.dispatchTouchEvent(ev);

            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = ev;
                mHeaderController.onTouchMove(ev.getX(), ev.getY());

                float offsetX = mHeaderController.getOffsetX();
                float offsetY = mHeaderController.getOffsetY();

                if (mDisableWhenHorizontalMove && !mPreventForHorizontal
                        && (Math.abs(offsetX) > mPagingTouchSlop
                        && Math.abs(offsetX) > Math.abs(offsetY))) {
                    mPreventForHorizontal = true;
                }
                if (mPreventForHorizontal) {
                    return super.dispatchTouchEvent(ev);
                }

                boolean moveDown = offsetY > 0;

                if (moveDown) {
                    if (mContent != null && mScrollHandler.canScrollUp(mContent)) {
                        if (mHeaderController.isInTouch()) {
                            mHeaderController.onTouchRelease();
                            sendDownEvent();
                        }
                        return super.dispatchTouchEvent(ev);
                    } else {
                        if (!mHeaderController.isInTouch()) {
                            mHeaderController.onTouchDown(ev.getX(), ev.getY());
                            offsetY = mHeaderController.getOffsetY();
                        }
                        willMovePos(offsetY);
                        return true;
                    }
                } else {
                    if (mHeaderController.canMoveUp()) {
                        willMovePos(offsetY);
                        return true;
                    } else {
                        if (mHeaderController.isInTouch()) {
                            mHeaderController.onTouchRelease();
                            sendDownEvent();
                        }
                        return super.dispatchTouchEvent(ev);
                    }
                }

        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * if deltaY > 0, move the content down
     *
     * @param deltaY
     */
    private void willMovePos(float deltaY) {

        // has reached the top
        int delta = mHeaderController.willMove(deltaY);
        //Log.d(TAG, String.format("willMovePos deltaY = %s, delta = %d", deltaY, delta));

        if (delta == 0) {
            return;
        }

        if (!mHasSendCancelEvent && mHeaderController.isInTouch()) {
            sendCancelEvent();
        }

        movePos(delta);
    }

    private void movePos(float delta) {
        if (mContent != null) {
            mContent.offsetTopAndBottom((int) delta);
        }

        if (mActionView != null) {
            mActionView.offsetTopAndBottom((int) delta);

            mFlyView.offsetTopAndBottom((int) delta);

            if (mHeaderController.isOverHeight()) {
                float percentage = mHeaderController.getOverPercentage();
                mFlyView.setRotation((-45) * percentage);
            }
        }

    }

    private void onRelease(int velocity) {
        mScrollChecker.tryToScrollTo(velocity);
    }

    private void onScrollFinish() {
        if (mHeaderController.isOverHeight()) {
            mBounceAnim = ObjectAnimator.ofFloat(mHeaderController.getCurrentPos(), mHeaderController.getHeight());
            mBounceAnim.setInterpolator(new ElasticOutInterpolator());
            mBounceAnim.setDuration(500);
            mBounceAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    movePos(mHeaderController.moveTo(value));
                }
            });
            mBounceAnim.start();

            if (mHeaderController.needSendRefresh()) {
                sendFlyAnimation();
            }
        }
    }

    private void resetViewState(View view) {
        view.clearAnimation();
        view.clearAnimation();
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setRotation(0);
        view.setRotationX(0);
        view.setRotationY(0);
    }

    private void sendFlyAnimation() {

        resetViewState(mFlyView);

        AnimatorSet flyUpAnim = new AnimatorSet();
        flyUpAnim.setDuration(1000);

        ObjectAnimator transX = ObjectAnimator.ofFloat(mFlyView, "translationX", 0, getWidth());

        ObjectAnimator transY = ObjectAnimator.ofFloat(mFlyView, "translationY", 0, -mHeaderController.getHeight());
        transY.setInterpolator(PathInterpolatorCompat.create(0.1f, 0.9f));

        ObjectAnimator rotation = ObjectAnimator.ofFloat(mFlyView, "rotation", mFlyView.getRotation(), 0);
        rotation.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator rotationX = ObjectAnimator.ofFloat(mFlyView, "rotationX", 0, 60);
        rotationX.setInterpolator(new DecelerateInterpolator());

        flyUpAnim.playTogether(transX, transY, rotationX,
                ObjectAnimator.ofFloat(mFlyView, "scaleX", 1, 0.5f),
                ObjectAnimator.ofFloat(mFlyView, "scaleY", 1, 0.5f),
                rotation
        );

        final int offDistX = -ACTION_ICON_SIZE/2-ACTION_BUTTON_CENTER;
        AnimatorSet flyDownAnim = new AnimatorSet();
        flyDownAnim.setDuration(1000);
        ObjectAnimator transX1 = ObjectAnimator.ofFloat(mFlyView, "translationX", getWidth(), offDistX);
        ObjectAnimator transY1 = ObjectAnimator.ofFloat(mFlyView, "translationY", -mHeaderController.getHeight(), 0);
        transY1.setInterpolator(PathInterpolatorCompat.create(0.1f, 1f));
        ObjectAnimator rotation1 = ObjectAnimator.ofFloat(mFlyView, "rotation", mFlyView.getRotation(), 0);
        rotation1.setInterpolator(new AccelerateInterpolator());
        flyDownAnim.playTogether(transX1, transY1,
                ObjectAnimator.ofFloat(mFlyView, "scaleX", 0.5f, 0.9f),
                ObjectAnimator.ofFloat(mFlyView, "scaleY", 0.5f, 0.9f),
                rotation1
        );
        flyDownAnim.setStartDelay(400);
        flyDownAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFlyView.setRotationY(180);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        AnimatorSet flyInAnim = new AnimatorSet();
        flyInAnim.setDuration(500);
        flyInAnim.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator tranX2 = ObjectAnimator.ofFloat(mFlyView, "translationX", offDistX, 0);
        ObjectAnimator rotationX2 = ObjectAnimator.ofFloat(mFlyView, "rotationX", 30, 0);
        flyInAnim.playTogether(tranX2, rotationX2,
                ObjectAnimator.ofFloat(mFlyView, "scaleX", 0.9f, 1f),
                ObjectAnimator.ofFloat(mFlyView, "scaleY", 0.9f, 1f));
        flyInAnim.setStartDelay(100);
        flyInAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mFlyView.setRotationY(0);
            }
        });

        AnimatorSet flyAnim = new AnimatorSet();
        flyAnim.playSequentially(flyUpAnim, flyDownAnim, flyInAnim);
        flyAnim.start();
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    class ScrollChecker implements Runnable {

        private Scroller mScroller;
        private boolean mIsRunning = false;
        private int mStart;

        public ScrollChecker() {
            mScroller = new Scroller(getContext());
        }

        @Override
        public void run() {
            boolean finish = !mScroller.computeScrollOffset();
            int curY = mScroller.getCurrY();
            int deltaY = mHeaderController.moveTo(curY);
            //Log.d(TAG, String.format("Scroller: currY = %d, deltaY = %d", curY, deltaY));

            if (!finish) {
                movePos(deltaY);
                postDelayed(this, 10);
            } else {
                finish();
            }
        }

        private void finish() {
            reset();
            onScrollFinish();
        }

        private void reset() {
            mIsRunning = false;
            removeCallbacks(this);
        }

        public void abortIfWorking() {
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                //onPtrScrollAbort();
                reset();
            }
        }

        public void tryToScrollTo(int velocity) {
            mStart = mHeaderController.getCurrentPos();
            removeCallbacks(this);

            // fix #47: Scroller should be reused, https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh/issues/47
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mHeaderController.startMove();

            mScroller.setFriction(ViewConfiguration.getScrollFriction() * 4);
            mScroller.fling(0, mStart, 0, velocity, 0, 0, mHeaderController.getMinHeight(),
                    mHeaderController.getMaxHeight());

            //Log.d(TAG, String.format("Scroller: velocity = %d, duration = %d", velocity, mScroller.getDuration()));

            post(this);
            mIsRunning = true;
        }
    }

    private static class SimpleAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
