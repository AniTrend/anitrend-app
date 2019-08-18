package com.mxt.anitrend.model.api.retro.base

import com.mxt.anitrend.model.entity.giphy.GiphyContainer

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by max on 2017/12/09.
 * giphy request end point
 */

interface GiphyModel {

    @GET("search")
    fun findGif(
            @Query("api_key") api_key: String, @Query("q") q: String?,
            @Query("limit") limit: Int, @Query("offset") offset: Int?,
            @Query("rating") rating: String, @Query("lang") lang: String?
    ): Call<GiphyContainer>

    @GET("trending")
    fun getTrending(
            @Query("api_key") api_key: String, @Query("limit") limit: Int?,
            @Query("offset") offset: Int, @Query("rating") rating: String?
    ): Call<GiphyContainer>
}
