package com.mxt.anitrend.base.interfaces.base;

import android.animation.Animator;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by max on 2018/02/24.
 * original impl -> https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */

public interface BaseAnimation {

    int DEFAULT_ANIMATION_DURATION = 250;

    Animator[] getAnimators(View view);

    Interpolator getInterpolator();

    int getAnimationDuration();
}
