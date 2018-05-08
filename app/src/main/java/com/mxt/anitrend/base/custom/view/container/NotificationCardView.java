package com.mxt.anitrend.base.custom.view.container;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.CompatUtil;

public class NotificationCardView extends CardViewBase {
    public NotificationCardView(@NonNull Context context) {
        super(context);
        onInit();
    }

    public NotificationCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public NotificationCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @Override
    public void onInit() {
        setRadius(getResources().getDimensionPixelSize(R.dimen.sm_margin));
        setUseCompatPadding(true);
        setPreventCornerOverlap(false);
        int contentPadding = 0;
        setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding);
        setCardBackgroundColor(CompatUtil.getColorFromAttr(getContext(), R.attr.cardColor));
        requestLayout();
    }
}
