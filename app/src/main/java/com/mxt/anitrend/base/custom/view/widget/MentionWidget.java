package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;

public class MentionWidget extends AppCompatImageView implements CustomView {

    public MentionWidget(Context context) {
        super(context);
        onInit();
    }

    public MentionWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public MentionWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        final int padding = getResources().getDimensionPixelSize(R.dimen.spacing_small);
        setPadding(padding, padding, padding, padding);
        setImageDrawable(CompatUtil.INSTANCE.getDrawable(getContext(), R.drawable.ic_reply_blue_600_18dp));
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
