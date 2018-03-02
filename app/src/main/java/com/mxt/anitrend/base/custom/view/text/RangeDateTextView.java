package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.util.AttributeSet;

import com.mxt.anitrend.util.DateUtil;

/**
 * Created by max on 2017/10/28.
 * Returns date formats such as started, starts, ended or ends
 */

public class RangeDateTextView extends SingleLineTextView {

    public RangeDateTextView(Context context) {
        super(context);
    }

    public RangeDateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RangeDateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter("startDate")
    public static void setStartDate(RangeDateTextView view, int fuzzyDate) {
        view.setText(String.format("%s: %s", DateUtil.getStartTitle(fuzzyDate), DateUtil.convertDate(fuzzyDate)));
    }

    @BindingAdapter("endDate")
    public static void setEndDate(RangeDateTextView view, int fuzzyDate) {
        view.setText(String.format("%s: %s", DateUtil.getEndTitle(fuzzyDate), DateUtil.convertDate(fuzzyDate)));
    }
}
