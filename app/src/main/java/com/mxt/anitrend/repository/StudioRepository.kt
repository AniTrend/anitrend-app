package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.data.mapper.toStudioRecord
import com.mxt.anitrend.domain.model.StudioRecord
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.StudioBase
import com.mxt.anitrend.graphql.generated.StudioBaseData
import com.mxt.anitrend.graphql.generated.StudioMedia
import com.mxt.anitrend.graphql.generated.StudioMediaData
import com.mxt.anitrend.model.api.retro.anilist.StudioService
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.mapper.toStudioMediaConnection
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity

class StudioRepository(
    private val studioService: StudioService,
    private val settings: Settings,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository(ioDispatcher) {

    suspend fun getStudioBase(id: Long): Result<StudioRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioBase.request(id = id.toInt())
            val response = studioService.getStudioBase(request)
            if (response.isSuccessful) {
                handleStudioBase(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStudioBase(body: GraphContainer<StudioBaseData>): StudioRecord {
        val graphErrors = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.studio?.toStudioRecord() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getStudioMedia(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
    ): Result<ConnectionContainer<PageContainer<MediaEntity>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioMedia.request(id = id.toInt(), page = page, perPage = perPage, sort = sort)
            val response = studioService.getStudioMedia(request)
            if (response.isSuccessful) {
                handleStudioMedia(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    fun isAuthenticated() = settings.isAuthenticated

    private fun handleStudioMedia(body: GraphContainer<StudioMediaData>): ConnectionContainer<PageContainer<MediaEntity>> {
        val graphErrors = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.toStudioMediaConnection() ?: throw IllegalStateException("Empty response body")
    }
}
