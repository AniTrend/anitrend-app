package com.mxt.anitrend.base.custom.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.mxt.anitrend.base.interfaces.base.BaseAnimation;

/**
 * Created by max on 2018/02/24.
 */

public class ScaleAnimation implements BaseAnimation {

    private final float FROM, TO;
    private Interpolator interpolator = new LinearInterpolator();

    public ScaleAnimation() {
        this.FROM = .85f;
        this.TO = 1f;
    }

    public ScaleAnimation(float FROM, float TO) {
        this.FROM = FROM;
        this.TO = TO;
    }

    @Override
    public Animator[] getAnimators(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", FROM, TO);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", FROM, TO);
        return new Animator[]{scaleX, scaleY};
    }

    @Override
    public Interpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public int getAnimationDuration() {
        return DEFAULT_ANIMATION_DURATION;
    }
}
