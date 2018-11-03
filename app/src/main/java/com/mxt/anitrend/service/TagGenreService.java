package com.mxt.anitrend.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.annimon.stream.Stream;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

import retrofit2.Call;
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

    private void fetchAllMediaTags() {
        WidgetPresenter<List<MediaTag>> widgetPresenter = new WidgetPresenter<>(getApplicationContext());
        if(widgetPresenter.getDatabase().getBoxStore(MediaTag.class).count() < 1) {
            widgetPresenter.getParams().putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(false));
            widgetPresenter.requestData(KeyUtil.MEDIA_TAG_REQ, getApplicationContext(), new RetroCallback<List<MediaTag>>() {
                @Override
                public void onResponse(@NonNull Call<List<MediaTag>> call, @NonNull Response<List<MediaTag>> response) {
                    List<MediaTag> responseBody;
                    if (response.isSuccessful() && (responseBody = response.body()) != null)
                        if (!CompatUtil.isEmpty(responseBody))
                            widgetPresenter.getDatabase().saveMediaTags(responseBody);
                        else
                            Log.e(ServiceName, ErrorUtil.getError(response));
                }

                @Override
                public void onFailure(@NonNull Call<List<MediaTag>> call, @NonNull Throwable throwable) {
                    Log.e("fetchAllMediaTags", throwable.getMessage());
                    throwable.printStackTrace();
                }
            });
        }
    }

    private void fetchAllMediaGenres() {
        WidgetPresenter<List<String>> widgetPresenter = new WidgetPresenter<>(getApplicationContext());
        if(widgetPresenter.getDatabase().getBoxStore(Genre.class).count() < 1) {
            widgetPresenter.getParams().putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(false));
            widgetPresenter.requestData(KeyUtil.GENRE_COLLECTION_REQ, getApplicationContext(), new RetroCallback<List<String>>() {
                @Override
                public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                    List<String> responseBody;
                    if (response.isSuccessful() && (responseBody = response.body()) != null) {
                        if (!CompatUtil.isEmpty(responseBody)) {
                            List<Genre> genreList = Stream.of(responseBody)
                                    .map(Genre::new)
                                    .toList();
                            widgetPresenter.getDatabase().saveGenreCollection(genreList);
                        }
                    } else
                        Log.e(ServiceName, ErrorUtil.getError(response));
                }

                @Override
                public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable throwable) {
                    Log.e("fetchAllMediaGenres", throwable.getMessage());
                    throwable.printStackTrace();
                }
            });
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        fetchAllMediaGenres();
        fetchAllMediaTags();
    }
}
