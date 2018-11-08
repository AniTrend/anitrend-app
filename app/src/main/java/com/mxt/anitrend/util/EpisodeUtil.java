package com.mxt.anitrend.util;

import com.mxt.anitrend.model.entity.anilist.ExternalLink;

import java.util.List;

/**
 * Created by max on 2017/11/04.
 * Episode list helper that check for crunchy feeds
 */

public class EpisodeUtil {

    private static final String crunchyStandard = "crunchyroll", feedStandard = "feedburner";

    public static String episodeSupport(List<ExternalLink> links) {
        for (ExternalLink link : links)
            if(link.getUrl().contains(crunchyStandard))
                return linkStripper(link.getUrl());
            else if(link.getUrl().contains(feedStandard))
                return link.getUrl();
        return null;
    }

    private static String linkStripper(String link) {
        int lastIndex = link.lastIndexOf("/") + 1;
        String stripped = link.substring(lastIndex);
        return String.format("%s.rss", stripped);
    }

    /**
     * Gets the series title without description
     */
    public static String getActualTile(String episodeTitle) {
        return episodeTitle.replaceAll("((.- Episode)|(.Season)).*", "");
    }
}
