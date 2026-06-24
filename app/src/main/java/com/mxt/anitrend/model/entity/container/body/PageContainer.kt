package com.mxt.anitrend.model.entity.container.body

import com.google.gson.annotations.SerializedName
import com.mxt.anitrend.util.CompatUtil

/**
 * Page nested wrapper for various data types
 * with a base implementation of page info
 */
class PageContainer<T> : Container() {
    @SerializedName(
        value = "media",
        alternate = [
            "nodes",
            "mediaTrends",
            "reviews",
            "mediaList",
            "activities",
            "activityReplies",
            "users",
            "followers",
            "following",
            "notifications",
            "anime",
            "manga",
            "characters",
            "staff",
            "studios",
            "lists",
            "recommendations",
        ],
    )
    var pageData: List<T> = emptyList()

    override val isEmpty: Boolean
        get() = CompatUtil.isEmpty(pageData)
}
