package com.mxt.anitrend.model.entity.container.body;

import com.mxt.anitrend.model.entity.container.attribute.Edge;
import com.mxt.anitrend.util.CompatUtil;

import java.util.List;

/**
 * Edge Connection Container
 * T - Relation type
*/
public class EdgeContainer<T extends Edge> extends Container {

    private List<T> edges;

    public List<T> getEdges() {
        return edges;
    }

    @Override
    public boolean isEmpty() {
        return CompatUtil.isEmpty(edges);
    }
}
