package com.mxt.anitrend.model.entity.container.request;

import java.util.Map;
import java.util.WeakHashMap;

public class GraphQueryContainer {

    private String query;
    private Map<String, Object> variables;

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public GraphQueryContainer setVariable(String key, Object value) {
        if(variables == null)
            variables = new WeakHashMap<>();
        variables.put(key, value);
        return this;
    }
}
