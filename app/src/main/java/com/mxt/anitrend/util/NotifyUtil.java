package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.databinding.CustomAuthToastBinding;
import com.mxt.anitrend.databinding.CustomToastBinding;
import com.mxt.anitrend.model.entity.anilist.User;
import com.tapadoo.alerter.Alerter;

/**
 * Created by max on 2017/11/04.
 * Utilities for notifications
 */

public class NotifyUtil {

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, String title, String text, @DrawableRes int icon,
                              @ColorRes int backgroundColor, @KeyUtils.AlerterDuration long duration) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.getDrawable(activity, icon, R.color.white))
                .setProgressColorInt(CompatUtil.getColor(activity, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(duration == 0 ? KeyUtils.DURATION_SHORT : duration)
                .enableProgress(duration != 0)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, @StringRes int title, @StringRes int text, @DrawableRes int icon,
                              @ColorRes int backgroundColor, @KeyUtils.AlerterDuration long duration) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.getDrawable(activity, icon, R.color.white))
                .setProgressColorInt(CompatUtil.getColor(activity, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(duration == 0 ? KeyUtils.DURATION_SHORT : duration)
                .enableProgress(duration != 0)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, String title, String text, @DrawableRes int icon, @ColorRes int backgroundColor) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.getDrawable(activity, icon, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtils.DURATION_SHORT)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, @StringRes int title, @StringRes int text, @DrawableRes int icon, @ColorRes int backgroundColor) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.getDrawable(activity, icon, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtils.DURATION_SHORT)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, @StringRes int title, @StringRes int text, @DrawableRes int icon, @ColorRes int backgroundColor, View.OnClickListener clickListener) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.getDrawable(activity, icon, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtils.DURATION_SHORT)
                .setOnClickListener(clickListener)
                .show();
    }

    /**
     * Create a custom toast
     */
    public static void createLoginToast(FragmentActivity context, User user) {
        Toast notification = new Toast(context);
        CustomAuthToastBinding binding = CustomAuthToastBinding.inflate(CompatUtil.getLayoutInflater(context));
        binding.setModel(user);
        notification.setView(binding.getRoot());
        notification.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        notification.setDuration(Toast.LENGTH_LONG);
        notification.show();
    }

    public static Toast makeText(Context context, @StringRes int stringRes, @DrawableRes int drawableRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.getLayoutInflater(context));
        binding.toastText.setText(context.getString(stringRes));
        binding.toastIcon.setImageDrawable(CompatUtil.getTintedDrawable(context, drawableRes));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, @StringRes int stringRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.getLayoutInflater(context));
        binding.toastText.setText(context.getString(stringRes));
        binding.toastIcon.setImageDrawable(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, String stringRes, @DrawableRes int drawableRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.getLayoutInflater(context));
        binding.toastText.setText(stringRes);
        binding.toastIcon.setImageDrawable(CompatUtil.getTintedDrawable(context, drawableRes));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, String stringRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.getLayoutInflater(context));
        binding.toastText.setText(stringRes);
        binding.toastIcon.setImageDrawable(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Snackbar make(View parent, String stringRes, int duration) {
        Snackbar snackbar = Snackbar.make(parent, stringRes, duration);
        View snackBarContainer = snackbar.getView();
        snackBarContainer.setBackgroundColor(CompatUtil.getColorFromAttr(parent.getContext(), R.attr.colorPrimaryDark));
        TextView mainTextView = snackBarContainer.findViewById(android.support.design.R.id.snackbar_text);
        TextView actionTextView = snackBarContainer.findViewById(android.support.design.R.id.snackbar_action);
        mainTextView.setTextColor(CompatUtil.getColorFromAttr(parent.getContext(), R.attr.titleColor));
        actionTextView.setTextColor(CompatUtil.getColorFromAttr(parent.getContext(), R.attr.colorAccent));
        actionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return snackbar;
    }

    public static Snackbar make(View parent, @StringRes int stringRes, int duration) {
        Snackbar snackbar = Snackbar.make(parent, stringRes, duration);
        View snackBarContainer = snackbar.getView();
        snackBarContainer.setBackgroundColor(CompatUtil.getColorFromAttr(parent.getContext(), R.attr.colorPrimaryDark));
        TextView mainTextView = snackBarContainer.findViewById(android.support.design.R.id.snackbar_text);
        TextView actionTextView = snackBarContainer.findViewById(android.support.design.R.id.snackbar_action);
        mainTextView.setTextColor(CompatUtil.getColorFromAttr(parent.getContext(), R.attr.titleColor));
        actionTextView.setTextColor(CompatUtil.getColorFromAttr(parent.getContext(), R.attr.colorAccent));
        actionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return snackbar;
    }

    public static ProgressDialog createProgressDialog(Context context, @StringRes int stringRes) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(stringRes));
        return progressDialog;
    }
}
