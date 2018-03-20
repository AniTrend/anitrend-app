package com.mxt.anitrend.model.entity.container.body;

import com.google.gson.annotations.SerializedName;

public class DataContainer<T> {

    @SerializedName(value = "PageContainer", alternate = {"Character", "Staff", "Studio", "User", "Media", "MediaList",
            "Activity", "ActivityReply", "MediaTagCollection", "GenreCollection"
    })
    private T result;

    public T getResult() {
        return result;
    }

    public boolean isEmpty() {
        return result == null;
    }
}
