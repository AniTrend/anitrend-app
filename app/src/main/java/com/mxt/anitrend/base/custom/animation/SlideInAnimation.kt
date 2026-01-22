package com.mxt.anitrend.base.custom.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.mxt.anitrend.base.interfaces.base.BaseAnimation

/**
 * Created by max on 2018/02/26.
 */
class SlideInAnimation : BaseAnimation {

    private val interpolator: Interpolator = LinearInterpolator()

    override fun getAnimators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "translationY", view.measuredHeight.toFloat(), 0f))

    override fun getInterpolator(): Interpolator = interpolator

    override fun getAnimationDuration(): Int = BaseAnimation.DEFAULT_ANIMATION_DURATION + 100
}
