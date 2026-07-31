package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffBase
import com.mxt.anitrend.graphql.generated.StaffCharacters
import com.mxt.anitrend.graphql.generated.StaffMedia
import com.mxt.anitrend.graphql.generated.StaffOverview
import com.mxt.anitrend.graphql.generated.StaffRoles
import com.mxt.anitrend.model.api.retro.anilist.StaffService
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity
import com.mxt.anitrend.model.entity.base.StaffBase as StaffEntity

class StaffRepository(
    private val staffService: StaffService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository(ioDispatcher) {

    suspend fun getStaffBase(id: Long): Result<StaffEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffBase.request(id = id.toInt())
            val response = staffService.getStaffBase(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getStaffOverview(id: Long, asHtml: Boolean = false): Result<StaffEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffOverview.request(id = id.toInt(), asHtml = asHtml)
            val response = staffService.getStaffOverview(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getStaffCharacters(
        id: Long,
        onList: Boolean? = null,
        page: Int? = null,
        sort: List<MediaSort>? = null,
    ): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffCharacters.request(id = id.toInt(), onList = onList, page = page, sort = sort)
            val response = staffService.getStaffCharacters(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getStaffMedia(
        id: Long,
        onList: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
        type: MediaType? = null,
    ): Result<ConnectionContainer<PageContainer<MediaEntity>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffMedia.request(id = id.toInt(), onList = onList, page = page, perPage = perPage, sort = sort, type = type)
            val response = staffService.getStaffMedia(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getStaffRoles(
        id: Long,
        onList: Boolean? = null,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
        type: MediaType? = null,
    ): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffRoles.request(id = id.toInt(), onList = onList, page = page, perPage = perPage, sort = sort, type = type)
            val response = staffService.getStaffRoles(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
