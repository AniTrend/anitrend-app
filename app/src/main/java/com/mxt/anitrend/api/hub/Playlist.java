package com.mxt.anitrend.api.hub;

import java.util.List;

/**
 * Created by max on 2017/05/14.
 */
public class Playlist {

    private String title;
    private List<String> videos;
    private String addedDate;
    private String addedBy;
    private String _id;
    private String thumbnail;

    public String getTitle() {
        return title;
    }

    public List<String> getVideos() {
        return videos;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public String get_id() {
        return _id;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
