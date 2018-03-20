package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.general.MediaList;

/**
 * Created by max on 2018/02/23.
 * Helper class to handle series types
 */

public class SeriesUtil {

    public static <T extends MediaBase> boolean isAnimeType(T series) {
        return (series != null && series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]));
    }

    public static <T extends MediaBase> boolean isMangaType(T series) {
        return (series != null && series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.MANGA]));
    }

    public static MediaBase getSeriesModel(MediaList model) {
        return model.getAnime() != null ? model.getAnime() : model.getManga();
    }

    public static boolean isIncrementLimitReached(MediaList model) {
        MediaBase mediaBase = getSeriesModel(model);
        if(isAnimeType(mediaBase))
            return mediaBase.getTotal_episodes() == model.getEpisodes_watched() && mediaBase.getTotal_episodes() != 0;
        return mediaBase.getTotal_chapters() == model.getChapters_read() && mediaBase.getTotal_chapters() != 0;
    }

    public static boolean isAllowedStatus(MediaList model) {
        MediaBase mediaBase = getSeriesModel(model);
        if(SeriesUtil.isAnimeType(mediaBase))
            return !mediaBase.getAiring_status().equals(KeyUtils.key_not_yet_aired);
        return !mediaBase.getPublishing_status().equals(KeyUtils.key_not_yet_published);
    }

    public static <T extends MediaBase> String getSeriesTitle(T series, int languagePrefs) {
        switch (languagePrefs) {
            case KeyUtils.LANGUAGE_ENGLISH:
                return (series.getTitle_english());
            case KeyUtils.LANGUAGE_JAPANESE:
                return (series.getTitle_japanese());
            default:
                return (series.getTitle_romaji());
        }
    }

    public static String getSeriesTitle(MediaList series, int languagePrefs) {
        return getSeriesTitle(getSeriesModel(series), languagePrefs);
    }
}
