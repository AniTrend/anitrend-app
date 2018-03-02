package com.mxt.anitrend.base.custom.recycler;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.mxt.anitrend.base.interfaces.view.CustomView;

/**
 * Class {@link StatefulRecyclerView} extends {@link RecyclerView} and adds position management on configuration changes.
 *
 * @author FrantisekGazo
 * @version 2016-03-15
 * Modified by max
 */

public class StatefulRecyclerView extends RecyclerView implements CustomView {

    private boolean isListenerPresent;

    public StatefulRecyclerView(Context context) {
        super(context);
        onInit();
    }

    public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onInit();
    }

    /**
     * Add a listener that will be notified of any changes in scroll state or position.
     * <p>
     * <p>Components that add a listener should take care to remove it when finished.
     * Other components that take ownership of a view may call {@link #clearOnScrollListeners()}
     * to remove all attached listeners.</p>
     *
     * @param listener listener to set or null to clear
     */
    @Override
    public void addOnScrollListener(OnScrollListener listener) {
        super.addOnScrollListener(listener);
        isListenerPresent = true;
    }

    /**
     * Remove all secondary listener that were notified of any changes in scroll state or position.
     */
    @Override
    public void clearOnScrollListeners() {
        super.clearOnScrollListeners();
        isListenerPresent = false;
    }

    /*To avoid multiple instances of scroll listener from being added*/
    public boolean hasOnScrollListener() {
        return isListenerPresent;
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {

    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }
}
