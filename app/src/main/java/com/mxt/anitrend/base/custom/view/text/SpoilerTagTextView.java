package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.mxt.anitrend.R;

public class SpoilerTagTextView extends SingleLineTextView {

    public SpoilerTagTextView(Context context) {
        super(context);
    }

    public SpoilerTagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpoilerTagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter("app:isSpoiler")
    public static void setIsSpoiler(SingleLineTextView view, Boolean isSpoiler) {
        if (isSpoiler) view.setTextColor(CompatUtil.getColor(view.getContext(), R.color.colorStateOrange));
    }
}
