package com.mxt.anitrend.base.interfaces.event

import android.view.View
import com.annimon.stream.IntPair

/**
 * Created by max on 2017/11/15.
 * a click listener for view holders
 */
interface ItemClickListener<T> {
    /**
     * When the target view from [View.OnClickListener]
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been clicked
     * @param data the model that at the clicked index
     */
    fun onItemClick(
        target: View,
        data: IntPair<T>,
    )

    /**
     * When the target view from [View.OnLongClickListener]
     * is clicked from a view holder this method will be called
     *
     * @param target view that has been long clicked
     * @param data the model that at the long clicked index
     */
    fun onItemLongClick(
        target: View,
        data: IntPair<T>,
    )
}
