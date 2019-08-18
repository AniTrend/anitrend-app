package com.mxt.anitrend.util

import android.app.ProgressDialog
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity

import com.google.android.material.snackbar.Snackbar
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.CustomAuthToastBinding
import com.mxt.anitrend.databinding.CustomToastBinding
import com.mxt.anitrend.extension.*
import com.mxt.anitrend.model.entity.anilist.User
import com.tapadoo.alerter.Alerter

/**
 * Created by max on 2017/11/04.
 * Utilities for notifications
 */

object NotifyUtil {

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, title: String, text: String, @DrawableRes icon: Int,
                      @ColorRes backgroundColor: Int, @KeyUtil.AlerterDuration duration: Long) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(activity.getCompatDrawable(icon, R.color.white)!!)
                .setProgressColorInt(activity.getCompatColor(R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(if (duration == 0L) KeyUtil.DURATION_SHORT else duration)
                .enableProgress(duration != 0L)
                .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, @StringRes title: Int, @StringRes text: Int, @DrawableRes icon: Int,
                      @ColorRes backgroundColor: Int, @KeyUtil.AlerterDuration duration: Long) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(activity.getCompatDrawable(icon, R.color.white)!!)
                .setProgressColorInt(activity.getCompatColor(R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(if (duration == 0L) KeyUtil.DURATION_SHORT else duration)
                .enableProgress(duration != 0L)
                .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, title: String, text: String, @DrawableRes icon: Int, @ColorRes backgroundColor: Int) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(activity.getCompatDrawable(icon, R.color.white)!!)
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtil.DURATION_SHORT)
                .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, @StringRes title: Int, @StringRes text: Int, @DrawableRes icon: Int, @ColorRes backgroundColor: Int) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(activity.getCompatDrawable(icon, R.color.white)!!)
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtil.DURATION_SHORT)
                .show()
    }

    /**
     * Create an alert using the activity base
     */
    fun createAlerter(activity: FragmentActivity, @StringRes title: Int, @StringRes text: Int, @DrawableRes icon: Int, @ColorRes backgroundColor: Int, clickListener: View.OnClickListener) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(activity.getCompatDrawable(icon, R.color.white)!!)
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtil.DURATION_SHORT)
                .setOnClickListener(clickListener)
                .show()
    }

    /**
     * Create a custom toast
     */
    fun createLoginToast(context: FragmentActivity, user: User) {
        val notification = Toast(context)
        val binding = CustomAuthToastBinding.inflate(context.layoutInflater)
        binding.model = user
        notification.view = binding.root
        notification.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        notification.duration = Toast.LENGTH_LONG
        notification.show()
    }

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

    @Suppress("DEPRECATION")
    fun createProgressDialog(context: Context, @StringRes stringRes: Int): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.getString(stringRes))
        return progressDialog
    }
}
