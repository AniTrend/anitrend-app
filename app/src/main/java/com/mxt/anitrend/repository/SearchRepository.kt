package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.CharacterSearch
import com.mxt.anitrend.graphql.generated.CharacterSort
import com.mxt.anitrend.graphql.generated.MediaSearch
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffSearch
import com.mxt.anitrend.graphql.generated.StaffSort
import com.mxt.anitrend.graphql.generated.StudioSearch
import com.mxt.anitrend.graphql.generated.StudioSort
import com.mxt.anitrend.graphql.generated.UserSearch
import com.mxt.anitrend.graphql.generated.UserSort
import com.mxt.anitrend.model.api.retro.anilist.SearchModel
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.CharacterBase as CharacterEntity
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity
import com.mxt.anitrend.model.entity.base.StaffBase as StaffEntity
import com.mxt.anitrend.model.entity.base.StudioBase as StudioEntity
import com.mxt.anitrend.model.entity.base.UserBase as UserEntity

sealed class SearchMutation {
    // No mutation operations yet. Add event types when mutation methods are added.
    data object Noop : SearchMutation()
}

class SearchRepository(
    private val searchService: SearchModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val _mutationEvents = MutableSharedFlow<SearchMutation>(replay = 1, extraBufferCapacity = 64)
    val mutationEvents: SharedFlow<SearchMutation> = _mutationEvents.asSharedFlow()

    private fun <T> handleGraphResponse(body: com.mxt.anitrend.model.entity.container.body.AniListContainer<T>): T {
        val graphErrors: List<GraphError>? = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.result ?: throw IllegalStateException("Empty response body")
    }

    suspend fun searchMedia(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        type: MediaType? = null,
        format: com.mxt.anitrend.graphql.generated.MediaFormat? = null,
        startDate: String? = null,
        endDate: String? = null,
        season: com.mxt.anitrend.graphql.generated.MediaSeason? = null,
        genres: List<String?>? = null,
        genresExclude: List<String?>? = null,
        isAdult: Boolean? = null,
        sort: List<MediaSort>? = null,
    ): Result<PageContainer<MediaEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaSearch.request(
                id = id, page = page, perPage = perPage,
                search = search, type = type, format = format,
                startDate = startDate, endDate = endDate,
                season = season, genres = genres,
                genresExclude = genresExclude, isAdult = isAdult,
                sort = sort,
            )
            val response = searchService.getMediaSearch(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchStudio(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<StudioSort>? = null,
    ): Result<PageContainer<StudioEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getStudioSearch(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchStaff(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<StaffSort>? = null,
    ): Result<PageContainer<StaffEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getStaffSearch(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchCharacter(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<CharacterSort>? = null,
    ): Result<PageContainer<CharacterEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getCharacterSearch(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchUser(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<UserSort>? = null,
    ): Result<PageContainer<UserEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getUserSearch(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
