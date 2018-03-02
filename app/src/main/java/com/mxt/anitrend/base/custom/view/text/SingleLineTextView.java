package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.content.res.AssetManager;
import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.presenter.base.BasePresenter;

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