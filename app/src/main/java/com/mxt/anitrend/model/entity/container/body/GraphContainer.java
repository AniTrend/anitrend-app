package com.mxt.anitrend.model.entity.container.body;

import com.mxt.anitrend.model.entity.container.attribute.GraphError;
import com.mxt.anitrend.util.CompatUtil;

import java.util.List;

public class GraphContainer<T> {

    private DataContainer<T> data;
    private List<GraphError> errors;

    public DataContainer<T> getData() {
        return data;
    }

    public List<GraphError> getErrors() {
        return errors;
    }

    public boolean isEmpty() {
        return data == null && !CompatUtil.isEmpty(errors);
    }
}
