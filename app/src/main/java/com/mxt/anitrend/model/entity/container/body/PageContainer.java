package com.mxt.anitrend.model.entity.container.body;

import com.google.gson.annotations.SerializedName;
import com.mxt.anitrend.util.CompatUtil;

import java.util.List;

/**
 * Page nested wrapper for various data types
 * with a base implementation of page info
 */
public class PageContainer<T> extends Container {

    @SerializedName(value = "media", alternate = { "nodes",
            "mediaTrends", "reviews", "mediaList",
            "activities", "activityReplies",
            "users", "followers", "following",
            "anime", "manga", "characters", "staff", "studios"
    })
    private List<T> pageData;

    public List<T> getPageData() {
        return pageData;
    }

    @Override
    public boolean isEmpty() {
        return CompatUtil.isEmpty(pageData);
    }
}
