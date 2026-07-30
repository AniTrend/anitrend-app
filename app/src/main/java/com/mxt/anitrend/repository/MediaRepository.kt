package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.MediaBase
import com.mxt.anitrend.graphql.generated.MediaCharacters
import com.mxt.anitrend.graphql.generated.MediaEpisodes
import com.mxt.anitrend.graphql.generated.MediaOverview
import com.mxt.anitrend.graphql.generated.MediaRelations
import com.mxt.anitrend.graphql.generated.MediaSocial
import com.mxt.anitrend.graphql.generated.MediaStaff
import com.mxt.anitrend.graphql.generated.MediaStats
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.RecommendationMedia
import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.mapper.toPageInfoRecord
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.base.RecommendationBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity

class MediaRepository(
    private val mediaService: MediaService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val feedStore: FeedStore? = null,
) : AbstractRepository(ioDispatcher) {

    suspend fun getMediaBase(id: Long, type: MediaType?, isAdult: Boolean?): Result<MediaEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaBase.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaBase(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaOverview(id: Long, type: MediaType?, isAdult: Boolean?, asHtml: Boolean = false): Result<Media> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaOverview.request(id = id.toInt(), type = type, isAdult = isAdult, asHtml = asHtml)
            val response = mediaService.getMediaOverview(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaRelations(id: Long, type: MediaType?, isAdult: Boolean?): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaRelations.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaRelations(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaStats(id: Long, type: MediaType?, isAdult: Boolean?): Result<Media> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaStats.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaStats(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaEpisodes(id: Long, type: MediaType?, isAdult: Boolean?): Result<ConnectionContainer<List<ExternalLink>>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaEpisodes.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaEpisodes(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaCharacters(
        id: Long,
        type: MediaType?,
        isAdult: Boolean?,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.CharacterSort>? = null,
    ): Result<ConnectionContainer<EdgeContainer<CharacterEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaCharacters.request(id = id.toInt(), type = type, isAdult = isAdult, page = page, perPage = perPage, sort = sort)
            val response = mediaService.getMediaCharacters(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaStaff(
        id: Long,
        type: MediaType?,
        isAdult: Boolean?,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.StaffSort>? = null,
    ): Result<ConnectionContainer<EdgeContainer<StaffEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaStaff.request(id = id.toInt(), type = type, sort = sort, isAdult = isAdult, page = page, perPage = perPage)
            val response = mediaService.getMediaStaff(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaRecommendations(
        id: Long,
        type: MediaType?,
        isAdult: Boolean?,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.RecommendationSort>? = null,
    ): Result<ConnectionContainer<PageContainer<RecommendationBase>>> = withContext(ioDispatcher) {
        runCatching {
            val request = RecommendationMedia.request(id = id.toInt(), type = type, isAdult = isAdult, page = page, perPage = perPage, sort = sort)
            val response = mediaService.getMediaRecommendations(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaSocial(
        mediaId: Long,
        isFollowing: Boolean = true,
        page: Int? = null,
        perPage: Int? = null,
        commitToStore: Boolean = true,
        queryKey: FeedQueryKey? = null,
        queryGeneration: Int = 0,
    ): Result<PageContainer<FeedList>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaSocial.request(mediaId = mediaId.toInt(), isFollowing = isFollowing, page = page, perPage = perPage)
            val response = mediaService.getMediaSocial(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                val pageInfo = result.takeIf { it.hasPageInfo() }?.pageInfo?.toPageInfoRecord()
                val resolvedQueryKey = queryKey ?: FeedQueryKey(
                    scope = FeedScope.MEDIA,
                    userId = null,
                    mediaId = mediaId,
                    activityType = null,
                    isFollowing = isFollowing,
                    isMixed = null,
                )

                if (commitToStore && queryKey != null && feedStore != null) {
                    feedStore.apply(
                        FeedStoreChange.PageLoaded(
                            queryKey = resolvedQueryKey,
                            page = pageInfo?.currentPage ?: page ?: 1,
                            generation = queryGeneration,
                            feeds = result.pageData.map { it.toFeedRecord(revision = 0L) },
                            pageInfo = pageInfo,
                        ),
                    )
                }

                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
