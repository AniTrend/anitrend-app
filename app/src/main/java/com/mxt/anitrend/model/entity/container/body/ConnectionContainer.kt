package com.mxt.anitrend.model.entity.container.body

import com.google.gson.annotations.SerializedName

class ConnectionContainer<T : Any> {
    @SerializedName(
        value = "relations",
        alternate = [
            "anime",
            "manga",
            "media",
            "characters",
            "characterMedia",
            "staff",
            "staffMedia",
            "stats",
            "statistics",
            "favourites",
            "nodes",
            "externalLinks",
            "recommendations",
        ],
    )
    lateinit var connection: T

    val isEmpty: Boolean
        get() = !::connection.isInitialized
}
