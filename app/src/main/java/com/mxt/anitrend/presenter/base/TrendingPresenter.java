package com.mxt.anitrend.presenter.base;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.async.HomePageFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import java.util.List;

import retrofit2.Callback;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class TrendingPresenter extends CommonPresenter<List<Series>> {

    private Context mContext;

    public TrendingPresenter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    public Search getDefaultSearch(){
        return new Search(FilterTypes.SeriesTypes[FilterTypes.SeriesType.ANIME.ordinal()], /*anime or manga*/
                            null, /*year*/
                            null, /*season*/
                            null, /*Type, e.g. TV, Movie e.t.c*/
                            FilterTypes.AnimeStatusTypes[FilterTypes.AnimeStatusType.CURRENTLY_AIRING.ordinal()], /*status*/
                            null, /*genre */
                            getApiPrefs().getExcluded(), /*genre exclude*/
                            null, /*sort*/
                            getApiPrefs().getOrder(), /*order*/
                            true, /*airing data*/
                            false, /*full page true: no pagination; false: paginate using the page variable*/
                            null);/*page*/
    }

    @Override
    public void beginAsync(Callback<List<Series>> callback, Search searchModel) {
        new HomePageFetch(callback, searchModel, mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}