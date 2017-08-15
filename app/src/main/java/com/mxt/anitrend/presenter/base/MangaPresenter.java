package com.mxt.anitrend.presenter.base;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.base.custom.async.HomePageFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import java.util.Arrays;
import java.util.List;

import retrofit2.Callback;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class MangaPresenter extends CommonPresenter<List<Series>> {

    private Search searchModel;
    private Context mContext;

    public MangaPresenter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public void beginAsync(Callback<List<Series>> callback, int page) {
            searchModel = new Search(KeyUtils.SeriesTypes[KeyUtils.MANGA], /*anime or manga*/
                 getApiPrefs().getYear(), /*year*/
                 null, /*season*/
                 getApiPrefs().getShowType(), /*Type, e.g. TV, Movie e.t.c*/
                 decodeStatus(getApiPrefs().getStatus()), /*status*/
                 getApiPrefs().getGenres(), /*genre */
                 getApiPrefs().getExcluded(), /*genre exclude*/
                 getApiPrefs().getSort(), /*sort*/
                 getApiPrefs().getOrder(), /*order*/
                 true, /*airing data*/
                 false, /*full page true: no pagination; false: paginate using the page variable*/
                 page);/*page*/

        new HomePageFetch(callback, searchModel, mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void beginAsyncTrend(Callback<List<Series>> callback, int page) {
        searchModel = new Search(KeyUtils.SeriesTypes[KeyUtils.MANGA], /*anime or manga*/
                null, /*year*/
                null, /*season*/
                null, /*Type, e.g. TV, Movie e.t.c*/
                null, /*status*/
                null, /*genre */
                getApiPrefs().getExcluded(), /*genre exclude*/
                "id-desc", /*sort*/
                null, /*order*/
                true, /*airing data*/
                false, /*full page true: no pagination; false: paginate using the page variable*/
                page);/*page*/

        new HomePageFetch(callback, searchModel, mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String decodeStatus(String status) {
        return KeyUtils.MangaStatusTypes[Arrays.asList(KeyUtils.AnimeStatusTypes).indexOf(status)];
    }
}
