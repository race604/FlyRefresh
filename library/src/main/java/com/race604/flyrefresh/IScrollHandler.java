package com.race604.flyrefresh;

import android.view.View;

/**
 * Created by jing on 15-5-20.
 */
public interface IScrollHandler {
    boolean canScrollUp(View view);
    boolean canScrollDown(View view);
}
