package com.mxt.anitrend.model.entity.container.body;

import com.google.gson.annotations.SerializedName;

public class ConnectionContainer<T> {

    @SerializedName(value = "relations", alternate = {"anime", "manga", "media",
            "characters", "staff", "staffMedia",
            "stats", "favourites"
    })
    private T connection;

    public T getConnection() {
        return connection;
    }
}
