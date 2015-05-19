package com.race604.flyrefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.race604.utils.UIUtils;

/**
 * Created by Jing on 15/5/18.
 */
public class FlyRefreshLayout extends ViewGroup {

    private static final String TAG = FlyRefreshLayout.class.getCanonicalName();
    private static final boolean D = true;

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

    private View mHeaderView;
    protected View mContent;
    protected HeaderController mHeaderController;

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

            arr.recycle();
        }

        mHeaderController = new HeaderController(mHeaderHeight, mHeaderExpandHeight, mHeaderShrinkHeight);
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
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

        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (mContent != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, mHeaderShrinkHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren();
    }

    private void layoutChildren() {
        int offsetY = mHeaderController.getOffsetY();
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
    }

    private void sendCancelEvent() {
        MotionEvent last = mDownEvent;
        last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(),
                last.getEventTime() + ViewConfiguration.getLongPressTimeout(),
                MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        super.dispatchTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mContent == null || mHeaderView == null) {
            return super.dispatchTouchEvent(ev);
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHeaderController.onTouchRelease();
                if (mHeaderController.isInTouch()) {
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

                mPreventForHorizontal = false;
                if (mHeaderController.isInTouch()) {
                    // do nothing, intercept child event
                } else {
                    super.dispatchTouchEvent(ev);
                }
                return true;

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
                boolean moveUp = !moveDown;
                boolean canMoveUp = mHeaderController.hasMoved();

                // disable move when header not reach top
                if (moveDown && mContent != null && !checkCanDoScrollUp(mContent)) {
                    return super.dispatchTouchEvent(ev);
                }

                if ((moveUp && canMoveUp) || moveDown) {
                    //movePos(offsetY);
                    return true;
                }
        }
        return super.dispatchTouchEvent(ev);
    }

    private static boolean checkCanDoScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

}
