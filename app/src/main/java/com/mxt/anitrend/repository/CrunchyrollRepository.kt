package com.mxt.anitrend.repository

import com.mxt.anitrend.model.api.retro.crunchy.EpisodeService
import com.mxt.anitrend.model.entity.crunchy.Rss
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call

class CrunchyrollRepository(
    private val feedService: EpisodeService,
    private val crunchyrollService: EpisodeService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun getLatestFeed(): Result<Rss> = execute(feedService.latestFeed)

    suspend fun getPopularFeed(): Result<Rss> = execute(feedService.popularFeed)

    suspend fun getRss(link: String?): Result<Rss> = execute(crunchyrollService.getRssByUrl(link))

    private suspend fun execute(call: Call<Rss>): Result<Rss> = withContext(ioDispatcher) {
        runCatching {
            val response = call.execute()
            if (response.isSuccessful) {
                response.body() ?: throw IllegalStateException("Empty response body")
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
