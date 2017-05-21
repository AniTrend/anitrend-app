package com.mxt.anitrend.custom;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Class {@link StatefulRecyclerView} extends {@link RecyclerView} and adds position management on configuration changes.
 *
 * @author FrantisekGazo
 * @version 2016-03-15
 */

public class StatefulRecyclerView extends RecyclerView {

    private static final String SAVED_SUPER_STATE = "super-state";
    private static final String SAVED_LAYOUT_MANAGER = "layout-manager-state";

    private Parcelable mLayoutManagerSavedState;

    private boolean isListenerPresent;

    public StatefulRecyclerView(Context context) {
        super(context);
    }

    public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StatefulRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState());
        bundle.putParcelable(SAVED_LAYOUT_MANAGER, this.getLayoutManager().onSaveInstanceState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mLayoutManagerSavedState = bundle.getParcelable(SAVED_LAYOUT_MANAGER);
            state = bundle.getParcelable(SAVED_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * Restores scroll position after configuration change.
     * <p>
     * <b>NOTE:</b> Must be called after adapter has been set.
     */
    private void restorePosition() {
        if (mLayoutManagerSavedState != null) {
            this.getLayoutManager().onRestoreInstanceState(mLayoutManagerSavedState);
            mLayoutManagerSavedState = null;
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        restorePosition();
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

    public boolean hasOnScrollListener() {
        return isListenerPresent;
    }
}
