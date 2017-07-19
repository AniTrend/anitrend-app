package com.mxt.anitrend.custom.event;

/**
 * Created by Maxwell on 10/28/2016.
 */
public interface FragmentCallback<T> {

    /**
     * Normal fragments
     */
    void update();

    /**
     * Search page fragments
     */
    void update(T query);
}
