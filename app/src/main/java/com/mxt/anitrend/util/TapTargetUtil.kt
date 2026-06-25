package com.mxt.anitrend.util

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.mxt.anitrend.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence

/**
 * Created by max on 2018/03/01. MaterialTapTargetPrompt helper class, should not be used directly
 * @see TutorialUtil
 */
object TapTargetUtil {
    private val activePrompts = mutableListOf<String>()

    fun showMultiplePrompts(vararg tapTargetPrompts: MaterialTapTargetPrompt.Builder?) {
        if (tapTargetPrompts.isEmpty()) {
            return
        }
        val sequence = MaterialTapTargetSequence()
        tapTargetPrompts
            .filterNotNull()
            .forEach { sequence.addPrompt(it.create()) }
        if (sequence.size() > 0) {
            sequence.show()
        }
    }

    fun buildDefault(
        context: FragmentActivity,
        @IdRes resource: Int,
    ): MaterialTapTargetPrompt.Builder = MaterialTapTargetPrompt
        .Builder(context)
        .setTarget(context.findViewById(resource))
        .setAnimationInterpolator(FastOutSlowInInterpolator())
        .setPrimaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.titleColor))
        .setSecondaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.subtitleColor))
        .setBackgroundColour(
            ColorUtils.setAlphaComponent(
                CompatUtil.getColorFromAttr(context, R.attr.colorPrimaryDark),
                0xF2,
            ),
        )

    fun buildDefault(
        context: FragmentActivity,
        @StringRes primary: Int,
        @StringRes secondary: Int,
        @IdRes resource: Int,
    ): MaterialTapTargetPrompt.Builder = MaterialTapTargetPrompt
        .Builder(context)
        .setTarget(context.findViewById(resource))
        .setPrimaryText(primary)
        .setSecondaryText(secondary)
        .setAnimationInterpolator(FastOutSlowInInterpolator())
        .setPrimaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.titleColor))
        .setSecondaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.subtitleColor))
        .setBackgroundColour(
            ColorUtils.setAlphaComponent(
                CompatUtil.getColorFromAttr(context, R.attr.colorPrimaryDark),
                0xF2,
            ),
        )

    fun buildDefault(
        context: FragmentActivity,
        @StringRes primary: Int,
        @StringRes secondary: Int,
        target: View,
    ): MaterialTapTargetPrompt.Builder = MaterialTapTargetPrompt
        .Builder(context)
        .setTarget(target)
        .setPrimaryText(primary)
        .setSecondaryText(secondary)
        .setAnimationInterpolator(FastOutSlowInInterpolator())
        .setPrimaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.titleColor))
        .setSecondaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.subtitleColor))
        .setBackgroundColour(
            ColorUtils.setAlphaComponent(
                CompatUtil.getColorFromAttr(context, R.attr.colorPrimaryDark),
                0xF2,
            ),
        )

    /**
     * Checks if the current application tip for the given target is currently showing
     * and returns a boolean based on that
     */
    fun isActive(
        @KeyUtil.TapTargetType key: String,
    ): Boolean = activePrompts.contains(key)

    /**
     * Adds or removes the key for a given tip
     */
    fun setActive(
        @KeyUtil.TapTargetType key: String,
        remove: Boolean,
    ) {
        if (!remove) {
            activePrompts.add(key)
        } else if (isActive(key)) {
            activePrompts.remove(key)
        }
    }
}
