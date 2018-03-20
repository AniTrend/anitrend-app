package com.mxt.anitrend.model.entity.container.body;

import com.mxt.anitrend.model.entity.container.attribute.Edge;
import java.util.List;

public class EdgeContainer<T> extends Container {

    private List<Edge<T>> edges;

    public List<Edge<T>> getEdges() {
        return edges;
    }
}
