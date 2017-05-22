package com.mxt.anitrend.view.index.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesAnimeAdapter;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.presenter.index.FragmentPresenter;
import com.mxt.anitrend.viewmodel.fragment.DefaultListFragment;

/**
 * @see DefaultListFragment
 */
public class SeasonFragment extends DefaultListFragment<Series> {


    private int VIEW_POSITION;

    public SeasonFragment() {
        // Required empty public constructor
    }

    public static DefaultListFragment newInstance(int VIEW_POSITION) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_KEY, VIEW_POSITION);
        SeasonFragment mFragment = new SeasonFragment();
        mFragment.setArguments(bundle);
        return mFragment;
    }

    /**
     * Override and set mPresenter, mColumnSize, and fetch argument/s
     * @See CommonPresenter
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments().containsKey(ARG_KEY))
            VIEW_POSITION = getArguments().getInt(ARG_KEY);
        mColumnSize = R.integer.card_col_size_home;
        isPaginate = true;
        mPresenter = new FragmentPresenter(getContext());
    }

    /**
     * Override as normal, the saving of the model is also managed for
     * you so no need to save it.
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_KEY, VIEW_POSITION);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
            VIEW_POSITION = savedInstanceState.getInt(ARG_KEY);
    }

    public void updateUI() {
        if (recyclerView != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (mAdapter == null) {
                mAdapter = new SeriesAnimeAdapter(model, getActivity(), mPresenter.getAppPrefs());
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

    @Override
    public void update() {
        progressLayout.showLoading();
        makeRequest();
    }

    @Override
    public void makeRequest() {
        mPresenter.beginAsync(this, VIEW_POSITION, mPage);
    }

    @Override
    public void onLoadMore(int currentPage) {
        if (!isLimit) {
            mPage = currentPage;
            swipeRefreshLayout.setRefreshing(true);
            makeRequest();
        }
    }
}
