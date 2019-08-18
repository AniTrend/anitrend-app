package com.mxt.anitrend.model.entity.container.body

import com.google.gson.annotations.SerializedName

data class DataContainer<T>(
    @SerializedName(
            value = "Page",
            alternate = [
                "MediaTagCollection", "GenreCollection", "MediaListCollection",

                "Character", "Staff", "Studio", "User", "Media", "MediaList",

                "Activity", "ActivityReply", "MediaTrends", "Viewer", "Deleted",

                "ToggleLike", "ToggleFavourite", "ToggleFollow",

                "SaveMediaListEntry", "UpdateMediaListEntries", "DeleteMediaListEntry",

                "RateReview", "SaveReview", "DeleteReview",

                "SaveTextActivity", "SaveMessageActivity", "SaveActivityReply",
                "DeleteActivity", "DeleteActivityReply"
            ]
    )
    val result: T? = null

)