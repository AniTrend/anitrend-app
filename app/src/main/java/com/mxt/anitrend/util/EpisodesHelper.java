package com.mxt.anitrend.util;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.api.structure.ExternalLink;

import java.util.List;

/**
 * Created by Maxwell on 2/10/2017.
 */

public class EpisodesHelper {

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
}
