package com.mxt.anitrend.view.index.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesMangaAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.presenter.base.MangaPresenter;
import com.mxt.anitrend.viewmodel.fragment.DefaultListFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewMangaFragment extends DefaultListFragment<Series> {

    public NewMangaFragment() {
        // Required empty public constructor
    }

    public static DefaultListFragment newInstance() {
        return new NewMangaFragment();
    }

    /**
     * Override and set mPresenter, mColumnSize, and fetch argument/s
     * @See CommonPresenter
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColumnSize = R.integer.card_col_size_home;
        mPresenter = new MangaPresenter(getContext());
    }

    @Override
    public void updateUI() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                mAdapter = new SeriesMangaAdapter(model, getActivity(), mPresenter.getAppPrefs());
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
        onRefresh();
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        ((MangaPresenter)mPresenter).beginAsyncTrend(this);
    }
}
