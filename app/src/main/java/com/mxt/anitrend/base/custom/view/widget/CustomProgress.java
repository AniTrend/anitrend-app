package com.mxt.anitrend.base.custom.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;

/**
 * Created by max on 2017/07/01.
 * Custom progressbar
 */

public class CustomProgress extends ProgressBar implements CustomView {

    private PorterDuffColorFilter mColorFilter;

    /**
     * Create a new progress bar with range 0...100 and initial progress of 0.
     *
     * @param context the application environment
     */
    public CustomProgress(Context context) {
        super(context);
        onInit();
    }

    public CustomProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public CustomProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomProgress(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        mColorFilter = new PorterDuffColorFilter(CompatUtil.INSTANCE.getColorFromAttr(getContext(), R.attr.colorAccent), PorterDuff.Mode.SRC_IN);
        applyColorFilter(getProgressDrawable());
        applyColorFilter(getIndeterminateDrawable());
    }

    private void applyColorFilter(@Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && drawable != null)
            drawable.setColorFilter(mColorFilter);
    }

    @Override
    public void setProgressDrawable(Drawable drawable) {
        applyColorFilter(drawable);
        super.setProgressDrawable(drawable);
    }

    @Override
    public void setIndeterminateDrawable(Drawable drawable) {
        applyColorFilter(drawable);
        super.setIndeterminateDrawable(drawable);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
