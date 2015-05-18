package com.race604.utils;

import android.content.res.Resources;

/**
 * Created by Jing on 15/5/18.
 */
public class UIUtils {

    public static final int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
