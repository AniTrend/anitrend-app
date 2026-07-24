package com.mxt.anitrend.base.custom.recycler

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/06/09.
 * This class represents a custom OnScrollListener for RecyclerView which allow us to pre-fetch
 * data when user reaches the bottom in the list.
 *
 * Made By https://gist.github.com/Hochland/aca2f9152c1ff22d3b09f515530ac52b
 * Implementing original gist: https://gist.github.com/ssinss/e06f12ef66c51252563e
 * Modified by max to accommodate grid and staggered layout managers and other custom properties
 */
open class RecyclerScrollListener : RecyclerView.OnScrollListener() {
    private var previousTotal = 0 // The total number of items in the dataset after the last load
    private var loading = true // True if still waiting for the last set of data to load.
    var currentPage = 1
    var currentOffset = 0
    private var loadListener: RecyclerLoadListener? = null

    private var pageInfo: PageInfo? = null

    private var gridLayoutManager: GridLayoutManager? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null

    fun initListener(
        gridLayoutManager: GridLayoutManager,
        loadListener: RecyclerLoadListener,
    ) {
        this.gridLayoutManager = gridLayoutManager
        this.loadListener = loadListener
    }

    fun initListener(
        staggeredGridLayoutManager: StaggeredGridLayoutManager,
        loadListener: RecyclerLoadListener,
    ) {
        this.staggeredGridLayoutManager = staggeredGridLayoutManager
        this.loadListener = loadListener
    }

    override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int,
    ) {
        super.onScrolled(recyclerView, dx, dy)

        var totalItemCount = 0
        var firstVisibleItem = 0
        val visibleItemCount = recyclerView.childCount
        when {
            gridLayoutManager != null -> {
                totalItemCount = gridLayoutManager?.itemCount ?: 0
                firstVisibleItem = gridLayoutManager?.findFirstVisibleItemPosition() ?: 0
            }
            staggeredGridLayoutManager != null -> {
                totalItemCount = staggeredGridLayoutManager?.itemCount ?: 0
                val firstPositions = staggeredGridLayoutManager?.findFirstVisibleItemPositions(null)
                if (firstPositions != null && firstPositions.isNotEmpty()) {
                    firstVisibleItem = firstPositions[0]
                }
            }
        }

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
            }
        }
        val visibleThreshold = 9 // minimum allowed threshold before next page reload request
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            if (pageInfo == null || pageInfo?.hasNextPage() == true) {
                currentPage++
                currentOffset += KeyUtil.PAGING_LIMIT
                loadListener?.onLoadMore()
                loading = true
            }
        }
    }

    /**
     * Should be used when refreshing a layout
     */
    fun onRefreshPage() {
        loading = true
        previousTotal = 0
        currentPage = 1
        currentOffset = 0
        pageInfo = null
    }

    fun setPageInfo(pageInfo: PageInfo?) {
        this.pageInfo = pageInfo
    }

    fun getPageInfo(): PageInfo? = pageInfo
}
