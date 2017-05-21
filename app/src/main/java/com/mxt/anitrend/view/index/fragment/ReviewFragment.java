package com.mxt.anitrend.view.index.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesReviewTypeAdapter;
import com.mxt.anitrend.api.model.Review;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.ReviewType;
import com.mxt.anitrend.presenter.index.FragmentReviewPresenter;
import com.mxt.anitrend.viewmodel.fragment.DefaultReviewFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/05/02.
 */
public class ReviewFragment extends DefaultReviewFragment<ReviewType> {

    private int VIEW_POSITION;

    public ReviewFragment() {

    }

    public static ReviewFragment newInstance(int VIEW_POSITION) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_KEY, VIEW_POSITION);
        ReviewFragment mFragment = new ReviewFragment();
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
        model_type = FilterTypes.ReviewTypes[VIEW_POSITION];
        mPresenter = new FragmentReviewPresenter(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        addScrollLoadTrigger();
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void updateUI() {
        if(recyclerView != null) {
            if(swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if(mAdapter == null) {
                mAdapter = new SeriesReviewTypeAdapter(model, getActivity(), mPresenter.getAppPrefs(), mPresenter.getApiPrefs());
                recyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.onDataSetModified(mPresenter.getAppPrefs().getReviewType()?model.getAnime():model.getManga());
            }

            if (mAdapter.getItemCount() > 0) {
                progressLayout.showContent();
            } else
                showEmpty();
        }
    }

    @Override
    public void onResponse(Call<ReviewType> call, Response<ReviewType> response) {
        if(isVisible() && (!isDetached() || !isRemoving()))
            if(response.isSuccessful() && response.body() != null && (response.body().getAnime().size() > 0 || response.body().getManga().size() > 0)) {
                if(model == null)
                    model = response.body();
                else {
                    final List<Review> animeReviews = response.body().getAnime();
                    final List<Review> mangaReviews = response.body().getManga();
                    model.setAnime(animeReviews);
                    model.setManga(mangaReviews);
                }
                updateUI();
            } else if(response.isSuccessful() && response.body() != null && mPage != 1) {
                swipeRefreshLayout.setRefreshing(false);
                isLimit = true;
            } else {
                showError(response);
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
        mPage = 1;
        model = null;
        mAdapter = null;
        mPresenter.onRefreshPage();
        mPresenter.beginAsync(this, mPage, model_type);
    }

    @Override
    public void onLoadMore(int currentPage) {
        if(!isLimit) {
            mPage = currentPage;
            swipeRefreshLayout.setRefreshing(true);
            mPresenter.beginAsync(this, mPage, model_type);
        }
    }
}
