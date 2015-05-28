package com.race604.flyrefresh.sample;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import jp.wasabeef.recyclerview.animators.BaseItemAnimator;

/**
 * Created by jing on 15-5-28.
 */
public class SampleItemAnimator extends BaseItemAnimator {

    @Override
    protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
        View icon = holder.itemView.findViewById(R.id.icon);
        icon.setRotationX(30);
        View right = holder.itemView.findViewById(R.id.right);
        right.setPivotX(0);
        right.setPivotY(0);
        right.setRotationY(90);
    }

    @Override
    protected void animateRemoveImpl(RecyclerView.ViewHolder viewHolder) {
    }

    @Override
    protected void animateAddImpl(final RecyclerView.ViewHolder holder) {
        View target = holder.itemView;
        View icon = target.findViewById(R.id.icon);
        Animator swing = ObjectAnimator.ofFloat(icon, "rotationX", 45, 0);
        swing.setInterpolator(new OvershootInterpolator(5));

        View right = holder.itemView.findViewById(R.id.right);
        Animator rotateIn = ObjectAnimator.ofFloat(right, "rotationY", 90, 0);
        rotateIn.setInterpolator(new DecelerateInterpolator());

        AnimatorSet animator = new AnimatorSet();
        animator.setDuration(getAddDuration());
        animator.playTogether(swing, rotateIn);

        animator.start();
    }

}
