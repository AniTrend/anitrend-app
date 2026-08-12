package com.mxt.anitrend.util

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.CustomToastBinding
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

    @Suppress("DEPRECATION")
    fun makeText(context: Context, @StringRes stringRes: Int, @DrawableRes drawableRes: Int, duration: Int): Toast {
        val toast = Toast(context)
        val binding = CustomToastBinding.inflate(context.getLayoutInflater())
        binding.toastText.text = context.getString(stringRes)
        binding.toastIcon.setImageDrawable(context.getCompatTintedDrawable(drawableRes))
        toast.view = binding.root
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32f))
        toast.duration = duration
        return toast
    }

    @Suppress("DEPRECATION")
    fun makeText(context: Context, @StringRes stringRes: Int, duration: Int): Toast {
        val toast = Toast(context)
        val binding = CustomToastBinding.inflate(context.getLayoutInflater())
        binding.toastText.text = context.getString(stringRes)
        binding.toastIcon.setImageDrawable(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp))
        toast.view = binding.root
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32f))
        toast.duration = duration
        return toast
    }

    @Suppress("DEPRECATION")
    fun makeText(context: Context, stringRes: String, @DrawableRes drawableRes: Int, duration: Int): Toast {
        val toast = Toast(context)
        val binding = CustomToastBinding.inflate(context.getLayoutInflater())
        binding.toastText.text = stringRes
        binding.toastIcon.setImageDrawable(context.getCompatTintedDrawable(drawableRes))
        toast.view = binding.root
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32f))
        toast.duration = duration
        return toast
    }

    @Suppress("DEPRECATION")
    fun makeText(context: Context, stringRes: String, duration: Int): Toast {
        val toast = Toast(context)
        val binding = CustomToastBinding.inflate(context.getLayoutInflater())
        binding.toastText.text = stringRes
        binding.toastIcon.setImageDrawable(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp))
        toast.view = binding.root
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32f))
        toast.duration = duration
        return toast
    }

    fun make(parent: View, stringRes: String, duration: Int): Snackbar {
        val snackbar = Snackbar.make(parent, stringRes, duration)
        val snackBarContainer = snackbar.view
        snackBarContainer.setBackgroundColor(parent.context.getCompatColorAttr(R.attr.colorPrimaryDark))
        val mainTextView = snackBarContainer.findViewById<TextView>(R.id.snackbar_text)
        val actionTextView = snackBarContainer.findViewById<TextView>(R.id.snackbar_action)
        mainTextView.setTextColor(parent.context.getCompatColorAttr(R.attr.titleColor))
        actionTextView.setTextColor(parent.context.getCompatColorAttr(R.attr.colorAccent))
        actionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        return snackbar
    }

    fun make(parent: View, @StringRes stringRes: Int, duration: Int): Snackbar {
        val snackbar = Snackbar.make(parent, stringRes, duration)
        val snackBarContainer = snackbar.view
        snackBarContainer.setBackgroundColor(parent.context.getCompatColorAttr(R.attr.colorPrimaryDark))
        val mainTextView = snackBarContainer.findViewById<TextView>(R.id.snackbar_text)
        val actionTextView = snackBarContainer.findViewById<TextView>(R.id.snackbar_action)
        mainTextView.setTextColor(parent.context.getCompatColorAttr(R.attr.titleColor))
        actionTextView.setTextColor(parent.context.getCompatColorAttr(R.attr.colorAccent))
        actionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        return snackbar
    }

    fun createProgressDialog(context: Context, @StringRes stringRes: Int): AlertDialog {
        val progressLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(
                CompatUtil.dipToPx(24f),
                CompatUtil.dipToPx(16f),
                CompatUtil.dipToPx(24f),
                CompatUtil.dipToPx(16f),
            )
            addView(
                CircularProgressIndicator(context).apply {
                    isIndeterminate = true
                    indicatorSize = CompatUtil.dipToPx(14f)
                },
            )
            addView(
                Space(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        CompatUtil.dipToPx(16f),
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
                },
            )
            addView(
                MaterialTextView(context).apply {
                    text = context.getString(stringRes)
                    setPadding(CompatUtil.dipToPx(16f), 0, 0, 0)
                    gravity = Gravity.CENTER_VERTICAL
                },
            )
        }
        return DialogUtil.createDefaultDialog(context)
            .setView(progressLayout)
            .setCancelable(false)
            .show() as AlertDialog
    }
}
