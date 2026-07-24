package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.StudioBase
import com.mxt.anitrend.graphql.generated.StudioMedia
import com.mxt.anitrend.model.api.retro.anilist.StudioModel
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity
import com.mxt.anitrend.model.entity.base.StudioBase as StudioEntity

sealed class StudioMutation {
    // No mutation operations yet. Add event types when mutation methods are added.
    data object Noop : StudioMutation()
}

class StudioRepository(
    private val studioService: StudioModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val _mutationEvents = MutableSharedFlow<StudioMutation>(replay = 1, extraBufferCapacity = 64)
    val mutationEvents: SharedFlow<StudioMutation> = _mutationEvents.asSharedFlow()

    private fun <T> handleGraphResponse(body: com.mxt.anitrend.model.entity.container.body.AniListContainer<T>): T {
        val graphErrors: List<GraphError>? = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.result ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getStudioBase(id: Long): Result<StudioEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioBase.request(id = id.toInt())
            val response = studioService.getStudioBase(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getStudioMedia(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
    ): Result<ConnectionContainer<PageContainer<MediaEntity>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioMedia.request(id = id.toInt(), page = page, perPage = perPage, sort = sort)
            val response = studioService.getStudioMedia(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
