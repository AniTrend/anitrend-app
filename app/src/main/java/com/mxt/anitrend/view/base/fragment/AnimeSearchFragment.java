package com.mxt.anitrend.view.base.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.async.SortHelper;
import com.mxt.anitrend.utils.ComparatorProvider;
import com.mxt.anitrend.utils.FilterProvider;
import com.mxt.anitrend.viewmodel.fragment.SearchFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AnimeSearchFragment extends SearchFragment<Series> {


    public AnimeSearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String query) {
        Bundle args = new Bundle();
        SearchFragment<Series> mFragment = new AnimeSearchFragment();
        args.putString(ARG_KEY, query);
        mFragment.setArguments(args);
        return mFragment;
    }

    /**
     * Override if you need to include extra functionality into the method,
     * the method will get the arguments from the from your bundle and into
     * the model followed by initialization of your presenter
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mRequestType = AsyncTaskFetch.RequestType.ANIME_SEARCH_REQ;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResponse(Call<List<Series>> call, Response<List<Series>> response) {
        if(isVisible() && (!isDetached() || !isRemoving()))
            if(response.isSuccessful() && response.body() != null) {
                model = FilterProvider.getSeriesFilter(response.body());
                sortItems();
            }
            else
                showError();
    }

    /**
     * The sorting runner must be invoked in here
     */
    @Override
    public void sortItems() {
        mSorter = new SortHelper<>(ComparatorProvider.getSeriesComparator(mPresenter.getApiPrefs()), this, model);
        mSorter.execute();
    }
}
