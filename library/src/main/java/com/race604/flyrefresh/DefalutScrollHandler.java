package com.race604.flyrefresh;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ScrollView;

/**
 * Created by jing on 15-5-20.
 */
public class DefalutScrollHandler implements IScrollHandler {

    @Override
    public boolean canScrollUp(View view) {
        return checkCanDoScrollUp(view);
    }

    @Override
    public boolean canScrollDown(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                final int childCount = absListView.getChildCount();
                return childCount > 0
                        && (absListView.getLastVisiblePosition() < absListView.getChildCount() || absListView.getChildAt(childCount)
                        .getBottom() > (absListView.getBottom() - absListView.getPaddingBottom()));
            } else if (view instanceof ScrollView) {
                View child = ((ScrollView) view).getChildAt(0);
                return child != null && (child.getBottom() > view.getBottom() - view.getPaddingBottom());
            } else {
                return view.getScaleY() > 0; // fix this
            }
        } else {
            return view.canScrollVertically(1);
        }
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
