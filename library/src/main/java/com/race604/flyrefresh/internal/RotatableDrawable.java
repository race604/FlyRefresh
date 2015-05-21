package com.race604.flyrefresh.internal;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

/**
 * Created by jing on 15-5-21.
 */
public class RotatableDrawable extends LayerDrawable {

    private float mDegree = 0;

    /**
     * Create a new layer drawable with the list of specified layers.
     *
     * @param layers A list of drawables to use as layers in this new drawable.
     */
    public RotatableDrawable(Drawable[] layers) {
        super(layers);
    }

    public RotatableDrawable(Drawable drawable) {
        this(new Drawable[]{drawable});
    }

    public void setDegree(float degree) {
        mDegree = degree;
    }

    public float getDegree() {
        return mDegree;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        Rect bounds = getBounds();
        canvas.rotate(mDegree, bounds.centerX(), bounds.centerY());
        super.draw(canvas);
        canvas.restore();
    }

}
