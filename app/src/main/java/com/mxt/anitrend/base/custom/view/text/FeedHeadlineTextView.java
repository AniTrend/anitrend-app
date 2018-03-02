package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.UserActivity;

import java.util.Locale;

/**
 * Created by max on 2017/11/13.
 * Feeds including progress & status
 */

public class FeedHeadlineTextView extends AppCompatTextView implements CustomView {

    public FeedHeadlineTextView(Context context) {
        super(context);
        onInit();
    }

    public FeedHeadlineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public FeedHeadlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.heading_text_size));
        setEllipsize(TextUtils.TruncateAt.END);
        setMaxLines(2);
    }

    @BindingAdapter("feedHeadline")
    public static void setHeadline(FeedHeadlineTextView headline, UserActivity model) {
        if (TextUtils.isEmpty(model.getValue()))
            headline.setText(String.format(Locale.getDefault(), "%s: %s", model.getStatus(), model.getSeries().getTitle_romaji()));
        else
            headline.setText(String.format(Locale.getDefault(), "%s %s of: %s", model.getStatus(), model.getValue(), model.getSeries().getTitle_romaji()));
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
