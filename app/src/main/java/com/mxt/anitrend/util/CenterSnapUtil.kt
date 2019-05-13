package com.mxt.anitrend.util

import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.View

import com.mxt.anitrend.base.interfaces.view.CustomView

/**
 * Created by max on 2018/08/11.
 */

class CenterSnapUtil(private var positionChangeListener: PositionChangeListener?) : PagerSnapHelper(), CustomView {

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        return super.calculateDistanceToFinalSnap(layoutManager, targetView)
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        val position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (positionChangeListener != null && position != RecyclerView.NO_POSITION)
            positionChangeListener!!.onPageChanged(position + 1)
        return position
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {

    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        positionChangeListener = null
    }

    interface PositionChangeListener {
        fun onPageChanged(currentPage: Int)
    }
}
