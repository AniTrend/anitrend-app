package com.mxt.anitrend.presenter.detail;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.github.johnpersano.supertoasts.library.Style;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.base.custom.async.SeriesDetailPageFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import retrofit2.Callback;

/**
 * Created by Maxwell on 10/8/2016.
 */
public class SeriesPresenter extends CommonPresenter<Series> {

    private String series_type;
    private Context mContext;

    public SeriesPresenter(String series_type, Context mContext) {
        super(mContext);
        this.series_type = series_type;
        this.mContext = mContext;
    }

    @Override
    public void beginAsync(Callback<Series> callback, int id) {
        SeriesDetailPageFetch seriesDetailPageFetch = new SeriesDetailPageFetch(callback, mContext, series_type);
        seriesDetailPageFetch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
    }

    public String getSeason(Integer passing){
        if(passing != null && passing > 99) {
            String rep = String.valueOf(passing);

            String fullyear;

            int year = Integer.valueOf(rep.substring(0,2));

            if(year >= 99)
                fullyear = "19"+year;
            else
                fullyear = "20"+year;

            switch (rep.substring(2)) {
                case "1":
                    return fullyear+" Winter";
                case "2":
                    return fullyear+" Spring";
                case "3":
                    return fullyear+" Summer";
                case "4":
                    return fullyear+" Fall";
                default:
                    return "TBA";
            }
        }
        return "N/A";
    }

    public String getGenres(String[] genres){
        if(genres == null)
            return "Unknown";

        String genre_types = "";

        for (int i = 0; i <= genres.length -1; i++) {
            if(i == 0)
                genre_types += genres[i];
            else
                genre_types += ", "+genres[i];
        }
        return genre_types;
    }

    public void displayMessage(String msg, Activity activity) {
        createSuperToast(activity, msg, R.drawable.ic_info_outline_white_18dp, Style.TYPE_STANDARD);
    }
}
