package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.util.AttributeSet;

import androidx.databinding.BindingAdapter;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.CompatUtil;

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

    @BindingAdapter({"isSpoiler"})
    public static void setIsSpoiler(SingleLineTextView view, Boolean isSpoiler) {
        if (isSpoiler) view.setTextColor(CompatUtil.INSTANCE.getColor(view.getContext(), R.color.colorStateOrange));
    }
}
