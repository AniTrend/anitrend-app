package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.util.AttributeSet;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.model.entity.anilist.Review;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.general.SeriesList;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2017/11/13.
 * Custom text view to display appropriate
 * series tittle according to user preferences
 */

public class SeriesTitleView extends SingleLineTextView implements CustomView {

    private BasePresenter presenter;
    private boolean isAuthenticated;

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
        presenter = new BasePresenter(getContext());
        isAuthenticated = presenter.getApplicationPref().isAuthenticated();
    }

    public void setTitle(Series series) {
        switch (getLanguagePreference()) {
            case KeyUtils.LANGUAGE_ENGLISH:
                setText(series.getTitle_english());
                break;
            case KeyUtils.LANGUAGE_JAPANESE:
                setText(series.getTitle_japanese());
                break;
            case KeyUtils.LANGUAGE_ROMAJI:
                setText(series.getTitle_romaji());
                break;
        }
    }

    public void setTitle(SeriesBase series) {
        switch (getLanguagePreference()) {
            case KeyUtils.LANGUAGE_ENGLISH:
                setText(series.getTitle_english());
                break;
            case KeyUtils.LANGUAGE_JAPANESE:
                setText(series.getTitle_japanese());
                break;
            case KeyUtils.LANGUAGE_ROMAJI:
                setText(series.getTitle_romaji());
                break;
        }
    }

    public void setTitle(SeriesList series) {
        if(series.getAnime() != null)
            setTitle(series.getAnime());
        else if(series.getManga() != null)
            setTitle(series.getManga());
    }

    public void setTitle(Review series) {
        if(series.getAnime() != null)
            setTitle(series.getAnime());
        else if(series.getManga() != null)
            setTitle(series.getManga());
    }

    private @KeyUtils.LanguageType int getLanguagePreference() {
        if(isAuthenticated)
            switch (presenter.getDatabase().getCurrentUser().getTitle_language()) {
                case KeyUtils.key_language_english:
                    return KeyUtils.LANGUAGE_ENGLISH;
                case KeyUtils.key_language_romaji:
                    return KeyUtils.LANGUAGE_ROMAJI;
                case KeyUtils.key_language_japanese:
                    return KeyUtils.LANGUAGE_JAPANESE;
            }
        return KeyUtils.LANGUAGE_ROMAJI;
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
