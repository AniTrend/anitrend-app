package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.mxt.anitrend.R;

public class SpoilerTagTextView extends SingleLineTextView {

    public SpoilerTagTextView(Context context) {
        super(context);
        onInit();
    }

    public SpoilerTagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public SpoilerTagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @BindingAdapter("app:isSpoiler")
    public static void setIsSpoiler(SingleLineTextView view, Boolean isSpoiler) {
        if (isSpoiler) view.setTextColor(view.getResources().getColor(R.color.colorStateOrange));
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.END);
    }
}
