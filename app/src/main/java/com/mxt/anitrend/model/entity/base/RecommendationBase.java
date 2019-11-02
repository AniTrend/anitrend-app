package com.mxt.anitrend.model.entity.base;

import com.mxt.anitrend.model.entity.group.RecyclerItem;
import com.mxt.anitrend.util.KeyUtil;

public class RecommendationBase extends RecyclerItem {
    private long id;
    private MediaBase mediaRecommendation;
    private int rating;
    private UserBase user;
    @KeyUtil.RecommendationRating
    private String userRating;

    public long getId() {
        return id;
    }

    public MediaBase getMediaRecommendation() {
        return mediaRecommendation;
    }

    public int getRating() {
        return rating;
    }

    public UserBase getUser() {
        return user;
    }

    @KeyUtil.RecommendationRating
    public String getUserRating() {
        return userRating;
    }
}
