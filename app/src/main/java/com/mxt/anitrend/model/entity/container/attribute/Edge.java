package com.mxt.anitrend.model.entity.container.attribute;

import com.google.gson.annotations.SerializedName;

/**
 * Edge attribute item with type and value
 * T - Type of relation for the edge
 * V - Type of the model
 */
public class Edge<T, V> {

    @SerializedName(value = "role", alternate = {"staffRole", "relationType", "voiceActors"})
    private V type;
    private T node;

    public V getType() {
        return type;
    }

    public T getNode() {
        return node;
    }
}
