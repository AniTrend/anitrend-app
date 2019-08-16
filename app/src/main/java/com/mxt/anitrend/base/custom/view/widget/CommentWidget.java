package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;

/**
 * Created by max on 2017/11/07.
 * Comment Widget
 */

public class CommentWidget extends SingleLineTextView implements CustomView {

    public CommentWidget(Context context) {
        super(context);
        onInit();
    }

    public CommentWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public CommentWidget(Context context, AttributeSet attrs, int defStyleAttr) {
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
        setCompoundDrawablesWithIntrinsicBounds(CompatUtil.INSTANCE.getDrawableTintAttr(getContext(), R.drawable.ic_mode_comment_grey_600_18dp,
                R.attr.colorAccent),null, null, null);
    }

    public void setReplyCount(int replyCount) {
        setText(WidgetPresenter.convertToText(replyCount));
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
