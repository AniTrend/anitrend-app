package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.call.SeriesModel;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.Search;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Maxwell on 10/7/2016.
 * "anime",
 * 2016,
 * "summer",
 * null e.g movie,
 * KeyUtils.AnimeStatusTypes[KeyUtils.AnimeStatusType.FINISHED_AIRING.ordinal()],
 * null genre,
 * null, genre exclude
 * "popularity-desc", sort
 * true, airing data
 * true, full page
 * null); page
*/

public class HomePageFetch extends AsyncTask<Void,Void,Call<List<Series>>> {

    private Callback<List<Series>> callback;
    private volatile Search custom;
    private Context mContext;

    public HomePageFetch(Callback<List<Series>> callback, Search custom, Context mContext) {
        this.callback = callback;
        this.custom = custom;
        this.mContext = mContext;
    }

    @Override
    protected Call<List<Series>> doInBackground(Void... voids) {
        SeriesModel seriesModel = ServiceGenerator.createService(SeriesModel.class, mContext);
        return seriesModel.fetchFileteredBrowsable(custom.getSeries_type(),
                                                   custom.getYear(),
                                                   custom.getSeason()/*"summer"*/,
                                                   custom.getItem_type()/*e.g movie*/,
                                                   custom.getItem_status(),
                                                   custom.getGenre() /*genre*/,
                                                   custom.getGenre_exclude(), /*genre exclude*/
                                                   custom.getSort_by(), /*sort*/
                                                   custom.getAiring_data(), /*airing data*/
                                                   custom.getFull_page(), /*full page*/
                                                   custom.getPage()); /*page*/
    }

    @Override
    protected void onPostExecute(Call<List<Series>> listCall) {
        listCall.enqueue(callback);
    }
}
