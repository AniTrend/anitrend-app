package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.mxt.anitrend.base.interfaces.view.CustomView;

/**
 * Created by max on 2017/06/24.
 * Single line text view widget
 */

public class SingleLineTextView  extends AppCompatTextView implements CustomView {

    public SingleLineTextView(Context context) {
        super(context);
        onInit();
    }

    public SingleLineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public SingleLineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
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