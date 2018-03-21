package com.mxt.anitrend.model.entity.container.body;

import com.mxt.anitrend.model.entity.container.attribute.Edge;

import java.util.List;

/**
 * Edge Connection Container
 * T - Relation type
 * V - Data model
*/
public class EdgeContainer<T, V> extends Container {

    private List<Edge<T, V>> edges;

    public List<Edge<T, V>> getEdges() {
        return edges;
    }

    @Override
    public boolean isEmpty() {
        return edges != null && edges.isEmpty();
    }
}
