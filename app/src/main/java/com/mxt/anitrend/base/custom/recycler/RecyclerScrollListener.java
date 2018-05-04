package com.mxt.anitrend.base.custom.recycler;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener;
import com.mxt.anitrend.model.entity.container.attribute.PageInfo;
import com.mxt.anitrend.util.KeyUtil;


/**
 * Created by max on 2017/06/09.
 * This class represents a custom OnScrollListener for RecyclerView which allow us to pre-fetch
 * data when user reaches the bottom in the list.
 *
 * Made By https://gist.github.com/Hochland/aca2f9152c1ff22d3b09f515530ac52b
 * Implementing original gist: https://gist.github.com/ssinss/e06f12ef66c51252563e
 * Modified by max to accommodate grid and staggered layout managers and other custom properties
 */

public abstract class RecyclerScrollListener extends RecyclerView.OnScrollListener {

    private int mPreviousTotal = 0; // The total number of items in the dataset after the last load
    private boolean mLoading = true; // True if still waiting for the last set of data to load.
    private int mCurrentPage = 1;
    private int mCurrentOffset = 0;
    private RecyclerLoadListener mLoadListener;

    private PageInfo pageInfo;

    private GridLayoutManager mGridLayoutManager;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    public void initListener(GridLayoutManager gridLayoutManager, RecyclerLoadListener mLoadListener) {
        mGridLayoutManager = gridLayoutManager;
        this.mLoadListener = mLoadListener;
    }

    public void initListener(StaggeredGridLayoutManager staggeredGridLayoutManager, RecyclerLoadListener mLoadListener) {
        mStaggeredGridLayoutManager = staggeredGridLayoutManager;
        this.mLoadListener = mLoadListener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int mTotalItemCount = 0;
        int mFirstVisibleItem = 0;
        int mVisibleItemCount = recyclerView.getChildCount();
        if(mGridLayoutManager != null) {
            mTotalItemCount = mGridLayoutManager.getItemCount();
            mFirstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();
        } else if (mStaggeredGridLayoutManager != null) {
            mTotalItemCount = mStaggeredGridLayoutManager.getItemCount();
            int[] firstPositions = mStaggeredGridLayoutManager.findFirstVisibleItemPositions(null);
            if(firstPositions != null && firstPositions.length > 0)
                mFirstVisibleItem = firstPositions[0];
        }

        if (mLoading) {
            if (mTotalItemCount > mPreviousTotal) {
                mLoading = false;
                mPreviousTotal = mTotalItemCount;
            }
        }
        int mVisibleThreshold = 3; //minimum allowed threshold before next page reload request
        if (!mLoading && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem + mVisibleThreshold)) {
            if(pageInfo == null || pageInfo.hasNextPage()) {
                mCurrentPage++;
                mCurrentOffset += KeyUtil.PAGING_LIMIT;
                mLoadListener.onLoadMore();
                mLoading = true;
            }
        }
    }

    /**
     * Should be used when refreshing a layout
     */
    public void onRefreshPage() {
        mLoading = true;
        mPreviousTotal = 0;
        mCurrentPage = 1;
        mCurrentOffset = 0;
        pageInfo = null;
    }

    /**
     * @return Returns the current pagination page number
     */
    public int getCurrentPage() {
        return mCurrentPage;
    }

    /**
     * @return Returns the current pagination offset
     */
    public int getCurrentOffset() {
        return mCurrentOffset;
    }

    public void setCurrentPage(int mCurrentPage) {
        this.mCurrentPage = mCurrentPage;
    }

    public void setCurrentOffset(int mCurrentOffset) {
        this.mCurrentOffset = mCurrentOffset;
    }

    public void setPageInfo(@Nullable PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public @Nullable PageInfo getPageInfo() {
        return pageInfo;
    }
}
