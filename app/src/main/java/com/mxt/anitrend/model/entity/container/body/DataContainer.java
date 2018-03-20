package com.mxt.anitrend.model.entity.container.body;

import com.google.gson.annotations.SerializedName;

public class DataContainer<T> {

    @SerializedName(value = "Page", alternate = {"Character", "Staff", "Studio", "User", "Media", "MediaList",
            "Activity", "ActivityReply", "MediaTagCollection", "GenreCollection", "MediaTrends", "Viewer"
    })
    private T result;

    public T getResult() {
        return result;
    }

    public boolean isEmpty() {
        return result == null;
    }
}
