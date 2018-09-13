package com.mxt.anitrend.base.custom.view.container;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class NotificationCardView extends CardViewBase {

    public NotificationCardView(@NonNull Context context) {
        super(context);
    }

    public NotificationCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NotificationCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onInit() {
        applyStyle(0);
    }
}
