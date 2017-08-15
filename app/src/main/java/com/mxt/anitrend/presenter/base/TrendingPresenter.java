package com.mxt.anitrend.presenter.base;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.base.custom.async.HomePageFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import java.util.List;

import retrofit2.Callback;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class TrendingPresenter extends CommonPresenter<List<Series>> {
    private Context mContext;
    private String sort;
    private String status;

    public TrendingPresenter(Context mContext, String sort, String status) {
        super(mContext);
        this.sort = sort;
        this.status = status;
        this.mContext = mContext;
    }

    public void beginAsync(Callback<List<Series>> callback, int page) {
        new HomePageFetch(callback, new Search(
                KeyUtils.SeriesTypes[KeyUtils.ANIME],
                null, null,
                getApiPrefs().getShowType(),
                status,
                getApiPrefs().getGenres(),
                getApiPrefs().getExcluded(),
                sort, null,
                true, false,
                page), mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}