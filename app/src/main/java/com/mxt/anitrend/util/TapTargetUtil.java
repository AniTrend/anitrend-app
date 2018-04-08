package com.mxt.anitrend.util;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;

import com.mxt.anitrend.R;

import java.util.Map;
import java.util.WeakHashMap;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by max on 2018/03/01.
 * MaterialTapTargetPrompt helper class
 */

public class TapTargetUtil {

    private static Map<String, Void> activePrompts = new WeakHashMap<>();

    public static MaterialTapTargetPrompt.Builder buildDefault(FragmentActivity context, @IdRes int resource) {
        return new MaterialTapTargetPrompt.Builder(context)
                .setTarget(context.findViewById(resource))
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setPrimaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.titleColor))
                .setSecondaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.subtitleColor))
                .setBackgroundColour(ColorUtils.setAlphaComponent(CompatUtil.getColorFromAttr(context, R.attr.colorPrimaryDark), 0xF2));
    }

    public static MaterialTapTargetPrompt.Builder buildDefault(FragmentActivity context, @StringRes int primary, @StringRes int secondary, @IdRes int resource) {
        return new MaterialTapTargetPrompt.Builder(context)
                .setTarget(context.findViewById(resource))
                .setPrimaryText(primary).setSecondaryText(secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setPrimaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.titleColor))
                .setSecondaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.subtitleColor))
                .setBackgroundColour(ColorUtils.setAlphaComponent(CompatUtil.getColorFromAttr(context, R.attr.colorPrimaryDark), 0xF2));
    }

    public static MaterialTapTargetPrompt.Builder buildDefault(FragmentActivity context, @StringRes int primary, @StringRes int secondary, View target) {
        return new MaterialTapTargetPrompt.Builder(context).setTarget(target)
                .setPrimaryText(primary).setSecondaryText(secondary)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setPrimaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.titleColor))
                .setSecondaryTextColour(CompatUtil.getColorFromAttr(context, R.attr.subtitleColor))
                .setBackgroundColour(ColorUtils.setAlphaComponent(CompatUtil.getColorFromAttr(context, R.attr.colorPrimaryDark), 0xF2));
    }

    public static boolean isActive(@KeyUtil.TapTargetType String key) {
        return activePrompts.containsKey(key);
    }

    public static void setActive(@KeyUtil.TapTargetType String key, boolean remove) {
        if(!remove) activePrompts.put(key, null);
        else if(isActive(key))
            activePrompts.remove(key);
    }
}
