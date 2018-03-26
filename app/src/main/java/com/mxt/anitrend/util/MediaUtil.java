package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.base.MediaBase;

import java.util.Objects;

/**
 * Created by max on 2018/02/23.
 * Helper class to handle series types
 */

public class MediaUtil {

    public static <T extends MediaBase> boolean isAnimeType(T series) {
        return (series != null && Objects.equals(series.getType(), KeyUtils.ANIME));
    }

    public static <T extends MediaBase> boolean isMangaType(T series) {
        return (series != null && Objects.equals(series.getType(), KeyUtils.MANGA));
    }

    public static boolean isIncrementLimitReached(MediaList model) {
        MediaBase mediaBase = model.getMedia();
        if(isAnimeType(mediaBase))
            return mediaBase.getEpisodes() == model.getProgress() && mediaBase.getEpisodes() != 0;
        return mediaBase.getChapters() == model.getProgress() && mediaBase.getChapters() != 0;
    }


    public static boolean isAllowedStatus(MediaList model) {
        MediaBase mediaBase = model.getMedia();
        return !Objects.equals(mediaBase.getStatus(), KeyUtils.NOT_YET_RELEASED);
    }

    public static <T extends MediaBase> String getMediaTitle(T series) {
        return series.getTitle().getUserPreferred();
    }

    public static String getMediaListTitle(MediaList mediaList) {
        return getMediaTitle(mediaList.getMedia());
    }
}
