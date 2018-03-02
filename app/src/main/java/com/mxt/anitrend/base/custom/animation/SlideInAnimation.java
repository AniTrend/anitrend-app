package com.mxt.anitrend.base.custom.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.mxt.anitrend.base.interfaces.base.BaseAnimation;

/**
 * Created by max on 2018/02/26.
 */

public class SlideInAnimation implements BaseAnimation {

    private Interpolator interpolator = new LinearInterpolator();

    @Override
    public Animator[] getAnimators(View view) {
        return new Animator[] {
            ObjectAnimator.ofFloat(view, "translationY", view.getMeasuredHeight(), 0)
        };
    }

    @Override
    public Interpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public int getAnimationDuration() {
        return DEFAULT_ANIMATION_DURATION + 100;
    }
}
