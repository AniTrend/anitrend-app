package com.mxt.anitrend.async;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.call.SeriesModel;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.service.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Maxwell on 10/8/2016.
 */

public class SeriesDetailPageFetch extends AsyncTask<Integer, Void, Call<Series>> {

    private Callback<Series> callback;
    private Context mContext;
    private String mSearch;

    public SeriesDetailPageFetch(Callback<Series> callback, Context mContext, String mSearch) {
        this.callback = callback;
        this.mContext = mContext;
        this.mSearch = mSearch;
    }

    @Override
    protected void onPostExecute(Call<Series> seriesCall) {
        seriesCall.enqueue(callback);
    }

    @Override
    protected Call<Series> doInBackground(Integer... ids) {
         SeriesModel model = ServiceGenerator.createService(SeriesModel.class, mContext);
        return model.fetchSeriesPage(mSearch, ids[0]);
    }
}
