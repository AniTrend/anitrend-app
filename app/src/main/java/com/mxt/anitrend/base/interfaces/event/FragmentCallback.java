package com.mxt.anitrend.base.interfaces.event;

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
