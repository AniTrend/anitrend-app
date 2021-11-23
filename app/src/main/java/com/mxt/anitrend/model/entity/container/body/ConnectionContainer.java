package com.mxt.anitrend.model.entity.container.body;

import com.google.gson.annotations.SerializedName;

public class ConnectionContainer<T> {

    @SerializedName(value = "relations", alternate = {"anime", "manga", "media",
            "characters", "characterMedia", "staff", "staffMedia",
            "stats", "statistics", "favourites", "nodes",
            "externalLinks", "recommendations"
    })
    private T connection;

    public T getConnection() {
        return connection;
    }

    public boolean isEmpty() {
        return connection == null;
    }
}
