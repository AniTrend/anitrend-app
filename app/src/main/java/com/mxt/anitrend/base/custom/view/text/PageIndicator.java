package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;

import java.util.Locale;

/**
 * Created by max on 2017/11/25.
 */

public class PageIndicator extends AppCompatTextView implements CustomView {

    private int maximum;

    public PageIndicator(Context context) {
        super(context);
        onInit();
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        int padding = CompatUtil.INSTANCE.dipToPx(8);

        setTextColor(CompatUtil.INSTANCE.getColor(getContext(), R.color.colorTextLight));
        setBackground(CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.bubble_background));

        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        setPadding(padding, padding, padding, padding);
        setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public void setCurrentPosition(int index) {
        setText(String.format(Locale.getDefault(), "%d / %d", index, maximum));
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
