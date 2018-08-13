package com.mxt.anitrend.util;

import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.mxt.anitrend.base.custom.presenter.CommonPresenter;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.PromptStateChangeListener;

/**
 * Helper class to display application
 * Example usage:
 * <br/>
 *
 * <code>
 * new TutorialUtil().setContext(this)
 *     .setFocalColour(R.color.colorGrey600)
 *     .setTapTarget(KeyUtil.KEY_NOTIFICATION_TIP)
 *     .setApplicationPref(getPresenter().getApplicationPref())
 *     .createTapTarget(
 *         R.string.tip_notifications_title,
 *         R.string.tip_notifications_text,
 *         R.id.action_notification
 *     );
 * </code>
 */
public class TutorialUtil {

    private @KeyUtil.TapTargetType String tapTarget;
    private @ColorRes int focalColour;

    private @Nullable PromptStateChangeListener listener;
    private FragmentActivity context;

    private ApplicationPref applicationPref;

    /**
     * Optional. After the tip is dismissed, this helper class will automatically save
     * the state so that the tip doesn't get shown next time again.
     * @see #defaultStateChangeListener
     */
    public TutorialUtil setListener(@Nullable PromptStateChangeListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Mandatory preference key for saving or showing the state of the tip
     * <br/>
     *
     * @param tapTarget A type of string that represents the preference key for the target tip
     *                  @see KeyUtil.TapTargetType
     */
    public TutorialUtil setTapTarget(@KeyUtil.TapTargetType String tapTarget) {
        this.tapTarget = tapTarget;
        return this;
    }

    /**
     * Mandatory activity context used to find target views ids
     * <br/>
     *
     * @param context Must be a valid FragmentActivity derivative
     *                @see FragmentActivity
     */
    public TutorialUtil setContext(FragmentActivity context) {
        this.context = context;
        return this;
    }

    /**
     * Mandatory color resource for the focal point of the tap target
     */
    public TutorialUtil setFocalColour(@ColorRes int focalColour) {
        this.focalColour = focalColour;
        return this;
    }

    /**
     * Mandatory application prefs of the current fragment activity, it is wise to use
     * the presenters application preference object rather than creating a new one.
     * <br/>
     *
     * @see CommonPresenter#getApplicationPref()
     */
    public TutorialUtil setApplicationPref(ApplicationPref applicationPref) {
        this.applicationPref = applicationPref;
        return this;
    }

    /**
     * Get the prompt you want to display, highlighting a given a resource id
     * <br/>
     *
     * @param resource Item that should be focused on by the application tip
     */
    public @Nullable MaterialTapTargetPrompt.Builder createTapTarget(@IdRes int resource) {
        if(applicationPref == null) {
            Log.e(toString(), "Did you forget to set the current application preferences?");
            return null;
        }
        if (!TapTargetUtil.isActive(tapTarget) && applicationPref.shouldShowTipFor(tapTarget))
            return TapTargetUtil.buildDefault(context, resource)
                    .setPromptStateChangeListener(defaultStateChangeListener)
                    .setFocalColour(CompatUtil.getColor(context, focalColour));
        return null;
    }

    /**
     * Get the prompt you want to display, highlighting a given a resource id, heading and subheading
     * <br/>
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    public @Nullable MaterialTapTargetPrompt.Builder createTapTarget(@StringRes int primary, @StringRes int secondary, @IdRes int resource) {
        if(applicationPref == null) {
            Log.e(toString(), "Did you forget to set the current application preferences?");
            return null;
        }
        if (!TapTargetUtil.isActive(tapTarget) && applicationPref.shouldShowTipFor(tapTarget))
            return TapTargetUtil.buildDefault(context, primary, secondary, resource)
                    .setPromptStateChangeListener(defaultStateChangeListener)
                    .setFocalColour(CompatUtil.getColor(context, focalColour));
        return null;
    }

    /**
     * Get the prompt you want to display, highlighting a given a resource view
     * <br/>
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    public @Nullable MaterialTapTargetPrompt.Builder createTapTarget(@StringRes int primary, @StringRes int secondary, View resource) {
        if(applicationPref == null) {
            Log.e(toString(), "Did you forget to set the current application preferences?");
            return null;
        }
        if (!TapTargetUtil.isActive(tapTarget) && applicationPref.shouldShowTipFor(tapTarget))
            return TapTargetUtil.buildDefault(context, primary, secondary, resource)
                    .setPromptStateChangeListener(defaultStateChangeListener)
                    .setFocalColour(CompatUtil.getColor(context, focalColour));
        return null;
    }

    /**
     * Display hint, highlighting a given a resource id
     * <br/>
     *
     * @param resource Item that should be focused on by the application tip
     */
    public void showTapTarget(@IdRes int resource) {
        if(applicationPref == null) {
            Log.e(toString(), "Did you forget to set the current application preferences?");
            return;
        }
        if (!TapTargetUtil.isActive(tapTarget) && applicationPref.shouldShowTipFor(tapTarget))
            TapTargetUtil.buildDefault(context, resource)
                    .setPromptStateChangeListener(defaultStateChangeListener)
                    .setFocalColour(CompatUtil.getColor(context, focalColour))
                    .show();
    }

    /**
     * Display hint, highlighting a given a resource id, heading and subheading
     * <br/>
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    public void showTapTarget(@StringRes int primary, @StringRes int secondary, @IdRes int resource) {
        if(applicationPref == null) {
            Log.e(toString(), "Did you forget to set the current application preferences?");
            return;
        }
        if (!TapTargetUtil.isActive(tapTarget) && applicationPref.shouldShowTipFor(tapTarget))
            TapTargetUtil.buildDefault(context, primary, secondary, resource)
                    .setPromptStateChangeListener(defaultStateChangeListener)
                    .setFocalColour(CompatUtil.getColor(context, focalColour))
                    .show();
    }

    /**
     * Display hint, highlighting a given a resource view
     * <br/>
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    public void showTapTarget(@StringRes int primary, @StringRes int secondary, View resource) {
        if(applicationPref == null) {
            Log.e(toString(), "Did you forget to set the current application preferences?");
            return;
        }
        if (!TapTargetUtil.isActive(tapTarget) && applicationPref.shouldShowTipFor(tapTarget))
            TapTargetUtil.buildDefault(context, primary, secondary, resource)
                    .setPromptStateChangeListener(defaultStateChangeListener)
                    .setFocalColour(CompatUtil.getColor(context, focalColour))
                    .show();
    }

    private PromptStateChangeListener defaultStateChangeListener = new PromptStateChangeListener() {
        @Override
        public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
            switch (state) {
                case MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED:
                case MaterialTapTargetPrompt.STATE_FOCAL_PRESSED:
                    applicationPref.disableTipFor(tapTarget);
                    break;
                case MaterialTapTargetPrompt.STATE_DISMISSED:
                    TapTargetUtil.setActive(tapTarget, true);
                    break;
            }
            if (listener != null)
                listener.onPromptStateChanged(prompt, state);
        }
    };
}
