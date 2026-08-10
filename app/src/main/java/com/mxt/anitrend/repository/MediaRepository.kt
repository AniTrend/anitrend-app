package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.graphql.generated.MediaBase
import com.mxt.anitrend.graphql.generated.MediaBaseData
import com.mxt.anitrend.graphql.generated.MediaCharacters
import com.mxt.anitrend.graphql.generated.MediaCharactersData
import com.mxt.anitrend.graphql.generated.MediaEpisodes
import com.mxt.anitrend.graphql.generated.MediaEpisodesData
import com.mxt.anitrend.graphql.generated.MediaOverview
import com.mxt.anitrend.graphql.generated.MediaOverviewData
import com.mxt.anitrend.graphql.generated.MediaRelations
import com.mxt.anitrend.graphql.generated.MediaRelationsData
import com.mxt.anitrend.graphql.generated.MediaSocial
import com.mxt.anitrend.graphql.generated.MediaSocialData
import com.mxt.anitrend.graphql.generated.MediaStaff
import com.mxt.anitrend.graphql.generated.MediaStaffData
import com.mxt.anitrend.graphql.generated.MediaStats
import com.mxt.anitrend.graphql.generated.MediaStatsData
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.RecommendationMedia
import com.mxt.anitrend.graphql.generated.RecommendationMediaData
import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.mapper.toMediaCharactersRecord
import com.mxt.anitrend.data.mapper.toMediaDetailRecord
import com.mxt.anitrend.data.mapper.toMediaEpisodesRecord
import com.mxt.anitrend.data.mapper.toMediaOverviewRecord
import com.mxt.anitrend.data.mapper.toMediaRelationsRecord
import com.mxt.anitrend.data.mapper.toMediaStatsRecord
import com.mxt.anitrend.data.mapper.toMediaStaffRecord
import com.mxt.anitrend.data.mapper.toPageInfoRecord
import com.mxt.anitrend.data.mapper.toRecommendationRecord
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.domain.mediadetail.model.MediaCharactersRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaDetailRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaEpisodesRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaRelationsRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaStatsRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaStaffRecord
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.anilist.edge.StaffEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.mapper.toFeedPage
import com.mxt.anitrend.repository.mapper.toMediaBaseEntity
import com.mxt.anitrend.repository.mapper.toMediaCharactersConnection
import com.mxt.anitrend.repository.mapper.toMediaEpisodesConnection
import com.mxt.anitrend.repository.mapper.toMediaEntity
import com.mxt.anitrend.repository.mapper.toMediaRelationsConnection
import com.mxt.anitrend.repository.mapper.toMediaStaffConnection
import com.mxt.anitrend.repository.mapper.toMediaStatsEntity
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

    // Legacy entity-typed surface for the primary media detail query.
    //
    // Sources from the same generated transport as getMediaBaseRecord below and
    // maps the generated data back to the legacy entity at the repository
    // boundary (see MediaMapper.kt). GraphQL error, empty body/null root, and
    // HTTP failure semantics match the record surface.
    suspend fun getMediaBase(id: Long, type: MediaType?, isAdult: Boolean?): Result<MediaEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaBase.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaBaseRecord(request)
            if (response.isSuccessful) {
                handleMediaBase(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaBase(body: GraphQLResponse<MediaBaseData>): MediaEntity {
        val data = handleGraphQLResponse(body)
        return data.toMediaBaseEntity()
    }

    // Record-typed surface for the primary media detail query (Lane C).
    suspend fun getMediaBaseRecord(id: Long, type: MediaType?, isAdult: Boolean?): Result<MediaDetailRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaBase.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaBaseRecord(request)
            if (response.isSuccessful) {
                handleMediaBaseRecord(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Legacy entity-typed surface for the media overview query.
    //
    // Sources from the same generated transport as getMediaOverviewRecord below
    // and maps the generated data back to the legacy entity at the repository
    // boundary (see MediaMapper.kt). No remaining main-source consumers after
    // the MediaOverviewViewModel migration; kept for the legacy entity lane.
    suspend fun getMediaOverview(id: Long, type: MediaType?, isAdult: Boolean?, asHtml: Boolean = false): Result<Media> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaOverview.request(id = id.toInt(), type = type, isAdult = isAdult, asHtml = asHtml)
            val response = mediaService.getMediaOverviewRecord(request)
            if (response.isSuccessful) {
                handleMediaOverview(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaOverview(body: GraphQLResponse<MediaOverviewData>): Media {
        val data = handleGraphQLResponse(body)
        return data.media?.toMediaEntity() ?: throw IllegalStateException("Empty response body")
    }

    // Record-typed surface for the media overview query (Lane C).
    suspend fun getMediaOverviewRecord(id: Long, type: MediaType?, isAdult: Boolean?, asHtml: Boolean = false): Result<MediaOverviewRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaOverview.request(id = id.toInt(), type = type, isAdult = isAdult, asHtml = asHtml)
            val response = mediaService.getMediaOverviewRecord(request)
            if (response.isSuccessful) {
                handleMediaOverviewRecord(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Legacy entity-typed surface for the media relations query.
    //
    // Sources from the same generated transport as getMediaRelationsRecord below
    // and maps the generated data back to the legacy connection/edge entities at
    // the repository boundary (see MediaMapper.kt). Remaining consumers
    // (MediaRelationViewModel).
    suspend fun getMediaRelations(id: Long, type: MediaType?, isAdult: Boolean?): Result<ConnectionContainer<EdgeContainer<MediaEdge>>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaRelations.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaRelationsRecord(request)
            if (response.isSuccessful) {
                handleMediaRelations(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaRelations(body: GraphQLResponse<MediaRelationsData>): ConnectionContainer<EdgeContainer<MediaEdge>> {
        val data = handleGraphQLResponse(body)
        return data.toMediaRelationsConnection()
    }

    // Record-typed surface for the media relations query (Lane C).
    suspend fun getMediaRelationsRecord(id: Long, type: MediaType?, isAdult: Boolean?): Result<MediaRelationsRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaRelations.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaRelationsRecord(request)
            if (response.isSuccessful) {
                handleMediaRelationsRecord(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Legacy entity-typed surface for the media stats query.
    //
    // Sources from the same generated transport as getMediaStatsRecord below and
    // maps the generated data back to the legacy entity at the repository
    // boundary (see MediaMapper.kt). Remaining consumers (MediaStatsViewModel).
    suspend fun getMediaStats(id: Long, type: MediaType?, isAdult: Boolean?): Result<Media> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaStats.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaStatsRecord(request)
            if (response.isSuccessful) {
                handleMediaStats(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaStats(body: GraphQLResponse<MediaStatsData>): Media {
        val data = handleGraphQLResponse(body)
        return data.media?.toMediaStatsEntity() ?: throw IllegalStateException("Empty response body")
    }

    // Record-typed surface for the media stats query (Lane C).
    suspend fun getMediaStatsRecord(id: Long, type: MediaType?, isAdult: Boolean?): Result<MediaStatsRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaStats.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaStatsRecord(request)
            if (response.isSuccessful) {
                handleMediaStatsRecord(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Legacy entity-typed surface for the media episodes query.
    //
    // Sources from the same generated transport as getMediaEpisodesRecord below
    // and maps the generated data back to the legacy connection entity at the
    // repository boundary (see MediaMapper.kt). Remaining consumers
    // (WatchListFragment).
    suspend fun getMediaEpisodes(id: Long, type: MediaType?, isAdult: Boolean?): Result<ConnectionContainer<List<ExternalLink>>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaEpisodes.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaEpisodesRecord(request)
            if (response.isSuccessful) {
                handleMediaEpisodes(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaEpisodes(body: GraphQLResponse<MediaEpisodesData>): ConnectionContainer<List<ExternalLink>> {
        val data = handleGraphQLResponse(body)
        return data.toMediaEpisodesConnection()
    }

    // Record-typed surface for the media episodes query (Lane C).
    suspend fun getMediaEpisodesRecord(id: Long, type: MediaType?, isAdult: Boolean?): Result<MediaEpisodesRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaEpisodes.request(id = id.toInt(), type = type, isAdult = isAdult)
            val response = mediaService.getMediaEpisodesRecord(request)
            if (response.isSuccessful) {
                handleMediaEpisodesRecord(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Legacy entity-typed surface for the media characters query.
    //
    // Sources from the same generated transport as getMediaCharactersRecord below
    // and maps the generated data back to the legacy connection/edge entities at
    // the repository boundary (see MediaMapper.kt). Remaining consumers
    // (MediaCharacterViewModel).
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
            val response = mediaService.getMediaCharactersRecord(request)
            if (response.isSuccessful) {
                handleMediaCharacters(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaCharacters(body: GraphQLResponse<MediaCharactersData>): ConnectionContainer<EdgeContainer<CharacterEdge>> {
        val data = handleGraphQLResponse(body)
        return data.toMediaCharactersConnection()
    }

    // Record-typed surface for the media characters query (Lane C).
    suspend fun getMediaCharactersRecord(
        id: Long,
        type: MediaType?,
        isAdult: Boolean?,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.CharacterSort>? = null,
    ): Result<MediaCharactersRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaCharacters.request(id = id.toInt(), type = type, isAdult = isAdult, page = page, perPage = perPage, sort = sort)
            val response = mediaService.getMediaCharactersRecord(request)
            if (response.isSuccessful) {
                handleMediaCharactersRecord(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Legacy entity-typed surface for the media staff query.
    //
    // Sources from the same generated transport as getMediaStaffRecord below and
    // maps the generated data back to the legacy connection/edge entities at the
    // repository boundary (see MediaMapper.kt). Remaining consumers
    // (MediaStaffViewModel).
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
            val response = mediaService.getMediaStaffRecord(request)
            if (response.isSuccessful) {
                handleMediaStaff(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaStaff(body: GraphQLResponse<MediaStaffData>): ConnectionContainer<EdgeContainer<StaffEdge>> {
        val data = handleGraphQLResponse(body)
        return data.toMediaStaffConnection()
    }

    // Record-typed surface for the media staff query (Lane C).
    suspend fun getMediaStaffRecord(
        id: Long,
        type: MediaType?,
        isAdult: Boolean?,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.StaffSort>? = null,
    ): Result<MediaStaffRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaStaff.request(id = id.toInt(), type = type, sort = sort, isAdult = isAdult, page = page, perPage = perPage)
            val response = mediaService.getMediaStaffRecord(request)
            if (response.isSuccessful) {
                handleMediaStaffRecord(response.body() ?: throw IllegalStateException("Empty response body"))
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
    ): Result<RecommendationPageResult> = withContext(ioDispatcher) {
        runCatching {
            val request = RecommendationMedia.request(id = id.toInt(), type = type, isAdult = isAdult, page = page, perPage = perPage, sort = sort)
            val response = mediaService.getMediaRecommendations(request)
            if (response.isSuccessful) {
                handleMediaRecommendations(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaBaseRecord(body: GraphQLResponse<MediaBaseData>): MediaDetailRecord {
        val data = handleGraphQLResponse(body)
        val media = data.media
            ?: throw IllegalStateException("Empty response body")
        return media.toMediaDetailRecord()
    }

    private fun handleMediaOverviewRecord(body: GraphQLResponse<MediaOverviewData>): MediaOverviewRecord {
        val data = handleGraphQLResponse(body)
        val media = data.media
            ?: throw IllegalStateException("Empty response body")
        return media.toMediaOverviewRecord()
    }

    private fun handleMediaStatsRecord(body: GraphQLResponse<MediaStatsData>): MediaStatsRecord {
        val data = handleGraphQLResponse(body)
        val media = data.media
            ?: throw IllegalStateException("Empty response body")
        return media.toMediaStatsRecord()
    }

    private fun handleMediaEpisodesRecord(body: GraphQLResponse<MediaEpisodesData>): MediaEpisodesRecord {
        val data = handleGraphQLResponse(body)
        val media = data.media
            ?: throw IllegalStateException("Empty response body")
        return media.toMediaEpisodesRecord()
    }

    private fun handleMediaRelationsRecord(body: GraphQLResponse<MediaRelationsData>): MediaRelationsRecord {
        val data = handleGraphQLResponse(body)
        val media = data.media
            ?: throw IllegalStateException("Empty response body")
        return media.toMediaRelationsRecord()
    }

    private fun handleMediaCharactersRecord(body: GraphQLResponse<MediaCharactersData>): MediaCharactersRecord {
        val data = handleGraphQLResponse(body)
        val media = data.media
            ?: throw IllegalStateException("Empty response body")
        return media.toMediaCharactersRecord()
    }

    private fun handleMediaStaffRecord(body: GraphQLResponse<MediaStaffData>): MediaStaffRecord {
        val data = handleGraphQLResponse(body)
        val media = data.media
            ?: throw IllegalStateException("Empty response body")
        return media.toMediaStaffRecord()
    }

    private fun handleMediaRecommendations(body: GraphQLResponse<RecommendationMediaData>): RecommendationPageResult {
        val data = handleGraphQLResponse(body)
        val recommendations = data.media?.recommendations
            ?: throw IllegalStateException("Empty response body")
        return RecommendationPageResult(
            recommendations = recommendations.nodes.orEmpty().mapNotNull { it?.toRecommendationRecord() },
            pageInfo = recommendations.pageInfo?.toPageInfoRecord(),
        )
    }

    // Legacy entity-typed surface for the media feed boundary.
    //
    // Sources from the same generated transport as getMediaSocialRecords below
    // and maps the generated data back to the legacy feed entities at the
    // repository boundary (see MediaMapper.kt). Remaining consumers
    // (MediaFeedViewModel). May commit to the FeedStore.
    suspend fun getMediaSocial(
        mediaId: Long,
        isFollowing: Boolean = true,
        page: Int? = null,
        perPage: Int? = null,
        commitToStore: Boolean = true,
        queryKey: FeedQueryKey? = null,
        readToken: Long = 0L,
    ): Result<PageContainer<FeedList>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaSocial.request(mediaId = mediaId.toInt(), isFollowing = isFollowing, page = page, perPage = perPage)
            val response = mediaService.getMediaSocialRecord(request)
            if (response.isSuccessful) {
                val result = handleMediaSocialPage(response.body() ?: throw IllegalStateException("Empty response body"))
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
                            token = readToken,
                            feeds = result.pageData.map { it.toFeedRecord(revision = readToken) },
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

    private fun handleMediaSocialPage(body: GraphQLResponse<MediaSocialData>): PageContainer<FeedList> {
        val data = handleGraphQLResponse(body)
        return data.toFeedPage()
    }

    // Record-typed surface for the media feed boundary (Lane C).
    //
    // Preserves the exact transport, request parameters, page-info, null
    // handling, revision-token, and failure semantics of the legacy
    // entity-typed getMediaSocial above, but maps at the data boundary into
    // FeedRecord and may commit to the FeedStore.
    suspend fun getMediaSocialRecords(
        mediaId: Long,
        isFollowing: Boolean = true,
        page: Int? = null,
        perPage: Int? = null,
        commitToStore: Boolean = true,
        queryKey: FeedQueryKey? = null,
        readToken: Long = 0L,
    ): Result<FeedRecordPage> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaSocial.request(mediaId = mediaId.toInt(), isFollowing = isFollowing, page = page, perPage = perPage)
            val response = mediaService.getMediaSocialRecord(request)
            if (response.isSuccessful) {
                val result = handleMediaSocialPage(response.body() ?: throw IllegalStateException("Empty response body"))
                val pageInfo = result.toRecordPageInfo()
                val feeds = result.toFeedRecords(revision = readToken)
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
                            token = readToken,
                            feeds = feeds,
                            pageInfo = pageInfo,
                        ),
                    )
                }

                FeedRecordPage(feeds = feeds, pageInfo = pageInfo)
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
