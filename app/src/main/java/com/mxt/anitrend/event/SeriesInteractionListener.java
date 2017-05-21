package com.mxt.anitrend.event;

import com.mxt.anitrend.api.structure.ListItem;

/**
 * Created by max on 2017/03/06.
 * Anime and Manga event listener for both clicks and long clicks
 */
public interface SeriesInteractionListener {

    /**
     * Bind to onClickListener in an adapter, and implement in fragment or class
     * <br/>
     * @param item the adapter position of the clicked item.
     * @param vId the current view id.
     */
    void onClickSeries(ListItem item, int vId);

    /**
     * Bind to onLongClickListener in an adapter, and implement in fragment or class
     * <br/>
     * @param item the adapter position of the clicked item.
     */
    void onLongClickSeries(ListItem item);
}
