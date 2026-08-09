package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.data.mapper.toCharacterRecord
import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.graphql.generated.CharacterActors
import com.mxt.anitrend.graphql.generated.CharacterBase
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterMedia
import com.mxt.anitrend.graphql.generated.CharacterOverview
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffSort
import com.mxt.anitrend.model.api.retro.anilist.CharacterService
import com.mxt.anitrend.model.entity.anilist.MediaCharacter
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.mapper.toMediaCharacterEntity
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity

class CharacterRepository(
    private val characterService: CharacterService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository(ioDispatcher) {

    suspend fun getCharacterBase(id: Long): Result<CharacterRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterBase.request(id = id.toInt())
            val response = characterService.getCharacterBase(request)
            if (response.isSuccessful) {
                handleCharacterBase(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleCharacterBase(body: GraphContainer<CharacterBaseData>): CharacterRecord {
        val graphErrors = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.character?.toCharacterRecord() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getCharacterOverview(id: Long, asHtml: Boolean = false): Result<MediaCharacter> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterOverview.request(id = id.toInt(), asHtml = asHtml)
            val response = characterService.getCharacterOverview(request)
            if (response.isSuccessful) {
                handleCharacterOverview(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleCharacterOverview(body: GraphContainer<CharacterOverviewData>): MediaCharacter {
        val graphErrors = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.toMediaCharacterEntity() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getCharacterMedia(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<MediaSort>? = null,
        type: MediaType? = null,
    ): Result<ConnectionContainer<PageContainer<MediaEntity>>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterMedia.request(id = id.toInt(), page = page, perPage = perPage, sort = sort, type = type)
            val response = characterService.getCharacterMedia(request)
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getCharacterActors(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<StaffSort>? = null,
    ): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterActors.request(id = id.toInt(), page = page, perPage = perPage, sort = sort)
            val response = characterService.getCharacterActors(request)
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
