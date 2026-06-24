package com.mxt.anitrend.base.custom.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.mxt.anitrend.base.interfaces.base.BaseAnimation

/**
 * Created by max on 2018/02/24.
 */
class ScaleAnimation
@JvmOverloads
constructor(
    private val from: Float = 0.85f,
    private val to: Float = 1f,
) : BaseAnimation {
    private val interpolator: Interpolator = LinearInterpolator()

    override fun getAnimators(view: View): Array<Animator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", from, to)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", from, to)
        return arrayOf(scaleX, scaleY)
    }

    override fun getInterpolator(): Interpolator = interpolator

    override fun getAnimationDuration(): Int = BaseAnimation.DEFAULT_ANIMATION_DURATION
}
