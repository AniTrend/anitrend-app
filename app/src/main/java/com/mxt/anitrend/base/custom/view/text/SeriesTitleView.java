package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.util.AttributeSet;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.Media;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2017/11/13.
 * Custom text view to display appropriate
 * series tittle according to user preferences
 */

public class SeriesTitleView extends SingleLineTextView implements CustomView {

    public SeriesTitleView(Context context) {
        super(context);
    }

    public SeriesTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeriesTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        super.onInit();
    }

    public void setTitle(MediaBase mediaBase) {
        setText(mediaBase.getTitle().getUserPreferred());
    }

    public void setTitle(MediaList mediaList) {
        setTitle(mediaList.getMedia());
    }

    public void setTitle(Review review) {
        setTitle(review.getMedia());
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
