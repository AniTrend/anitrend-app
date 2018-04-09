package com.mxt.anitrend.model.entity.container.attribute;

import java.util.List;
import java.util.Map;

public class GraphError {

    private String message;
    private int status;
    private List<Map<String, Integer>> locations;

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public List<Map<String, Integer>> getLocations() {
        return locations;
    }

    @Override
    public String toString() {
        return "message='" + message + '\'' + ", status=" + status + ", locations=" + locations;
    }
}
