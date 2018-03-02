package com.mxt.anitrend.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.SeriesModel;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.Tag;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.KeyUtils;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

/**
 * Created by max on 2017/10/24.
 * Genre & Tags Service
 */

public class TagGenreService extends IntentService {

    private static final String ServiceName = "TagGenreService";

    public TagGenreService() {
        super(ServiceName);
    }

    public final void initAnalytics() {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        analytics.setAnalyticsCollectionEnabled(true);
        analytics.setMinimumSessionDuration(KeyUtils.DURATION_LONG);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            initAnalytics();
            BasePresenter basePresenter = new BasePresenter(getApplicationContext());
            SeriesModel seriesModel = WebFactory.createService(SeriesModel.class, getApplicationContext());
            if(basePresenter.getDatabase().getBoxStore(Tag.class).count() < 1) {
                Response<List<Tag>> tagsResponse = seriesModel.getTags().execute();

                if (tagsResponse.isSuccessful() && tagsResponse.body() != null)
                    basePresenter.getDatabase().saveTags(tagsResponse.body());
                else
                    Log.e(ServiceName, ErrorUtil.getError(tagsResponse));
            }
            if(basePresenter.getDatabase().getBoxStore(Genre.class).count() < 1) {
                Response<List<Genre>> genreResponse = seriesModel.getGenres().execute();
                if (genreResponse.isSuccessful() && genreResponse.body() != null)
                    basePresenter.getDatabase().saveGenres(genreResponse.body());
                else
                    Log.e(ServiceName, ErrorUtil.getError(genreResponse));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
