package com.mxt.anitrend.model.api.retro.base;

import com.mxt.anitrend.model.entity.giphy.GiphyContainer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by max on 2017/12/09.
 * giphy request end point
 */

public interface GiphyModel {

    @GET("search")
    Call<GiphyContainer> findGif(@Query("api_key") String api_key, @Query("q") String q,
                               @Query("limit") int limit, @Query("offset") int offset,
                               @Query("rating") String rating, @Query("lang") String lang);

    @GET("trending")
    Call<GiphyContainer> getTrending(@Query("api_key") String api_key, @Query("limit") int limit,
                                     @Query("offset") int offset, @Query("rating") String rating);
}
