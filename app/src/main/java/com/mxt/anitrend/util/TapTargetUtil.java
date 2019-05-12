package com.mxt.anitrend.util;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;

import com.annimon.stream.Stream;
import com.mxt.anitrend.R;

import java.util.ArrayList;
import java.util.List;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;

/**
 * Created by max on 2018/03/01. MaterialTapTargetPrompt helper class, should not be used directly
 * @see TutorialUtil
 */

public class TapTargetUtil {

    private static List<String> activePrompts = new ArrayList<>();

    public static void showMultiplePrompts(@Nullable MaterialTapTargetPrompt.Builder... tapTargetPrompts) {
        if (tapTargetPrompts != null) {
            MaterialTapTargetSequence materialTapTargetSequence = new MaterialTapTargetSequence();
            Stream.of(tapTargetPrompts).filter(it -> it != null)
                    .forEach(it -> materialTapTargetSequence.addPrompt(it.create()));
            if(materialTapTargetSequence.size() > 0)
                materialTapTargetSequence.show();
        }
    }

    public static MaterialTapTargetPrompt.Builder buildDefault(FragmentActivity context, @IdRes int resource) {
        return new MaterialTapTargetPrompt.Builder(context)
                .setTarget(context.findViewById(resource))
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setPrimaryTextColour(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.titleColor))
                .setSecondaryTextColour(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.subtitleColor))
                .setBackgroundColour(ColorUtils.setAlphaComponent(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.colorPrimaryDark), 0xF2));
    }

    public static MaterialTapTargetPrompt.Builder buildDefault(FragmentActivity context, @StringRes int primary, @StringRes int secondary, @IdRes int resource) {
        return new MaterialTapTargetPrompt.Builder(context)
                .setTarget(context.findViewById(resource))
                .setPrimaryText(primary).setSecondaryText(secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setPrimaryTextColour(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.titleColor))
                .setSecondaryTextColour(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.subtitleColor))
                .setBackgroundColour(ColorUtils.setAlphaComponent(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.colorPrimaryDark), 0xF2));
    }

    public static MaterialTapTargetPrompt.Builder buildDefault(FragmentActivity context, @StringRes int primary, @StringRes int secondary, View target) {
        return new MaterialTapTargetPrompt.Builder(context).setTarget(target)
                .setPrimaryText(primary).setSecondaryText(secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setPrimaryTextColour(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.titleColor))
                .setSecondaryTextColour(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.subtitleColor))
                .setBackgroundColour(ColorUtils.setAlphaComponent(CompatUtil.INSTANCE.getColorFromAttr(context, R.attr.colorPrimaryDark), 0xF2));
    }

    /**
     * Checks if the current application tip for the given target is currently showing
     * and returns a boolean based on that
     */
    public static boolean isActive(@KeyUtil.TapTargetType String key) {
        return activePrompts.contains(key);
    }

    /**
     * Adds or removes the key for a given tip
     */
    public static void setActive(@KeyUtil.TapTargetType String key, boolean remove) {
        if(!remove) activePrompts.add(key);
        else if(isActive(key))
            activePrompts.remove(key);
    }
}
