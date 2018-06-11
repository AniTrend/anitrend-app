package com.mxt.anitrend.base.interfaces.event;

import android.view.View;

import com.annimon.stream.IntPair;

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
     * @param data the model that at the clicked index
     */
    void onItemClick(View target, IntPair<T> data);

    /**
     * When the target view from {@link View.OnLongClickListener}
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data the model that at the long clicked index
     */
    void onItemLongClick(View target, IntPair<T> data);
}
