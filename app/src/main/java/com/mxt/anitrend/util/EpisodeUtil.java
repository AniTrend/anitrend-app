package com.mxt.anitrend.util;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;

import java.util.List;

/**
 * Created by max on 2017/11/04.
 * Episode list helper that check for crunchy feeds
 */

public class EpisodeUtil {

    private static final String HTTP_SECURE = "https", HTTP_STANDARD = "http";
    private static final String crunchyStandard = BuildConfig.CRUNCHY_LINK.replace(HTTP_SECURE, HTTP_STANDARD);
    private static final String feedStandard = BuildConfig.FEEDS_LINK.replace(HTTP_SECURE, HTTP_STANDARD);

    public static String episodeSupport(List<ExternalLink> links) {
        for (ExternalLink link : links)
            if(link.getUrl().startsWith(BuildConfig.CRUNCHY_LINK) || link.getUrl().startsWith(crunchyStandard))
                return linkStripper(link.getUrl()+".rss");
            else if(link.getUrl().startsWith(BuildConfig.FEEDS_LINK) || link.getUrl().startsWith(feedStandard))
                return link.getUrl();
        return null;
    }

    private static String linkStripper(String link) {
        String stripped = link.replace(BuildConfig.CRUNCHY_LINK, "");
        return stripped.replace(crunchyStandard, "");
    }

    /**
     * Gets the series title without description
     */
    public static String getActualTile(String episodeTitle) {
        return episodeTitle.replaceAll("((.- Episode)|(.Season)).*", "");
    }
}
