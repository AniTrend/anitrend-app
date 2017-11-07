package com.mxt.anitrend.view.index.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.FeedVideoAdapter;
import com.mxt.anitrend.adapter.recycler.index.PlaylistAdapter;
import com.mxt.anitrend.api.hub.Video;
import com.mxt.anitrend.presenter.index.PlaylistPresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.viewmodel.fragment.DefaultResultFragment;

/**
 * Created by max on 2017/08/12.
 */

public class FeedVideoFragment extends DefaultResultFragment<Video> {

    public static FeedVideoFragment newInstance() {
        return new FeedVideoFragment();
    }

    /**
     * Override and set mPresenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     * @See CommonPresenter
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColumnSize = R.integer.card_col_size_home;
        isPaginate = true;
        mPresenter = new PlaylistPresenter<>(getContext(), KeyUtils.FEED_TYPE);
    }

    /**
     * Is automatically called in the @onStart Method
     */
    @Override
    protected void updateUI() {
        if (recyclerView != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (mAdapter == null) {
                mAdapter = new FeedVideoAdapter(model, getActivity());
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(model);
            }
            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else {
                showEmpty(getString(R.string.layout_empty_response));
            }
        }
    }

    /**
     * All new or updated network requests should be handled in this method
     */
    @Override
    public void makeRequest() {
        mPresenter.beginAsync(this, mPage);
    }

    /**
     * Provides the next page number
     *
     * @param nextPage next page
     */
    @Override
    public void onLoadMore(int nextPage) {
        if (!isLimit) {
            mPage = nextPage;
            swipeRefreshLayout.setRefreshing(true);
            makeRequest();
        }
    }

    /**
     * Normal fragments
     */
    @Override
    public void update() {
        progressLayout.showLoading();
        makeRequest();
    }
}
