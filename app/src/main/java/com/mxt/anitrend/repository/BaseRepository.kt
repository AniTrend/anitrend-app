package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.EmptyGraphQLVariables
import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.GenreCollection
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.graphql.generated.MediaTagCollection
import com.mxt.anitrend.graphql.generated.ToggleFavourite
import com.mxt.anitrend.graphql.generated.ToggleLike
import com.mxt.anitrend.model.api.retro.anilist.BaseModel
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.UserBase as UserEntity

sealed class BaseMutation {
    data class LikeToggled(val users: List<UserEntity>) : BaseMutation()
    data class FavouriteToggled(val result: Any) : BaseMutation()
}

class BaseRepository(
    private val baseService: BaseModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val _mutationEvents = MutableSharedFlow<BaseMutation>(replay = 1, extraBufferCapacity = 64)
    val mutationEvents: SharedFlow<BaseMutation> = _mutationEvents.asSharedFlow()

    fun emitMutationEvent(event: BaseMutation) {
        _mutationEvents.tryEmit(event)
    }

    private fun <T> handleGraphResponse(body: com.mxt.anitrend.model.entity.container.body.AniListContainer<T>): T {
        val graphErrors: List<GraphError>? = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.result ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getGenres(): Result<List<String>> = withContext(ioDispatcher) {
        runCatching {
            val request: GraphQLRequest<EmptyGraphQLVariables> = GraphQLRequest(
                query = GenreCollection.document,
                operationName = GenreCollection.name,
            )
            val response = baseService.getGenres(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getTags(): Result<List<MediaTag>> = withContext(ioDispatcher) {
        runCatching {
            val request: GraphQLRequest<EmptyGraphQLVariables> = GraphQLRequest(
                query = MediaTagCollection.document,
                operationName = MediaTagCollection.name,
            )
            val response = baseService.getTags(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun toggleLike(id: Long, type: LikeableType): Result<List<UserEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = ToggleLike.request(id = id.toInt(), type = type)
            val response = baseService.toggleLike(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                _mutationEvents.emit(BaseMutation.LikeToggled(result))
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun toggleFavourite(
        animeId: Int? = null,
        mangaId: Int? = null,
        characterId: Int? = null,
        staffId: Int? = null,
        studioId: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val request = ToggleFavourite.request(animeId = animeId, mangaId = mangaId, characterId = characterId, staffId = staffId, studioId = studioId, page = page, perPage = perPage)
            val response = baseService.toggleFavourite(request).execute()
            if (!response.isSuccessful) {
                throw RuntimeException(response.apiError())
            }
            _mutationEvents.emit(BaseMutation.FavouriteToggled(Unit))
        }
    }
}
