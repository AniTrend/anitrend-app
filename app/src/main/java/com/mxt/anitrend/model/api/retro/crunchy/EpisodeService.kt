package com.mxt.anitrend.model.api.retro.crunchy

import com.mxt.anitrend.model.entity.crunchy.Rss
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by max on 2017/10/22.
 */

interface EpisodeService {
    @get:GET("crunchyroll/rss/popular?format=xml")
    val popularFeed: Call<Rss>

    @get:GET("crunchyroll/rss")
    val latestFeed: Call<Rss>

    @GET
    fun getRssByUrl(
        @Url link: String?,
    ): Call<Rss>
}
