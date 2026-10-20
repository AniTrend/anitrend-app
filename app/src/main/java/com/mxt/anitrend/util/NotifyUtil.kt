package com.mxt.anitrend.util

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.*
// Alerter library removed; replaced with Material3 Snackbar-based implementation

/**
 * Created by max on 2017/11/04.
 * Utilities for notifications
 */

object NotifyUtil {

    /**
     * Translate notification duration constants to Snackbar duration constants.
     */
    private fun translateAlerterDuration(@KeyUtil.NotificationDuration duration: Long): Int = when (duration) {
        0L, KeyUtil.DURATION_SHORT -> Snackbar.LENGTH_SHORT
        KeyUtil.DURATION_MEDIUM -> Snackbar.LENGTH_LONG
        KeyUtil.DURATION_LONG -> Snackbar.LENGTH_INDEFINITE
        else -> Snackbar.LENGTH_SHORT
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(
        activity: FragmentActivity,
        title: String,
        text: String,
        @DrawableRes icon: Int,
        @ColorRes backgroundColor: Int,
        @KeyUtil.NotificationDuration duration: Long,
    ) {
        val container = activity.findViewById<View>(android.R.id.content) ?: return
        Snackbar.make(container, "$title: $text", translateAlerterDuration(duration))
            .setBackgroundTint(activity.getCompatColor(backgroundColor))
            .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(
        activity: FragmentActivity,
        @StringRes title: Int,
        @StringRes text: Int,
        @DrawableRes icon: Int,
        @ColorRes backgroundColor: Int,
        @KeyUtil.NotificationDuration duration: Long,
    ) {
        val container = activity.findViewById<View>(android.R.id.content) ?: return
        val message = "${activity.getString(title)}: ${activity.getString(text)}"
        Snackbar.make(container, message, translateAlerterDuration(duration))
            .setBackgroundTint(activity.getCompatColor(backgroundColor))
            .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, title: String, text: String, @DrawableRes icon: Int, @ColorRes backgroundColor: Int) {
        val container = activity.findViewById<View>(android.R.id.content) ?: return
        Snackbar.make(container, "$title: $text", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(activity.getCompatColor(backgroundColor))
            .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, @StringRes title: Int, @StringRes text: Int, @DrawableRes icon: Int, @ColorRes backgroundColor: Int) {
        val container = activity.findViewById<View>(android.R.id.content) ?: return
        val message = "${activity.getString(title)}: ${activity.getString(text)}"
        Snackbar.make(container, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(activity.getCompatColor(backgroundColor))
            .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, @StringRes title: Int, @StringRes text: Int, @DrawableRes icon: Int, @ColorRes backgroundColor: Int, clickListener: View.OnClickListener) {
        val container = activity.findViewById<View>(android.R.id.content) ?: return
        val message = "${activity.getString(title)}: ${activity.getString(text)}"
        Snackbar.make(container, message, Snackbar.LENGTH_SHORT)
            .setAction(R.string.Close, clickListener)
            .setBackgroundTint(activity.getCompatColor(backgroundColor))
            .show()
    }

    fun makeText(context: Context, @StringRes stringRes: Int, duration: Int): Toast = Toast.makeText(context, stringRes, duration)

    fun makeText(context: Context, stringRes: String, duration: Int): Toast = Toast.makeText(context, stringRes, duration)
}
