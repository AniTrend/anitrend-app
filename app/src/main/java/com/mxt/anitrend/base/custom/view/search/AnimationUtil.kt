/*******************************************************************************
 * Copyright (c) 2025 Miguel Catalan Banuls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mxt.anitrend.base.custom.view.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener

/**
 * @author Miguel Catalan Banuls
 */
object AnimationUtil {

    const val ANIMATION_DURATION_SHORT = 150
    const val ANIMATION_DURATION_MEDIUM = 400
    const val ANIMATION_DURATION_LONG = 800

    interface AnimationListener {
        /** @return true to override parent. Else execute Parent method */
        fun onAnimationStart(view: View): Boolean
        fun onAnimationEnd(view: View): Boolean
        fun onAnimationCancel(view: View): Boolean
    }

    @JvmStatic
    fun crossFadeViews(showView: View, hideView: View) {
        crossFadeViews(showView, hideView, ANIMATION_DURATION_SHORT)
    }

    @JvmStatic
    fun crossFadeViews(showView: View, hideView: View, duration: Int) {
        fadeInView(showView, duration)
        fadeOutView(hideView, duration)
    }

    @JvmStatic
    fun fadeInView(view: View) {
        fadeInView(view, ANIMATION_DURATION_SHORT)
    }

    @JvmStatic
    fun fadeInView(view: View, duration: Int) {
        fadeInView(view, duration, null)
    }

    @JvmStatic
    fun fadeInView(view: View, duration: Int, listener: AnimationListener?) {
        view.visibility = View.VISIBLE
        view.alpha = 0f

        val vpListener = listener?.let { animListener ->
            object : ViewPropertyAnimatorListener {
                override fun onAnimationStart(view: View) {
                    if (!animListener.onAnimationStart(view)) {
                        view.isDrawingCacheEnabled = true
                    }
                }

                override fun onAnimationEnd(view: View) {
                    if (!animListener.onAnimationEnd(view)) {
                        view.isDrawingCacheEnabled = false
                    }
                }

                override fun onAnimationCancel(view: View) {}
            }
        }

        ViewCompat.animate(view)
            .alpha(1f)
            .setDuration(duration.toLong())
            .setListener(vpListener)
    }

    @JvmStatic
    fun reveal(view: View, listener: AnimationListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cx = view.width - TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                24f,
                view.resources.displayMetrics
            ).toInt()
            val cy = view.height / 2
            val finalRadius = maxOf(view.width, view.height)

            val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius.toFloat())
            view.visibility = View.VISIBLE
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    listener.onAnimationStart(view)
                }

                override fun onAnimationEnd(animation: Animator) {
                    listener.onAnimationEnd(view)
                }

                override fun onAnimationCancel(animation: Animator) {
                    listener.onAnimationCancel(view)
                }

                override fun onAnimationRepeat(animation: Animator) {}
            })
            anim.start()
        } else {
            fadeInView(view, ANIMATION_DURATION_MEDIUM, listener)
        }
    }

    @JvmStatic
    fun fadeOutView(view: View) {
        fadeOutView(view, ANIMATION_DURATION_SHORT)
    }

    @JvmStatic
    fun fadeOutView(view: View, duration: Int) {
        fadeOutView(view, duration, null)
    }

    @JvmStatic
    fun fadeOutView(view: View, duration: Int, listener: AnimationListener?) {
        ViewCompat.animate(view)
            .alpha(0f)
            .setDuration(duration.toLong())
            .setListener(object : ViewPropertyAnimatorListener {
                override fun onAnimationStart(view: View) {
                    if (listener == null || !listener.onAnimationStart(view)) {
                        view.isDrawingCacheEnabled = true
                    }
                }

                override fun onAnimationEnd(view: View) {
                    if (listener == null || !listener.onAnimationEnd(view)) {
                        view.visibility = View.GONE
                        view.isDrawingCacheEnabled = false
                    }
                }

                override fun onAnimationCancel(view: View) {}
            })
    }
}
