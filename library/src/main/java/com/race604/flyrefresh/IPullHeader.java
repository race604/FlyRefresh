package com.race604.flyrefresh;

/**
 * Created by jing on 15-5-19.
 */
public interface IPullHeader {
    void onPullProgress(PullHeaderLayout parent, int state, float progress);
}
