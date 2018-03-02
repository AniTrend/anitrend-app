package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.util.AttributeSet;

import com.mxt.anitrend.model.entity.general.Airing;
import com.mxt.anitrend.util.DateUtil;

import javax.annotation.Nullable;

/**
 * Created by max on 2017/10/27.
 * Shows information regarding airing
 */

public class AiringTextView extends SingleLineTextView {

    public AiringTextView(Context context) {
        super(context);
    }

    public AiringTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AiringTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        super.onInit();
    }

    @BindingAdapter("airingDate")
    public static void setAiring(AiringTextView view, @Nullable Airing airing) {
        if(airing != null) {
            view.setText(DateUtil.getNextEpDate(airing));
            view.setVisibility(VISIBLE);
        }
        else
            view.setVisibility(GONE);
    }
}
