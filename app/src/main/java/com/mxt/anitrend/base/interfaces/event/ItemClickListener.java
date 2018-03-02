package com.mxt.anitrend.base.interfaces.event;

import android.view.View;

/**
 * Created by max on 2017/11/15.
 * a click listener for view holders
 */

public interface ItemClickListener<T> {

    /**
     * When the target view from {@link View.OnClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data the model that at the click index
     */
    void onItemClick(View target, T data);

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data the model that at the long click index
     */
    void onItemLongClick(View target, T data);
}
