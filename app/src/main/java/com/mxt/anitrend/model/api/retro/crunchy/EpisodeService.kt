package com.mxt.anitrend.model.api.retro.crunchy

import com.mxt.anitrend.model.entity.crunchy.Rss
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by max on 2017/10/22.
 */

interface EpisodeService {
    @GET("crunchyroll/rss/popular?format=xml")
    suspend fun getPopularFeed(): Response<Rss>

    @GET("crunchyroll/rss")
    suspend fun getLatestFeed(): Response<Rss>

    @GET
    suspend fun getRssByUrl(
        @Url link: String?,
    ): Response<Rss>
}
