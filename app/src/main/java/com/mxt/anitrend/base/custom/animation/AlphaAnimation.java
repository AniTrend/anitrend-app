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

public class AlphaAnimation implements BaseAnimation {

    private final float FROM, TO;
    private Interpolator interpolator = new LinearInterpolator();

    public AlphaAnimation() {
        this.FROM = .85f;
        this.TO = 1f;
    }

    public AlphaAnimation(float FROM, float TO) {
        this.FROM = FROM;
        this.TO = TO;
    }

    @Override
    public Animator[] getAnimators(View view) {
        return new Animator[] {ObjectAnimator.ofFloat(view, "alpha", FROM, TO)};
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
