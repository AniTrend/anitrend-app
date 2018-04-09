package com.mxt.anitrend.model.entity.container.body;

import com.mxt.anitrend.model.entity.container.attribute.GraphError;

import java.util.List;

public class GraphContainer<T> {

    private T data;
    private List<GraphError> errors;

    public T getData() {
        return data;
    }

    public List<GraphError> getErrors() {
        return errors;
    }

    public boolean isSuccess() {
        return data != null && errors == null;
    }
}
