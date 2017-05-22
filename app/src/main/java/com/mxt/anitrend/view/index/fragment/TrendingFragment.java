package com.mxt.anitrend.view.index.fragment;


import android.os.Bundle;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesAnimeAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.presenter.base.TrendingPresenter;
import com.mxt.anitrend.viewmodel.fragment.DefaultListFragment;

/**
 * @see DefaultListFragment
 */
public class TrendingFragment extends DefaultListFragment<Series> {

    public TrendingFragment() {
        // Required empty public constructor
    }

    public static DefaultListFragment newInstance() {
        return new TrendingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColumnSize = R.integer.card_col_size_home;
        isPaginate = true;
        mPresenter = new TrendingPresenter(getContext(), "popularity-desc", FilterTypes.AnimeStatusTypes[FilterTypes.AnimeStatusType.CURRENTLY_AIRING.ordinal()]);
    }

    @Override
    public void updateUI() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                mAdapter = new SeriesAnimeAdapter(model, getActivity(), mPresenter.getAppPrefs());
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(model);
            }

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                showEmpty(getString(R.string.layout_empty_response));
        }
    }

    @Override
    public void update() {
        progressLayout.showLoading();
        makeRequest();
    }

    @Override
    public void onLoadMore(int nextPage) {
        if (!isLimit) {
            mPage = nextPage;
            swipeRefreshLayout.setRefreshing(true);
            makeRequest();
        }
    }

    @Override
    public void makeRequest() {
        mPresenter.beginAsync(this, mPage);
    }

}