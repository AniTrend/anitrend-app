package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.util.AttributeSet;

import com.mxt.anitrend.R;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MediaUtil;

import java.util.Locale;
import java.util.Objects;

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

    public void setSeriesModel(MediaList mediaList, boolean isCurrentUser) {
        MediaBase model = mediaList.getMedia();
        if (MediaUtil.isAnimeType(model)) {
            if(CompatUtil.equals(model.getStatus(), KeyUtil.NOT_YET_RELEASED))
                setText(R.string.TBA);
            else {
                if (isCurrentUser && !MediaUtil.isIncrementLimitReached(mediaList))
                    setText(String.format(Locale.getDefault(), "%s/%s +", mediaList.getProgress(),
                            model.getEpisodes() < 1 ? "?" : model.getEpisodes()));
                else
                    setText(String.format(Locale.getDefault(), "%s/%s", mediaList.getProgress(),
                            model.getEpisodes() < 1 ? "?" : model.getEpisodes()));
            }
        } else if (MediaUtil.isMangaType(model)) {
            if(CompatUtil.equals(model.getStatus(), KeyUtil.NOT_YET_RELEASED))
                setText(R.string.TBA);
            else {
                if (isCurrentUser && !MediaUtil.isIncrementLimitReached(mediaList))
                    setText(String.format(Locale.getDefault(), "%s/%s +", mediaList.getProgress(),
                            model.getChapters() < 1 ? "?" : model.getChapters()));
                else
                    setText(String.format(Locale.getDefault(), "%s/%s", mediaList.getProgress(),
                            model.getChapters() < 1 ? "?" : model.getChapters()));
            }
        }
    }
}
