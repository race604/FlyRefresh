package com.race604.flyrefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.race604.utils.UIUtils;

/**
 * Created by Jing on 15/5/18.
 */
public class FlyRefreshView extends FrameLayout {

    private final static int DEFAULT_EXPAND = UIUtils.dpToPx(40);
    private final static int DEFAULT_HEIGHT = 0;

    private int mHeaderHeight = DEFAULT_HEIGHT;
    private int mHeaderExpandHeight = DEFAULT_EXPAND;
    private int mHeaderShrinkHeight = DEFAULT_HEIGHT;

    public FlyRefreshView(Context context) {
        super(context);
        init(context, null);
    }

    public FlyRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FlyRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlyRefreshView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.FlyRefreshView);
        mHeaderHeight = arr.getDimensionPixelOffset(R.styleable.FlyRefreshView_frv_header_height,
                DEFAULT_HEIGHT);
        mHeaderExpandHeight = arr.getDimensionPixelOffset(R.styleable.FlyRefreshView_frv_header_expand_height,
                DEFAULT_EXPAND);
        mHeaderShrinkHeight = arr.getDimensionPixelOffset(R.styleable.FlyRefreshView_frv_header_shrink_height,
                DEFAULT_HEIGHT);

        arr.recycle();
    }
}
