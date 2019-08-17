package com.mxt.anitrend.model.api.retro.crunchy

import com.mxt.anitrend.model.entity.crunchy.Rss

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by max on 2017/10/22.
 */

interface EpisodeModel {

    @get:GET("crunchyroll/rss/anime/popular?format=xml")
    val popularFeed: Call<Rss>

    @get:GET("crunchyroll/rss/anime")
    val latestFeed: Call<Rss>

    @GET("/{path}")
    fun getRSS(
            @Path("path") link: String?
    ): Call<Rss>
}
