package com.mxt.anitrend.base.interfaces.view;

/**
 * Created by max on 2017/06/24.
 * Designed to init constructors for custom views
 */

public interface CustomView {

    /**
     * Optionally included when constructing custom views
     */
    void onInit();

    /**
     * Clean up any resources that won't be needed
     */
    void onViewRecycled();
}
