package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.general.SeriesList;

/**
 * Created by max on 2018/02/23.
 * Helper class to handle series types
 */

public class SeriesUtil {

    public static <T extends SeriesBase> boolean isAnimeType(T series) {
        return (series != null && series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]));
    }

    public static <T extends SeriesBase> boolean isMangaType(T series) {
        return (series != null && series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.MANGA]));
    }

    public static SeriesBase getSeriesModel(SeriesList model) {
        return model.getAnime() != null ? model.getAnime() : model.getManga();
    }

    public static boolean isIncrementLimitReached(SeriesList model) {
        SeriesBase seriesBase = getSeriesModel(model);
        if(isAnimeType(seriesBase))
            return seriesBase.getTotal_episodes() == model.getEpisodes_watched() && seriesBase.getTotal_episodes() != 0;
        return seriesBase.getTotal_chapters() == model.getChapters_read() && seriesBase.getTotal_chapters() != 0;
    }

    public static boolean isAllowedStatus(SeriesList model) {
        SeriesBase seriesBase = getSeriesModel(model);
        if(SeriesUtil.isAnimeType(seriesBase))
            return !seriesBase.getAiring_status().equals(KeyUtils.key_not_yet_aired);
        return !seriesBase.getPublishing_status().equals(KeyUtils.key_not_yet_published);
    }

    public static <T extends SeriesBase> String getSeriesTitle(T series, int languagePrefs) {
        switch (languagePrefs) {
            case KeyUtils.LANGUAGE_ENGLISH:
                return (series.getTitle_english());
            case KeyUtils.LANGUAGE_JAPANESE:
                return (series.getTitle_japanese());
            default:
                return (series.getTitle_romaji());
        }
    }

    public static String getSeriesTitle(SeriesList series, int languagePrefs) {
        return getSeriesTitle(getSeriesModel(series), languagePrefs);
    }
}
