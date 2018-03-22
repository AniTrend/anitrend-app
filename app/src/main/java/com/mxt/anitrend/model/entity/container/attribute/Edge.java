package com.mxt.anitrend.model.entity.container.attribute;

import com.google.gson.annotations.SerializedName;

/**
 * Edge attribute item with type and value
 * T - Type of relation for the edge
 * V - Type of the model
 */
public class Edge<T, V> {

    @SerializedName(value = "role", alternate = {"staffRole", "relationType", "voiceActors"})
    private T type;
    @SerializedName(value = "media", alternate = "node")
    private V value;

    public T getType() {
        return type;
    }

    public V getValue() {
        return value;
    }
}
