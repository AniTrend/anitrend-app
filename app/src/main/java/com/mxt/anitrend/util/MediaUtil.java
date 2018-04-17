package com.mxt.anitrend.util;

import com.annimon.stream.Stream;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrend;
import com.mxt.anitrend.model.entity.base.MediaBase;

import java.util.Collections;
import java.util.List;

/**
 * Created by max on 2018/02/23.
 * Helper class to handle series types
 */

public class MediaUtil {

    public static <T extends MediaBase> boolean isAnimeType(T series) {
        return (series != null && CompatUtil.equals(series.getType(), KeyUtil.ANIME));
    }

    public static <T extends MediaBase> boolean isMangaType(T series) {
        return (series != null && CompatUtil.equals(series.getType(), KeyUtil.MANGA));
    }

    public static boolean isIncrementLimitReached(MediaList model) {
        MediaBase mediaBase = model.getMedia();
        if(isAnimeType(mediaBase))
            return mediaBase.getEpisodes() == model.getProgress() && mediaBase.getEpisodes() != 0;
        return mediaBase.getChapters() == model.getProgress() && mediaBase.getChapters() != 0;
    }


    public static boolean isAllowedStatus(MediaList model) {
        MediaBase mediaBase = model.getMedia();
        return !CompatUtil.equals(mediaBase.getStatus(), KeyUtil.NOT_YET_RELEASED);
    }

    public static <T extends MediaBase> String getMediaTitle(T series) {
        return series.getTitle().getUserPreferred();
    }

    public static String getMediaListTitle(MediaList mediaList) {
        return getMediaTitle(mediaList.getMedia());
    }

    public static List<MediaBase> mapMediaTrend(List<MediaTrend> mediaTrends) {
        if(mediaTrends != null)
            return Stream.of(mediaTrends)
                    .map(MediaTrend::getMedia)
                    .toList();
        return Collections.emptyList();
    }

    public static List<MediaList> getAiringMedia(List<MediaList> mediaLists) {
        if(mediaLists != null)
            return Stream.of(mediaLists)
                    .filter(media -> CompatUtil.equals(media.getMedia().getStatus(), KeyUtil.RELEASING))
                    .toList();
        return Collections.emptyList();
    }
}
