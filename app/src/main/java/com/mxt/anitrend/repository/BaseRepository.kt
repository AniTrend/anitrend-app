package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.EmptyGraphQLVariables
import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.data.mapper.toUserSummaryRecords
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.graphql.generated.GenreCollection
import com.mxt.anitrend.graphql.generated.GenreCollectionData
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.graphql.generated.MediaTagCollection
import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import com.mxt.anitrend.graphql.generated.ToggleFavourite
import com.mxt.anitrend.graphql.generated.ToggleLike
import com.mxt.anitrend.model.api.retro.anilist.BaseService
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.base.NotificationHistory_
import com.mxt.anitrend.repository.mapper.toMediaTags
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.UserBase as UserEntity

sealed class BaseMutation {
    data class LikeToggled(
        val users: List<UserEntity>,
        val targetId: Long,
        val targetType: LikeableType,
    ) : BaseMutation()

    data class FavouriteToggled(
        val result: Any,
        val animeId: Int? = null,
        val mangaId: Int? = null,
        val characterId: Int? = null,
        val staffId: Int? = null,
        val studioId: Int? = null,
    ) : BaseMutation()
}

class BaseRepository(
    private val baseService: BaseService,
    private val boxQuery: BoxQuery,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val feedStore: FeedStore? = null,
) : AbstractRepository<BaseMutation>(ioDispatcher) {

    /** Cached genre collection from local DB. */
    val cachedGenres: List<Genre>
        get() = boxQuery.genreCollection

    /** Cached media tags from local DB. */
    val cachedTags: List<MediaTag>
        get() = boxQuery.mediaTags

    /** Returns true if a notification has been marked as read locally. */
    fun isNotificationRead(notificationId: Long): Boolean = boxQuery.getBoxStore(NotificationHistory::class.java)
        .query(NotificationHistory_.id.equal(notificationId))
        .build()
        .use { query -> query.findFirst() != null }

    suspend fun getGenres(): Result<List<String>> = withContext(ioDispatcher) {
        runCatching {
            val request: GraphQLRequest<EmptyGraphQLVariables> = GraphQLRequest(
                query = GenreCollection.document,
                operationName = GenreCollection.name,
            )
            val response = baseService.getGenres(request).execute()
            if (response.isSuccessful) {
                handleGenreCollection(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleGenreCollection(body: GraphContainer<GenreCollectionData>): List<String> {
        val graphErrors = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.genreCollection?.filterNotNull() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getTags(): Result<List<MediaTag>> = withContext(ioDispatcher) {
        runCatching {
            val request: GraphQLRequest<EmptyGraphQLVariables> = GraphQLRequest(
                query = MediaTagCollection.document,
                operationName = MediaTagCollection.name,
            )
            val response = baseService.getTags(request).execute()
            if (response.isSuccessful) {
                handleMediaTagCollection(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaTagCollection(body: GraphContainer<MediaTagCollectionData>): List<MediaTag> {
        val graphErrors = body.errors
        if (!graphErrors.isNullOrEmpty()) {
            throw RuntimeException(graphErrors.first().message ?: "GraphQL error")
        }
        return body.data?.mediaTagCollection?.toMediaTags() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun toggleLike(
        id: Long,
        type: LikeableType,
        commitToStore: Boolean = true,
        replyFeedId: Long? = null,
        revision: Long = 0L,
    ): Result<List<UserEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = ToggleLike.request(id = id.toInt(), type = type)
            val response = baseService.toggleLike(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore) {
                    val likes = result.toUserSummaryRecords()
                    when (type) {
                        LikeableType.ACTIVITY -> {
                            feedStore?.apply(
                                FeedStoreChange.FeedLikesReplaced(
                                    feedId = id,
                                    likes = likes,
                                    revision = revision,
                                ),
                            )
                        }
                        LikeableType.ACTIVITY_REPLY -> {
                            val parentFeedId = replyFeedId ?: feedStore?.state?.value?.repliesById?.get(id)?.activityId
                            if (parentFeedId != null) {
                                feedStore?.apply(
                                    FeedStoreChange.ReplyLikesReplaced(
                                        feedId = parentFeedId,
                                        replyId = id,
                                        likes = likes,
                                        revision = revision,
                                    ),
                                )
                            }
                        }
                        else -> Unit
                    }
                }
                _mutationEvents.emit(
                    BaseMutation.LikeToggled(
                        users = result,
                        targetId = id,
                        targetType = type,
                    ),
                )
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
            _mutationEvents.emit(
                BaseMutation.FavouriteToggled(
                    result = Unit,
                    animeId = animeId,
                    mangaId = mangaId,
                    characterId = characterId,
                    staffId = staffId,
                    studioId = studioId,
                ),
            )
        }
    }
}
