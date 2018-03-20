package com.mxt.anitrend.model.entity.container.attribute;

import com.google.gson.annotations.SerializedName;

public class Edge<T> {

    @SerializedName(value = "role", alternate = {"staffRole", "relationType"})
    private String type;
    private T node;

    public String getType() {
        return type;
    }

    public T getNode() {
        return node;
    }
}
