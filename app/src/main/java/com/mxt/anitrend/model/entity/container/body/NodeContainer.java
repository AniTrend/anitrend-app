package com.mxt.anitrend.model.entity.container.body;

import java.util.List;

public class NodeContainer<T> extends Container {

    private List<T> nodes;

    public List<T> getNodes() {
        return nodes;
    }
}
