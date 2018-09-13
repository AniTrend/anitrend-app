package com.mxt.anitrend.base.custom.view.container;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.CompatUtil;

public class LoginCardView extends CardViewBase {


    public LoginCardView(@NonNull Context context) {
        super(context);
    }

    public LoginCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        applyStyle(getResources().getDimensionPixelSize(R.dimen.md_margin));
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
