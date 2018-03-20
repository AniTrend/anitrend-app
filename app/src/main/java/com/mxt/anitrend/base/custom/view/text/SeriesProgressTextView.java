package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.general.MediaList;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.SeriesUtil;

import java.util.Locale;

/**
 * Created by max on 2017/10/29.
 * Episode Text View with
 */

public class SeriesProgressTextView extends SingleLineTextView {

    public SeriesProgressTextView(Context context) {
        super(context);
    }

    public SeriesProgressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeriesProgressTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSeriesModel(MediaList seriesModel, boolean isCurrentUser) {
        MediaBase model = SeriesUtil.getSeriesModel(seriesModel);
        if (SeriesUtil.isAnimeType(model)) {
            if(TextUtils.isEmpty(model.getAiring_status()) || model.getAiring_status().equals(KeyUtils.key_not_yet_aired))
                setText(R.string.TBA);
            else {
                if (isCurrentUser && !SeriesUtil.isIncrementLimitReached(seriesModel))
                    setText(String.format(Locale.getDefault(), "%s/%s +", seriesModel.getEpisodes_watched(),
                            seriesModel.getAnime().getTotal_episodes() < 1 ? "?" : seriesModel.getAnime().getTotal_episodes()));
                else
                    setText(String.format(Locale.getDefault(), "%s/%s", seriesModel.getEpisodes_watched(),
                            seriesModel.getAnime().getTotal_episodes() < 1 ? "?" : seriesModel.getAnime().getTotal_episodes()));
            }
        } else if (SeriesUtil.isMangaType(model)) {
            if(TextUtils.isEmpty(model.getPublishing_status()) || model.getPublishing_status().equals(KeyUtils.key_not_yet_published))
                setText(R.string.TBA);
            else {
                if (isCurrentUser && !SeriesUtil.isIncrementLimitReached(seriesModel))
                    setText(String.format(Locale.getDefault(), "%s/%s +", seriesModel.getChapters_read(),
                            seriesModel.getManga().getTotal_chapters() < 1 ? "?" : seriesModel.getManga().getTotal_chapters()));
                else
                    setText(String.format(Locale.getDefault(), "%s/%s", seriesModel.getChapters_read(),
                            seriesModel.getManga().getTotal_chapters() < 1 ? "?" : seriesModel.getManga().getTotal_chapters()));
            }
        }
    }
}
