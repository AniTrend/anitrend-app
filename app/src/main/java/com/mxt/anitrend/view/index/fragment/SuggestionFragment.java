package com.mxt.anitrend.view.index.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.index.SeriesAnimeAdapter;
import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.api.structure.UserStats;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.presenter.index.FragmentPresenter;
import com.mxt.anitrend.utils.Recommendation;
import com.mxt.anitrend.viewmodel.fragment.DefaultListFragment;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mxt.anitrend.api.structure.FilterTypes.SeriesType;
import static com.mxt.anitrend.api.structure.FilterTypes.SeriesTypes;

/**
 * Created by ecdv on 2017-05-04.
 */

public class SuggestionFragment extends DefaultListFragment<Series> {

    private final String KEY_CURRENT_USER = "current_user_key";

    private User mUser;

    public SuggestionFragment() {
        // Required empty public constructor
    }

    public static DefaultListFragment newInstance() {
        return new SuggestionFragment();
    }

    /**
     * Override and set mPresenter, mColumnSize, and fetch argument/s
     * @See CommonPresenter
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        outState.putParcelable(KEY_CURRENT_USER, mUser);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
            mUser = savedInstanceState.getParcelable(KEY_CURRENT_USER);
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
        onRefresh();
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if(mUser == null) {
            UserModel userModel = ServiceGenerator.createService(UserModel.class, getContext());
            userModel.fetchCurrentUser();
            new AsyncTaskFetch<>(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if(isVisible() && !isDetached() || !isRemoving()){
                        if(response.isSuccessful() && response.body() != null) {
                            mUser = response.body();
                            makeRequest();
                        }
                        else
                            showError(response);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    if(isVisible() && !isDetached() || !isRemoving()) {
                        t.printStackTrace();
                        showEmpty(t.getLocalizedMessage());
                    }
                }
            }, getContext()).execute(AsyncTaskFetch.RequestType.USER_CURRENT_REQ);
            return;
        }
        fireRefresh();
    }

    private void fireRefresh() {
        super.onRefresh();
    }

    @Override
    public void makeRequest() {
        UserStats userStats;
        if((userStats = mUser.getStats()) != null) {
            HashMap<String, Integer> genreList = userStats.getFavourite_genres_map();
            if (genreList != null) {
                mPresenter.beginAsync(this, new Search(SeriesTypes[SeriesType.ANIME.ordinal()], /*anime or manga*/
                        null, /*year*/
                        null, /*season*/
                        mPresenter.getApiPrefs().getShowType(), /*Type e.g. TV or Movie e.t.c*/
                        null, /*status*/
                        Recommendation.getTopFavourites(genreList, mPresenter.getApiPrefs()), /*genre */
                        mPresenter.getApiPrefs().getExcluded(), /*genre exclude*/
                        mPresenter.getApiPrefs().getSort(), /*sort*/
                        mPresenter.getApiPrefs().getOrder(), /*order*/
                        true, /*airing data*/
                        false, /*full page true: no pagination; false: paginate using the page variable*/
                        mPage /*page*/));
                return;
            }
        }
        showEmpty(getString(R.string.text_insufficient_genres));
    }

    @Override
    public void onLoadMore(int nextPage) {
        if (!isLimit) {
            mPage = nextPage;
            swipeRefreshLayout.setRefreshing(true);
            makeRequest();
        }
    }

}
