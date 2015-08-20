package com.race604.flyrefresh;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import com.race604.flyrefresh.internal.ElasticOutInterpolator;
import com.race604.flyrefresh.internal.SimpleAnimatorListener;
import com.race604.utils.UIUtils;

/**
 * Created by Jing on 15/5/18.
 */
public class PullHeaderLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private static final String TAG = PullHeaderLayout.class.getCanonicalName();
    private static final boolean D = true;

    public static final int STATE_IDLE = 0;
    public static final int STATE_DRAGE = 1;
    public static final int STATE_FLING = 2;
    public static final int STATE_BOUNCE = 3;

    final static int ACTION_BUTTON_CENTER = UIUtils.dpToPx(40);
    final static int ACTION_ICON_SIZE = UIUtils.dpToPx(32);
    private final static int DEFAULT_EXPAND = UIUtils.dpToPx(300);
    private final static int DEFAULT_HEIGHT = UIUtils.dpToPx(240);
    private final static int DEFAULT_SHRINK = UIUtils.dpToPx(48);

    private int mHeaderId = 0;
    private int mContentId = 0;

    private Drawable mActionDrawable;
    private FloatingActionButton mActionView;
    private ImageView mFlyView;
    private View mHeaderView;
    private IPullHeader mPullHeaderView;
    protected View mContent;
    protected HeaderController mHeaderController;

    private VelocityTracker mVelocityTracker;
    private ValueAnimator mBounceAnim;
    private int mPullState = STATE_IDLE;

    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsBeingDragged = false;
    private int mLastMotionY = 0;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mNestedYOffset;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];

    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;

    private ScrollerCompat mScroller;

    private OnPullListener mPullListener;

    public PullHeaderLayout(Context context) {
        this(context, null);
    }

    public PullHeaderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int headerHeight = DEFAULT_HEIGHT;
        int headerExpandHeight = DEFAULT_EXPAND;
        int headerShrinkHeight = DEFAULT_SHRINK;

        if (attrs != null) {
            TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PullHeaderLayout);
            headerHeight = arr.getDimensionPixelOffset(R.styleable.PullHeaderLayout_phl_header_height,
                    DEFAULT_HEIGHT);
            headerExpandHeight = arr.getDimensionPixelOffset(R.styleable.PullHeaderLayout_phl_header_expand_height,
                    DEFAULT_EXPAND);
            headerShrinkHeight = arr.getDimensionPixelOffset(R.styleable.PullHeaderLayout_phl_header_shrink_height,
                    DEFAULT_SHRINK);

            mHeaderId = arr.getResourceId(R.styleable.PullHeaderLayout_phl_header, mHeaderId);
            mContentId = arr.getResourceId(R.styleable.PullHeaderLayout_phl_content, mContentId);

            mActionDrawable = arr.getDrawable(R.styleable.PullHeaderLayout_phl_action);

            arr.recycle();
        }

        mHeaderController = new HeaderController(headerHeight, headerExpandHeight, headerShrinkHeight);

        final ViewConfiguration conf = ViewConfiguration.get(getContext());

        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);

        setNestedScrollingEnabled(true);

        init();
    }

    private void init() {
        mScroller = ScrollerCompat.create(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onStopNestedScroll(View target) {
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed) {
        final int myConsumed = moveBy(dyUnconsumed);
        final int myUnconsumed = dyUnconsumed - myConsumed;
        dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && mHeaderController.canScrollUp()) {
            final int delta = moveBy(dy);
            consumed[0] = 0;
            consumed[1] = delta;
            //dispatchNestedScroll(0, myConsumed, 0, consumed[1], null);
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            flingWithNestedDispatch((int) velocityY);
            return true;
        }
        return false;
    }

    private boolean flingWithNestedDispatch(int velocityY) {
        final boolean canFling = (mHeaderController.canScrollUp() && velocityY > 0) ||
                (mHeaderController.canScrollDown() && velocityY < 0);
        if (!dispatchNestedPreFling(0, velocityY)) {
            dispatchNestedFling(0, velocityY, canFling);
            if (canFling) {
                fling(velocityY);
            }
        }
        return canFling;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return flingWithNestedDispatch((int) velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

    public void setHeaderSize(int height, int maxHeight, int minHeight) {
        mHeaderController.setSize(height, maxHeight, minHeight);
        if (isLayoutRequested()) {
            requestLayout();
        }
    }

    public void setOnPullListener(OnPullListener listener) {
        mPullListener = listener;
    }

    public void setActionDrawable(Drawable actionDrawable) {
        mActionDrawable = actionDrawable;
        if (mActionDrawable != null) {
            if (mActionView == null) {
                final int bgColor = UIUtils.getThemeColorFromAttrOrRes(getContext(), R.attr.colorAccent, R.color.accent);
                final int pressedColor = UIUtils.darkerColor(bgColor, 0.8f);

                final ShapeDrawable bgDrawable = new ShapeDrawable(new OvalShape());
                bgDrawable.getPaint().setColor(bgColor);
                mActionView = new FloatingActionButton(getContext());
                mActionView.setRippleColor(pressedColor);
                mActionView.setBackgroundDrawable(bgDrawable);
                addView(mActionView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            if (mFlyView == null) {
                mFlyView = new ImageView(getContext());
                mFlyView.setScaleType(ImageView.ScaleType.FIT_XY);
                addView(mFlyView, new LayoutParams(ACTION_ICON_SIZE, ACTION_ICON_SIZE));
                mFlyView.bringToFront();
                float elevation = ViewCompat.getElevation(mActionView);
                ViewCompat.setElevation(mFlyView, elevation + 1);
            }
            mFlyView.setImageDrawable(mActionDrawable);

        } else {
            if (mActionView != null) {
                removeView(mActionView);
                removeView(mFlyView);
                mActionView = null;
                mFlyView = null;
            }
        }
    }

    @Nullable
    public View getIconView() {
        return mFlyView;
    }

    @Nullable
    public FloatingActionButton getHeaderActionButton() {
        return mActionView;
    }

    public void setHeaderView(View headerView, LayoutParams lp) {
        if (mHeaderView != null) {
            removeView(mHeaderView);
            mPullHeaderView = null;
        }

        addView(headerView, 0, lp);
        mHeaderView = headerView;

        if (mHeaderView instanceof IPullHeader) {
            mPullHeaderView = (IPullHeader) mHeaderView;
        }
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
                if (child1 instanceof IPullHeader) {
                    mHeaderView = child1;
                    mContent = child2;
                    mPullHeaderView = (IPullHeader) mHeaderView;
                } else if (child2 instanceof IPullHeader) {
                    mHeaderView = child2;
                    mContent = child1;
                    mPullHeaderView = (IPullHeader) mHeaderView;
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

        setActionDrawable(mActionDrawable);

        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mContent != null) {
            measureChildWithMargins(mContent, widthMeasureSpec, 0, heightMeasureSpec, mHeaderController.getMinHeight());
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
        int offsetY = mHeaderController.getCurPosition();
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
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

            mActionView.layout(center - halfWidth , offsetY - halfHeight,
                    center + halfWidth, offsetY + halfHeight);

            halfWidth = (mFlyView.getMeasuredWidth() + 1) / 2;
            halfHeight = (mFlyView.getMeasuredHeight() + 1) / 2;
            mFlyView.layout(center - halfWidth, offsetY - halfHeight,
                    center + halfWidth, offsetY + halfHeight);
        }

    }

    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) MotionEventCompat.getY(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;
        releaseVelocityTracker();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        if(!isEnabled()) {
            return false;
        }

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId
                            + " in onInterceptTouchEvent");
                    break;
                }

                final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
                final int yDiff = Math.abs(y - mLastMotionY);
                if (yDiff > mTouchSlop
                        && (getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == 0) {
                    mIsBeingDragged = true;
                    mLastMotionY = y;
                    obtainVelocityTracker(ev);
                    mNestedYOffset = 0;
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int y = (int) ev.getY();

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = y;
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                /*
                * If being flinged and user touches the screen, initiate drag;
                * otherwise don't.  mScroller.isFinished should be false when
                * being flinged.
                */
                mIsBeingDragged = !mScroller.isFinished();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                endDrag();
                stopNestedScroll();
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int actionMasked = MotionEventCompat.getActionMasked(ev);
        if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
            tryBounceBack();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        MotionEvent vtev = MotionEvent.obtain(ev);

        final int actionMasked = MotionEventCompat.getActionMasked(ev);

        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
        }
        vtev.offsetLocation(0, mNestedYOffset);

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                if ((mIsBeingDragged = !mScroller.isFinished())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionY = (int) ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                        mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }

                final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
                int deltaY = mLastMotionY - y;
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    vtev.offsetLocation(0, mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }
                if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop;
                    } else {
                        deltaY += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionY = y - mScrollOffset[1];

                    final int scrolledDeltaY = moveBy(deltaY);
                    final int unconsumedY = deltaY - scrolledDeltaY;
                    if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
                        mLastMotionY -= mScrollOffset[1];
                        vtev.offsetLocation(0, mScrollOffset[1]);
                        mNestedYOffset += mScrollOffset[1];
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker,
                            mActivePointerId);

                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        flingWithNestedDispatch(-initialVelocity);
                    }

                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && getChildCount() > 0) {
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionY = (int) MotionEventCompat.getY(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionY = (int) MotionEventCompat.getY(ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    protected void onMoveHeader(int state, float progress) {}

    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityY) {
        mPullState = STATE_FLING;
        mScroller.abortAnimation();
        mScroller.fling(0, mHeaderController.getScroll(), 0, velocityY, 0, 0,
                mHeaderController.getMinScroll(), mHeaderController.getMaxScroll(),
                0, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private int moveBy(float deltaY) {
        int oldScroll = mHeaderController.getScroll();
        int consumed = mHeaderController.move(deltaY);

        final int delta = mHeaderController.getScroll() - oldScroll;
        if (delta == 0) {
            return 0;
        }

        if (mPullState != STATE_DRAGE) {
            mPullState = STATE_DRAGE;
        }

        if (mContent != null) {
            mContent.offsetTopAndBottom(-delta);
        }

        if (mActionView != null) {
            mActionView.offsetTopAndBottom(-delta);

            mFlyView.offsetTopAndBottom(-delta);

            float percentage = mHeaderController.getMovePercentage();
            onMoveHeader(mPullState, percentage);

            if (mPullHeaderView != null) {
                mPullHeaderView.onPullProgress(this, mPullState, percentage);
            }

            if (mPullListener != null) {
                mPullListener.onPullProgress(this, mPullState, percentage);
            }

        }

        return consumed;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldY = mHeaderController.getScroll();
            int y = mScroller.getCurrY();

            if (oldY != y) {
                moveBy(y - oldY);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            tryBounceBack();
        }
    }

    private void tryBounceBack() {
        if (mHeaderController.isOverHeight()) {
            mBounceAnim = ObjectAnimator.ofFloat(mHeaderController.getScroll(), 0);
            mBounceAnim.setInterpolator(new ElasticOutInterpolator());
            mBounceAnim.setDuration(500);
            mBounceAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float deltaY = (float) animation.getAnimatedValue() - mHeaderController.getScroll();
                    moveBy(deltaY);
                }
            });
            mBounceAnim.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPullState = STATE_IDLE;
                }
            });

            mBounceAnim.start();
            mPullState = STATE_BOUNCE;

            if (mHeaderController.needSendRefresh()) {
                startRefresh();
            }
        } else {
            mPullState = STATE_IDLE;
        }
    }

    public void startRefresh() {}

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

    public interface OnPullListener {
        void onPullProgress(PullHeaderLayout view, int state, float progress);
    }
}
