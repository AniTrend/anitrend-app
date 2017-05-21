package com.mxt.anitrend.utils;

import com.mxt.anitrend.BuildConfig;

/**
 * Created by max on 2017/05/14.
 */

public class HubUtil {

    public static String defaultFilter() {
        return "{\"videoStatus\":\"final\",\"addedBy\":\"UeQuqJ90j3igXB6A\"}";
    }

    public static String playlistFilter() {
        return "{\"videos\":{\"$exists\":true},\"addedBy\":\"UeQuqJ90j3igXB6A\"}";
    }

    public static String getSubtitleLink(String id) {
        return String.format("%s/host/%s", BuildConfig.HUB_BASE_LINK, id);
    }

    public static String getEpisodeLink(String location) {
        return String.format("%s%s", BuildConfig.HUB_BASE_LINK, location.replace("/api/",""));
    }

    public static String getThubnailLink(String location) {
        return String.format("%s%s", BuildConfig.HUB_BASE_LINK, location.replace("/api/",""));
    }
}
