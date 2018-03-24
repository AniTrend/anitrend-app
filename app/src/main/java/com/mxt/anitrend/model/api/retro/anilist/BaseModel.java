package com.mxt.anitrend.model.api.retro.anilist;

import com.mxt.anitrend.base.custom.annotation.GraphQuery;
import com.mxt.anitrend.model.entity.anilist.MediaTag;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import java.util.List;

import okhttp3.ResponseBody;
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
    Call<List<String>> getGenres(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("Tags")
    @Headers("Content-Type: application/json")
    Call<List<MediaTag>> getTags(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("ToggleLike")
    @Headers("Content-Type: application/json")
    Call<List<UserBase>> toggleLike(@Body QueryContainerBuilder request);

    @POST("/")
    @GraphQuery("ToggleFavourite")
    @Headers("Content-Type: application/json")
    Call<ResponseBody> toggleFavourite(@Body QueryContainerBuilder request);
}
