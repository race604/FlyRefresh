package com.race604.flyrefresh.sample;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jing on 15-5-28.
 */
public class SampleItemAnimator extends RecyclerView.ItemAnimator {

    private List<RecyclerView.ViewHolder> mAnimationAddViewHolders = new ArrayList<RecyclerView.ViewHolder>();

    @Override
    public void runPendingAnimations() {
        if (!mAnimationAddViewHolders.isEmpty()) {
            AnimatorSet animator;
            View target;
            for (final RecyclerView.ViewHolder viewHolder : mAnimationAddViewHolders) {
                target = viewHolder.itemView;
                AnimatorSet zoomin = new AnimatorSet();

                zoomin.playTogether(
                        ObjectAnimator.ofFloat(target, "scaleX", 0, 1.0f),
                        ObjectAnimator.ofFloat(target, "scaleY", 0, 1.0f),
                        ObjectAnimator.ofFloat(target, "alpha", 0.5f, 1.0f)
                );
                zoomin.setInterpolator(new AccelerateInterpolator());
                zoomin.setDuration(300);

                View icon = target.findViewById(R.id.icon);
                if (icon != null) {
                    animator = new AnimatorSet();
                    Animator swing = ObjectAnimator.ofFloat(icon, "rotationX", 0, 45, -30, 0);
                    swing.setDuration(500);
                    swing.setInterpolator(new DecelerateInterpolator());
                    animator.playSequentially(zoomin, swing);
                    animator.start();
                } else {
                    zoomin.start();
                }

            }
        }
        mAnimationAddViewHolders.clear();
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder viewHolder) {
        return false;
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        return mAnimationAddViewHolders.add(viewHolder);
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder viewHolder, int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1, int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    public void endAnimations() {

    }

    @Override
    public boolean isRunning() {
        return !mAnimationAddViewHolders.isEmpty();
    }
}
