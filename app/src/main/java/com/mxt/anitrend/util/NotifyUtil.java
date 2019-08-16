package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;
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
                              @ColorRes int backgroundColor, @KeyUtil.AlerterDuration long duration) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.INSTANCE.getDrawable(activity, icon, R.color.white))
                .setProgressColorInt(CompatUtil.INSTANCE.getColor(activity, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(duration == 0 ? KeyUtil.DURATION_SHORT : duration)
                .enableProgress(duration != 0)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, @StringRes int title, @StringRes int text, @DrawableRes int icon,
                              @ColorRes int backgroundColor, @KeyUtil.AlerterDuration long duration) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.INSTANCE.getDrawable(activity, icon, R.color.white))
                .setProgressColorInt(CompatUtil.INSTANCE.getColor(activity, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(duration == 0 ? KeyUtil.DURATION_SHORT : duration)
                .enableProgress(duration != 0)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, String title, String text, @DrawableRes int icon, @ColorRes int backgroundColor) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.INSTANCE.getDrawable(activity, icon, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtil.DURATION_SHORT)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, @StringRes int title, @StringRes int text, @DrawableRes int icon, @ColorRes int backgroundColor) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.INSTANCE.getDrawable(activity, icon, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtil.DURATION_SHORT)
                .show();
    }

    /**
     * Create an alert using the activity base
     */
    public static void createAlerter(FragmentActivity activity, @StringRes int title, @StringRes int text, @DrawableRes int icon, @ColorRes int backgroundColor, View.OnClickListener clickListener) {
        Alerter.create(activity).setTitle(title).setText(text)
                .setIcon(CompatUtil.INSTANCE.getDrawable(activity, icon, R.color.white))
                .setBackgroundColorRes(backgroundColor)
                .enableIconPulse(true).enableSwipeToDismiss()
                .enableVibration(true).setDuration(KeyUtil.DURATION_SHORT)
                .setOnClickListener(clickListener)
                .show();
    }

    /**
     * Create a custom toast
     */
    public static void createLoginToast(FragmentActivity context, User user) {
        Toast notification = new Toast(context);
        CustomAuthToastBinding binding = CustomAuthToastBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(context));
        binding.setModel(user);
        notification.setView(binding.getRoot());
        notification.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        notification.setDuration(Toast.LENGTH_LONG);
        notification.show();
    }

    public static Toast makeText(Context context, @StringRes int stringRes, @DrawableRes int drawableRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(context));
        binding.toastText.setText(context.getString(stringRes));
        binding.toastIcon.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(context, drawableRes));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.INSTANCE.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, @StringRes int stringRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(context));
        binding.toastText.setText(context.getString(stringRes));
        binding.toastIcon.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.INSTANCE.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, String stringRes, @DrawableRes int drawableRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(context));
        binding.toastText.setText(stringRes);
        binding.toastIcon.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(context, drawableRes));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.INSTANCE.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, String stringRes, int duration) {
        Toast toast = new Toast(context);
        CustomToastBinding binding = CustomToastBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(context));
        binding.toastText.setText(stringRes);
        binding.toastIcon.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp));
        toast.setView(binding.getRoot());
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, CompatUtil.INSTANCE.dipToPx(32));
        toast.setDuration(duration);
        return toast;
    }

    public static Snackbar make(View parent, String stringRes, int duration) {
        Snackbar snackbar = Snackbar.make(parent, stringRes, duration);
        View snackBarContainer = snackbar.getView();
        snackBarContainer.setBackgroundColor(CompatUtil.INSTANCE.getColorFromAttr(parent.getContext(), R.attr.colorPrimaryDark));
        TextView mainTextView = snackBarContainer.findViewById(R.id.snackbar_text);
        TextView actionTextView = snackBarContainer.findViewById(R.id.snackbar_action);
        mainTextView.setTextColor(CompatUtil.INSTANCE.getColorFromAttr(parent.getContext(), R.attr.titleColor));
        actionTextView.setTextColor(CompatUtil.INSTANCE.getColorFromAttr(parent.getContext(), R.attr.colorAccent));
        actionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return snackbar;
    }

    public static Snackbar make(View parent, @StringRes int stringRes, int duration) {
        Snackbar snackbar = Snackbar.make(parent, stringRes, duration);
        View snackBarContainer = snackbar.getView();
        snackBarContainer.setBackgroundColor(CompatUtil.INSTANCE.getColorFromAttr(parent.getContext(), R.attr.colorPrimaryDark));
        TextView mainTextView = snackBarContainer.findViewById(R.id.snackbar_text);
        TextView actionTextView = snackBarContainer.findViewById(R.id.snackbar_action);
        mainTextView.setTextColor(CompatUtil.INSTANCE.getColorFromAttr(parent.getContext(), R.attr.titleColor));
        actionTextView.setTextColor(CompatUtil.INSTANCE.getColorFromAttr(parent.getContext(), R.attr.colorAccent));
        actionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        return snackbar;
    }

    public static ProgressDialog createProgressDialog(Context context, @StringRes int stringRes) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(stringRes));
        return progressDialog;
    }
}
