package com.mxt.anitrend.presenter.index;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.api.structure.Search;
import com.mxt.anitrend.async.HomePageFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import java.util.List;

import retrofit2.Callback;

import static com.mxt.anitrend.api.structure.FilterTypes.SeasonTitles;

/**
 * Created by Maxwell on 10/15/2016.
 */
public class FragmentPresenter extends CommonPresenter<List<Series>> {

    private Context mContext;

    public FragmentPresenter(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }

    @Override
    public void beginAsync(Callback<List<Series>> callback, int viewPosition) {
        new HomePageFetch(callback, new Search(FilterTypes.SeriesTypes[FilterTypes.SeriesType.ANIME.ordinal()], /*anime or manga*/
                getApiPrefs().getYear(), /*year*/
                SeasonTitles[viewPosition], /*season*/
                getApiPrefs().getShowType(), /*Type e.g. TV or Movie e.t.c*/
                getApiPrefs().getStatus(), /*status*/
                getApiPrefs().getGenres(), /*genre */
                getApiPrefs().getExcluded(), /*genre exclude*/
                getApiPrefs().getSort(), /*sort*/
                getApiPrefs().getOrder(), /*order*/
                true, /*airing data*/
                false, /*full page true: no pagination; false: paginate using the page variable*/
                null)/*page*/, mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void beginAsync(Callback<List<Series>> callback, int viewPosition, int page) {
        new HomePageFetch(callback, new Search(FilterTypes.SeriesTypes[FilterTypes.SeriesType.ANIME.ordinal()],
                getApiPrefs().getYear(),
                FilterTypes.SeasonTitles[viewPosition],
                getApiPrefs().getShowType(),
                getApiPrefs().getStatus(),
                getApiPrefs().getGenres(),
                getApiPrefs().getExcluded(),
                getApiPrefs().getSort(),
                getApiPrefs().getOrder(),
                true,
                false,
                page), mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void beginAsync(Callback<List<Series>> callback, Search searchModel) {
        new HomePageFetch(callback, searchModel, mContext).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
}
