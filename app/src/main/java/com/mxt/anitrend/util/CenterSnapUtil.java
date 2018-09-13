package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mxt.anitrend.base.interfaces.view.CustomView;

/**
 * Created by max on 2018/08/11.
 */

public class CenterSnapUtil extends PagerSnapHelper implements CustomView {

    private PositionChangeListener positionChangeListener;

    public CenterSnapUtil(PositionChangeListener positionChangeListener) {
        this.positionChangeListener = positionChangeListener;
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        return super.calculateDistanceToFinalSnap(layoutManager, targetView);
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        int position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if(positionChangeListener != null && position != RecyclerView.NO_POSITION)
            positionChangeListener.onPageChanged(position + 1);
        return position;
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
        positionChangeListener = null;
    }

    public interface PositionChangeListener {
        void onPageChanged(int currentPage);
    }
}
