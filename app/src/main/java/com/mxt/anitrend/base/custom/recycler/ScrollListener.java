package com.mxt.anitrend.base.custom.recycler;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.mxt.anitrend.base.interfaces.event.RecyclerLoadListener;


/**
 * Created By Maxwell
 * This class represents a custom OnScrollListener for RecyclerView which allow us to pre-fetch
 * data when user reaches the bottom in the list.
 *
 * WARNING: This is not my own implementation.
 * Made By https://gist.github.com/Hochland/aca2f9152c1ff22d3b09f515530ac52b
 * Implementing original gist: https://gist.github.com/ssinss/e06f12ef66c51252563e
 */

public abstract class ScrollListener extends RecyclerView.OnScrollListener {

    private int mPreviousTotal = 0; // The total number of items in the dataset after the last load
    private boolean mLoading = true; // True if still waiting for the last set of data to load.
    private int mCurrentPage;
    private RecyclerLoadListener mLoadListener;

    private GridLayoutManager mGridLayoutManager;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    public void initListener(GridLayoutManager gridLayoutManager, int mCurrentPage, RecyclerLoadListener mLoadListener) {
        mGridLayoutManager = gridLayoutManager;
        this.mCurrentPage = mCurrentPage;
        this.mLoadListener = mLoadListener;
    }

    public void initListener(StaggeredGridLayoutManager staggeredGridLayoutManager, int mCurrentPage, RecyclerLoadListener mLoadListener) {
        mStaggeredGridLayoutManager = staggeredGridLayoutManager;
        this.mCurrentPage = mCurrentPage;
        this.mLoadListener = mLoadListener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int mTotalItemCount;
        int mFirstVisibleItem = 0;
        int mVisibleItemCount = recyclerView.getChildCount();
        if(mGridLayoutManager != null) {
            mTotalItemCount = mGridLayoutManager.getItemCount();
            mFirstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();
        } else {
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
        int mVisibleThreshold = 6; //minimum allowed threshold before next page reload request
        if (!mLoading && (mTotalItemCount - mVisibleItemCount)
                <= (mFirstVisibleItem + mVisibleThreshold)) {
            // End has been reached

            mCurrentPage++;
            mLoadListener.onLoadMore(mCurrentPage);
            mLoading = true;
        }
    }

    public void onRefreshPage() {
        mPreviousTotal = 0;
        mCurrentPage = 1;
    }
}
