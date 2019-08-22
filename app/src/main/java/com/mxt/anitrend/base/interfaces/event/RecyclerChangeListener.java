package com.mxt.anitrend.base.interfaces.event;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by max on 2017/12/04.
 * data change listeners
 */

public interface RecyclerChangeListener<T> {

    void onItemsInserted(@NonNull List<T> swap);

    void onItemRangeInserted(@NonNull List<T> swap);

    void onItemRangeChanged(@NonNull List<T> swap);

    void onItemChanged(@NonNull T swap, int position);

    void onItemRemoved(int position);
}
