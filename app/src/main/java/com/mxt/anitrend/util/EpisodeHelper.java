package com.mxt.anitrend.util;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;

import java.util.List;

/**
 * Created by max on 2017/11/04.
 * Episode list helper that check for crunchy feeds
 */

public class EpisodeHelper {

    public static String episodeSupport(List<ExternalLink> links) {
        for (ExternalLink link : links)
            if(link.getUrl().startsWith(BuildConfig.CRUNCHY_LINK))
                return linkStripper(link.getUrl()+".rss");
            else if(link.getUrl().startsWith(BuildConfig.FEEDS_LINK))
                return link.getUrl();
        return null;
    }

    private static String linkStripper(String link) {
        return link.replace(BuildConfig.CRUNCHY_LINK, "");
    }

    /**
     * Gets the series title without description
     */
    public static String getActualTile(String episodeTitle) {
        return episodeTitle.replaceAll("((.- Episode)|(.Season)).*", "");
    }
}
