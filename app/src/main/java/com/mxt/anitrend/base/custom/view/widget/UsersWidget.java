package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;

public class UsersWidget extends SingleLineTextView implements CustomView {

    public UsersWidget(Context context) {
        super(context);
        onInit();
    }

    public UsersWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public UsersWidget(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawableTintAttr(getContext(),
                R.drawable.ic_people_grey_600_18dp, R.attr.colorAccent), null, null, null);
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
