package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by max on 2018/03/20.
 */

public interface BaseModel {

    @POST("/")
    @GraphQuery("Genres")
    @Headers("Content-Type: application/json")
    Call<List<String>> getGenres(@Body GraphQueryContainer request);

    @POST("/")
    @GraphQuery("Tags")
    @Headers("Content-Type: application/json")
    Call<List<MediaTag>> getTags(@Body GraphQueryContainer request);
}
