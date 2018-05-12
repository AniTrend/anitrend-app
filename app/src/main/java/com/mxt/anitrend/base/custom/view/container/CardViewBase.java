package com.mxt.anitrend.base.custom.view.container;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;

/**
 * Created by max on 2017/11/30.
 * A base custom card view with pre applied styles
 *
 * app:contentPadding="@dimen/xl_margin"
 * app:cardUseCompatPadding="true"
 * app:cardPreventCornerOverlap="true"
 * app:cardCornerRadius="@dimen/xs_margin"
 * app:cardBackgroundColor="?attr/cardColor"
 */

public class CardViewBase extends CardView implements CustomView {

    public CardViewBase(@NonNull Context context) {
        super(context);
        onInit();
    }

    public CardViewBase(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public CardViewBase(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        applyStyle(getResources().getDimensionPixelSize(R.dimen.xl_margin));
    }

    protected void applyStyle(int contentPadding) {
        setRadius(getResources().getDimensionPixelSize(R.dimen.sm_margin));
        setUseCompatPadding(true);
        setPreventCornerOverlap(false);
        setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding);
        setCardBackgroundColor(CompatUtil.getColorFromAttr(getContext(), R.attr.cardColor));
        requestLayout();
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
