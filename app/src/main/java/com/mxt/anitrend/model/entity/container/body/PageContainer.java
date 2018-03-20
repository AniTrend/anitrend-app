package com.mxt.anitrend.model.entity.container.body;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PageContainer<T> extends Container {

    @SerializedName(value = "media", alternate = {"reviews", "mediaList", "activities", "activityReplies", "studios",
            "characters", "users", "nodes", "followers", "following"
    })
    private List<T> pageData;

    public List<T> getPageData() {
        return pageData;
    }
}
