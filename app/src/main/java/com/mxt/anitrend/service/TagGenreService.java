package com.mxt.anitrend.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.BaseModel;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;

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
        analytics.setMinimumSessionDuration(KeyUtil.DURATION_LONG);
    }

    private void fetchAllMediaTags(BaseModel baseModel, BasePresenter basePresenter) throws IOException {
        if(basePresenter.getDatabase().getBoxStore(MediaTag.class).count() < 1) {
            Response<GraphContainer<List<MediaTag>>> graphContainerResponse = baseModel
                    .getTags(GraphUtil.getDefaultQuery(false))
                    .execute();

            GraphContainer<List<MediaTag>> graphContainer;
            if (graphContainerResponse.isSuccessful() && (graphContainer = graphContainerResponse.body()) != null)
                if(!graphContainer.isEmpty() && !graphContainer.getData().isEmpty())
                    basePresenter.getDatabase().saveMediaTags(graphContainer.getData().getResult());
                else
                    Log.e(ServiceName, ErrorUtil.getError(graphContainerResponse));
        }
    }

    private void fetchAllMediaGenres(BaseModel baseModel, BasePresenter basePresenter) throws IOException {
        if(basePresenter.getDatabase().getBoxStore(Genre.class).count() < 1) {
            Response<GraphContainer<List<String>>> graphContainerResponse = baseModel
                    .getGenres(GraphUtil.getDefaultQuery(false))
                    .execute();

            GraphContainer<List<String>> graphContainer;
            if (graphContainerResponse.isSuccessful() && (graphContainer = graphContainerResponse.body()) != null) {
                if(!graphContainer.isEmpty() && !graphContainer.getData().isEmpty()) {
                    List<Genre> genreList = Stream.of(graphContainer.getData().getResult())
                            .map(Genre::new)
                            .toList();
                    basePresenter.getDatabase()
                            .saveGenreCollection(genreList);
                }
            }
            else
                Log.e(ServiceName, ErrorUtil.getError(graphContainerResponse));
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            initAnalytics();
            BasePresenter basePresenter = new BasePresenter(getApplicationContext());
            BaseModel baseModel = WebFactory.createService(BaseModel.class, getApplicationContext());
            fetchAllMediaGenres(baseModel, basePresenter);
            fetchAllMediaTags(baseModel, basePresenter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
