package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import androidx.databinding.BindingAdapter;

import android.util.AttributeSet;
import com.mxt.anitrend.util.CompatUtil;
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

    @BindingAdapter({"isSpoiler"})
    public static void setIsSpoiler(SingleLineTextView view, Boolean isSpoiler) {
        if (isSpoiler) view.setTextColor(CompatUtil.INSTANCE.getColor(view.getContext(), R.color.colorStateOrange));
    }
}
