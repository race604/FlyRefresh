package com.race604.flyrefresh;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.race604.flyrefresh.internal.MountainSceneDrawable;
import com.race604.flyrefresh.internal.SimpleAnimatorListener;
import com.race604.utils.UIUtils;

/**
 * Created by jing on 15-5-27.
 */
public class FlyRefreshLayout extends PullHeaderLayout {

    private MountainSceneDrawable mSceneDrawable;

    public FlyRefreshLayout(Context context) {
        super(context);
        init(context);
    }

    public FlyRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlyRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FlyRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        int maxHeight = UIUtils.dpToPx(300);
        setHeaderSize((int) (maxHeight * 0.8f), maxHeight, UIUtils.dpToPx(48));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSceneDrawable = new MountainSceneDrawable();
        ImageView headerView = new ImageView(getContext());
        headerView.setScaleType(ImageView.ScaleType.FIT_XY);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setHeaderView(headerView, lp);
        headerView.setImageDrawable(mSceneDrawable);

        setActionDrawable(getResources().getDrawable(R.mipmap.ic_send));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void resetViewAnimation(View view) {
        view.clearAnimation();
        view.clearAnimation();
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setRotation(0);
        view.setRotationX(0);
        view.setRotationY(0);
    }

    @Override
    protected void onStartRefreshAnimation() {

        final View iconView = getIconView();
        resetViewAnimation(iconView);

        AnimatorSet flyUpAnim = new AnimatorSet();
        flyUpAnim.setDuration(1000);

        ObjectAnimator transX = ObjectAnimator.ofFloat(iconView, "translationX", 0, getWidth());
        ObjectAnimator transY = ObjectAnimator.ofFloat(iconView, "translationY", 0, -mHeaderController.getHeight());
        transY.setInterpolator(PathInterpolatorCompat.create(0.4f, 1f));
        ObjectAnimator rotation = ObjectAnimator.ofFloat(iconView, "rotation", -45, 0);
        rotation.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator rotationX = ObjectAnimator.ofFloat(iconView, "rotationX", 0, 60);
        rotationX.setInterpolator(new DecelerateInterpolator());

        flyUpAnim.playTogether(transX, transY, rotationX,
                ObjectAnimator.ofFloat(iconView, "scaleX", 1, 0.5f),
                ObjectAnimator.ofFloat(iconView, "scaleY", 1, 0.5f),
                rotation
        );

        final int offDistX = -ACTION_ICON_SIZE / 2 - ACTION_BUTTON_CENTER;
        final int offDistY = -UIUtils.dpToPx(10);
        AnimatorSet flyDownAnim = new AnimatorSet();
        flyDownAnim.setDuration(1000);
        ObjectAnimator transX1 = ObjectAnimator.ofFloat(iconView, "translationX", getWidth(), offDistX);
        ObjectAnimator transY1 = ObjectAnimator.ofFloat(iconView, "translationY", -mHeaderController.getHeight(), offDistY);
        transY1.setInterpolator(PathInterpolatorCompat.create(0.1f, 1f));
        ObjectAnimator rotation1 = ObjectAnimator.ofFloat(iconView, "rotation", iconView.getRotation(), 0);
        rotation1.setInterpolator(new AccelerateInterpolator());
        flyDownAnim.playTogether(transX1, transY1,
                ObjectAnimator.ofFloat(iconView, "scaleX", 0.5f, 0.9f),
                ObjectAnimator.ofFloat(iconView, "scaleY", 0.5f, 0.9f),
                rotation1
        );
        flyDownAnim.setStartDelay(400);
        flyDownAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                iconView.setRotationY(180);
            }
        });

        AnimatorSet flyInAnim = new AnimatorSet();
        flyInAnim.setDuration(500);
        flyInAnim.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator tranX2 = ObjectAnimator.ofFloat(iconView, "translationX", offDistX, 0);
        ObjectAnimator tranY2 = ObjectAnimator.ofFloat(iconView, "translationY", offDistY, 0);
        ObjectAnimator rotationX2 = ObjectAnimator.ofFloat(iconView, "rotationX", 30, 0);
        flyInAnim.playTogether(tranX2, tranY2, rotationX2,
                ObjectAnimator.ofFloat(iconView, "scaleX", 0.9f, 1f),
                ObjectAnimator.ofFloat(iconView, "scaleY", 0.9f, 1f));
        flyInAnim.setStartDelay(100);
        flyInAnim.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                iconView.setRotationY(0);
            }
        });

        AnimatorSet flyAnim = new AnimatorSet();
        flyAnim.playSequentially(flyUpAnim, flyDownAnim, flyInAnim);
        flyAnim.start();
    }

    @Override
    protected void onMoveHeader(int state, float progress) {
        super.onMoveHeader(state, progress);
        mSceneDrawable.setMoveFactor(state, progress);
        if (mHeaderController.isOverHeight()) {
            getIconView().setRotation((-45) * progress);
        }
    }
}
