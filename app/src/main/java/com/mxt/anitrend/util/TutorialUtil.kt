package com.mxt.anitrend.util

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.base.custom.presenter.CommonPresenter
import timber.log.Timber
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.PromptStateChangeListener

/**
 * Helper class to display application
 * Example usage:
 *
 * new TutorialUtil().setContext(this)
 *     .setFocalColour(R.color.colorGrey600)
 *     .setTapTarget(KeyUtil.KEY_NOTIFICATION_TIP)
 *     .setSettings(getPresenter().getSettings())
 *     .createTapTarget(
 *         R.string.tip_notifications_title,
 *         R.string.tip_notifications_text,
 *         R.id.action_notification
 *     );
 */
class TutorialUtil {

    @KeyUtil.TapTargetType
    private var tapTarget: String? = null

    @ColorRes
    private var focalColour: Int = 0

    private var listener: PromptStateChangeListener? = null
    private var context: FragmentActivity? = null
    private var settings: Settings? = null
    private val tagName = TutorialUtil::class.java.simpleName

    /**
     * Optional. After the tip is dismissed, this helper class will automatically save
     * the state so that the tip doesn't get shown next time again.
     * @see #defaultStateChangeListener
     */
    fun setListener(listener: PromptStateChangeListener?): TutorialUtil {
        this.listener = listener
        return this
    }

    /**
     * Mandatory preference key for saving or showing the state of the tip
     *
     * @param tapTarget A type of string that represents the preference key for the target tip
     *                  @see KeyUtil.TapTargetType
     */
    fun setTapTarget(@KeyUtil.TapTargetType tapTarget: String): TutorialUtil {
        this.tapTarget = tapTarget
        return this
    }

    /**
     * Mandatory activity context used to find target views ids
     *
     * @param context Must be a valid FragmentActivity derivative
     *                @see FragmentActivity
     */
    fun setContext(context: FragmentActivity): TutorialUtil {
        this.context = context
        return this
    }

    /**
     * Mandatory color resource for the focal point of the tap target
     */
    fun setFocalColour(@ColorRes focalColour: Int): TutorialUtil {
        this.focalColour = focalColour
        return this
    }

    /**
     * Mandatory application prefs of the current fragment activity, it is wise to use
     * the presenters application preference object rather than creating a new one.
     *
     * @see CommonPresenter.getSettings
     */
    fun setSettings(settings: Settings): TutorialUtil {
        this.settings = settings
        return this
    }

    /**
     * Get the prompt you want to display, highlighting a given a resource id
     *
     * @param resource Item that should be focused on by the application tip
     */
    fun createTapTarget(@IdRes resource: Int): MaterialTapTargetPrompt.Builder? {
        val prefs = settings
        val activity = context
        val target = tapTarget
        if (prefs == null) {
            Timber.tag(tagName).i("Did you forget to set the current application preferences?")
            return null
        }
        if (activity != null && target != null && !TapTargetUtil.isActive(target) && prefs.shouldShowTipFor(target))
            return TapTargetUtil.buildDefault(activity, resource)
                .setPromptStateChangeListener(defaultStateChangeListener)
                .setFocalColour(CompatUtil.getColor(activity, focalColour))
        return null
    }

    /**
     * Get the prompt you want to display, highlighting a given a resource id, heading and subheading
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    fun createTapTarget(
        @StringRes primary: Int,
        @StringRes secondary: Int,
        @IdRes resource: Int
    ): MaterialTapTargetPrompt.Builder? {
        val prefs = settings
        val activity = context
        val target = tapTarget
        if (prefs == null) {
            Timber.tag(tagName).i("Did you forget to set the current application preferences?")
            return null
        }
        if (activity != null && target != null && !TapTargetUtil.isActive(target) && prefs.shouldShowTipFor(target))
            return TapTargetUtil.buildDefault(activity, primary, secondary, resource)
                .setPromptStateChangeListener(defaultStateChangeListener)
                .setFocalColour(CompatUtil.getColor(activity, focalColour))
        return null
    }

    /**
     * Get the prompt you want to display, highlighting a given a resource view
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    fun createTapTarget(
        @StringRes primary: Int,
        @StringRes secondary: Int,
        resource: View
    ): MaterialTapTargetPrompt.Builder? {
        val prefs = settings
        val activity = context
        val target = tapTarget
        if (prefs == null) {
            Timber.tag(tagName).i("Did you forget to set the current application preferences?")
            return null
        }
        if (activity != null && target != null && !TapTargetUtil.isActive(target) && prefs.shouldShowTipFor(target))
            return TapTargetUtil.buildDefault(activity, primary, secondary, resource)
                .setPromptStateChangeListener(defaultStateChangeListener)
                .setFocalColour(CompatUtil.getColor(activity, focalColour))
        return null
    }

    /**
     * Display hint, highlighting a given a resource id
     *
     * @param resource Item that should be focused on by the application tip
     */
    fun showTapTarget(@IdRes resource: Int) {
        val prefs = settings
        val activity = context
        val target = tapTarget
        if (prefs == null) {
            Timber.tag(tagName).i("Did you forget to set the current application preferences?")
            return
        }
        if (activity != null && target != null && !TapTargetUtil.isActive(target) && prefs.shouldShowTipFor(target))
            TapTargetUtil.buildDefault(activity, resource)
                .setPromptStateChangeListener(defaultStateChangeListener)
                .setFocalColour(CompatUtil.getColor(activity, focalColour))
                .show()
    }

    /**
     * Display hint, highlighting a given a resource id, heading and subheading
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    fun showTapTarget(@StringRes primary: Int, @StringRes secondary: Int, @IdRes resource: Int) {
        val prefs = settings
        val activity = context
        val target = tapTarget
        if (prefs == null) {
            Timber.tag(tagName).i("Did you forget to set the current application preferences?")
            return
        }
        if (activity != null && target != null && !TapTargetUtil.isActive(target) && prefs.shouldShowTipFor(target))
            TapTargetUtil.buildDefault(activity, primary, secondary, resource)
                .setPromptStateChangeListener(defaultStateChangeListener)
                .setFocalColour(CompatUtil.getColor(activity, focalColour))
                .show()
    }

    /**
     * Display hint, highlighting a given a resource view
     *
     * @param primary Heading for the tip that should be displayed
     * @param secondary Sub Heading for the tip that should be displayed
     * @param resource Item that should be focused on by the application tip
     */
    fun showTapTarget(@StringRes primary: Int, @StringRes secondary: Int, resource: View) {
        val prefs = settings
        val activity = context
        val target = tapTarget
        if (prefs == null) {
            Timber.tag(tagName).i("Did you forget to set the current application preferences?")
            return
        }
        if (activity != null && target != null && !TapTargetUtil.isActive(target) && prefs.shouldShowTipFor(target))
            TapTargetUtil.buildDefault(activity, primary, secondary, resource)
                .setPromptStateChangeListener(defaultStateChangeListener)
                .setFocalColour(CompatUtil.getColor(activity, focalColour))
                .show()
    }

    private val defaultStateChangeListener = PromptStateChangeListener { prompt, state ->
        val target = tapTarget
        when (state) {
            MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED,
            MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                if (target != null)
                    settings?.disableTipFor(target)
            }
            MaterialTapTargetPrompt.STATE_DISMISSED -> {
                if (target != null)
                    TapTargetUtil.setActive(target, true)
            }
        }
        listener?.onPromptStateChanged(prompt, state)
    }
}
