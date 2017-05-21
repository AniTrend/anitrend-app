package com.mxt.anitrend.api.hub;

import java.util.List;

/**
 * Created by max on 2017/05/14.
 */
public class Result <T> {

    private int count;
    private List<T> results;

    public int getCount() {
        return count;
    }

    public List<T> getResults() {
        return results;
    }
}
