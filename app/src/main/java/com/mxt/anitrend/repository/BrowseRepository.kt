package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.data.mapper.toMediaListCollectionPageResult
import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.mapper.toPageInfoRecord
import com.mxt.anitrend.data.mapper.toReviewRecord
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStore
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.domain.medialist.model.MediaListCollectionPageResult
import com.mxt.anitrend.graphql.generated.DeleteMediaListEntry
import com.mxt.anitrend.graphql.generated.DeleteMediaListEntryData
import com.mxt.anitrend.graphql.generated.DeleteReview
import com.mxt.anitrend.graphql.generated.DeleteReviewData
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaBrowse
import com.mxt.anitrend.graphql.generated.MediaBrowseData
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaList
import com.mxt.anitrend.graphql.generated.MediaListBrowse
import com.mxt.anitrend.graphql.generated.MediaListBrowseData
import com.mxt.anitrend.graphql.generated.MediaListCollection
import com.mxt.anitrend.graphql.generated.MediaListCollectionData
import com.mxt.anitrend.graphql.generated.MediaListData
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.MediaWithList
import com.mxt.anitrend.graphql.generated.MediaWithListData
import com.mxt.anitrend.graphql.generated.RateReview
import com.mxt.anitrend.graphql.generated.RateReviewData
import com.mxt.anitrend.graphql.generated.ReviewBrowse
import com.mxt.anitrend.graphql.generated.ReviewBrowseData
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.graphql.generated.SaveMediaListEntry
import com.mxt.anitrend.graphql.generated.SaveMediaListEntryData
import com.mxt.anitrend.graphql.generated.SaveReview
import com.mxt.anitrend.graphql.generated.SaveReviewData
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.entity.anilist.MediaList as MediaEntityList
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.mapper.toDeleteState
import com.mxt.anitrend.repository.mapper.toMediaBaseEntity
import com.mxt.anitrend.repository.mapper.toMediaBrowsePage
import com.mxt.anitrend.repository.mapper.toMediaListBrowsePage
import com.mxt.anitrend.repository.mapper.toMediaListEntity
import com.mxt.anitrend.repository.mapper.toReview
import com.mxt.anitrend.repository.mapper.toReviewBrowsePage
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrowseRepository(
    private val browseService: BrowseService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mediaListStore: MediaListStore? = null,
    private val reviewStore: ReviewStore? = null,
) : AbstractRepository(ioDispatcher) {

    suspend fun getMediaListCollection(
        userId: Long? = null,
        userName: String? = null,
        type: MediaType? = null,
        forceSingleCompletedList: Boolean? = null,
        sort: List<MediaListSort>? = null,
        statusIn: List<MediaListStatus>? = null,
        scoreFormat: ScoreFormat = ScoreFormat.POINT_100,
        commitToStore: Boolean = true,
        queryKey: MediaListQueryKey? = null,
        readToken: Long = 0L,
    ): Result<MediaListCollectionPageResult> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaListCollection.request(
                userId = userId?.toInt(),
                userName = userName,
                type = type,
                forceSingleCompletedList = forceSingleCompletedList,
                sort = sort,
                statusIn = statusIn,
                scoreFormat = scoreFormat,
            )
            val response = browseService.getMediaListCollection(request)
            if (response.isSuccessful) {
                val result = handleMediaListCollection(response.body() ?: throw IllegalStateException("Empty response body"))
                val resolvedQueryKey = queryKey ?: MediaListQueryKey(
                    userId = userId,
                    userName = userName,
                    mediaType = type,
                    statuses = statusIn.orEmpty().toSet(),
                    sort = sort?.firstOrNull(),
                )

                if (commitToStore && queryKey != null && mediaListStore != null) {
                    mediaListStore.apply(
                        MediaListStoreChange.CollectionLoaded(
                            queryKey = resolvedQueryKey,
                            token = readToken,
                            entries = result.entries.map { entry ->
                                entry.copy(
                                    revision = readToken,
                                    ownerUserId = resolvedQueryKey.userId,
                                    ownerUserName = resolvedQueryKey.userName,
                                )
                            },
                            pageInfo = result.pageInfo,
                        ),
                    )
                }

                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMediaListCollection(body: GraphQLResponse<MediaListCollectionData>): MediaListCollectionPageResult {
        val data = handleGraphQLResponse(body)
        return data.mediaListCollection?.toMediaListCollectionPageResult()
            ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getMediaBrowse(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        seasonYear: Int? = null,
        type: MediaType? = null,
        format: MediaFormat? = null,
        startDateLike: String? = null,
        endDateLike: String? = null,
        season: MediaSeason? = null,
        genres: List<String>? = null,
        genresExclude: List<String>? = null,
        isAdult: Boolean? = null,
        sort: List<MediaSort>? = null,
        onList: Boolean? = null,
        status: MediaStatus? = null,
        tags: List<String>? = null,
        tagsExclude: List<String>? = null,
    ): Result<PageContainer<MediaBase>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaBrowse.request(
                id = id, page = page, perPage = perPage,
                seasonYear = seasonYear, type = type, format = format,
                startDateLike = startDateLike, endDateLike = endDateLike,
                season = season, genres = genres, genresExclude = genresExclude,
                isAdult = isAdult, sort = sort, onList = onList,
                status = status, tags = tags, tagsExclude = tagsExclude,
            )
            val response = browseService.getMediaBrowse(request)
            if (response.isSuccessful) {
                handleMediaBrowse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getReviewBrowse(
        page: Int? = null,
        perPage: Int? = null,
        mediaId: Long? = null,
        type: MediaType? = null,
        sort: List<ReviewSort>? = null,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        queryKey: ReviewQueryKey? = null,
        readToken: Long = 0L,
    ): Result<PageContainer<Review>> = withContext(ioDispatcher) {
        runCatching {
            val request = ReviewBrowse.request(page = page, perPage = perPage, mediaId = mediaId?.toInt(), type = type, sort = sort, asHtml = asHtml)
            val response = browseService.getReviewBrowse(request)
            if (response.isSuccessful) {
                val result = handleReviewBrowse(response.body() ?: throw IllegalStateException("Empty response body"))
                val pageInfo = result.takeIf { it.hasPageInfo() }?.pageInfo?.toPageInfoRecord()
                val resolvedQueryKey = queryKey ?: ReviewQueryKey(
                    mediaId = mediaId,
                    mediaType = type,
                    sort = sort?.firstOrNull(),
                )

                if (commitToStore && queryKey != null && reviewStore != null) {
                    reviewStore.apply(
                        ReviewStoreChange.PageLoaded(
                            queryKey = resolvedQueryKey,
                            page = pageInfo?.currentPage ?: page ?: 1,
                            token = readToken,
                            reviews = result.pageData.map { it.toReviewRecord(revision = readToken) },
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

    suspend fun getMediaListBrowse(
        id: Int? = null,
        userId: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
        type: MediaType? = null,
        status: MediaListStatus? = null,
        sort: List<MediaListSort>? = null,
        scoreFormat: ScoreFormat = ScoreFormat.POINT_100,
    ): Result<PageContainer<MediaEntityList>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaListBrowse.request(id = id, userId = userId?.toInt(), userName = userName, page = page, perPage = perPage, type = type, status = status, sort = sort, scoreFormat = scoreFormat)
            val response = browseService.getMediaListBrowse(request)
            if (response.isSuccessful) {
                handleMediaListBrowse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaList(
        id: Int? = null,
        mediaId: Long? = null,
        userName: String? = null,
        type: MediaType? = null,
        status: MediaListStatus? = null,
        sort: List<MediaListSort>? = null,
        scoreFormat: ScoreFormat = ScoreFormat.POINT_100,
    ): Result<MediaEntityList> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaList.request(id = id, mediaId = mediaId?.toInt(), userName = userName, type = type, status = status, sort = sort, scoreFormat = scoreFormat)
            val response = browseService.getMediaList(request)
            if (response.isSuccessful) {
                handleMediaList(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMediaWithList(
        id: Long,
        type: MediaType? = null,
        onList: Boolean? = null,
        scoreFormat: ScoreFormat = ScoreFormat.POINT_100,
    ): Result<MediaBase> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaWithList.request(id = id.toInt(), type = type, onList = onList, scoreFormat = scoreFormat)
            val response = browseService.getMediaWithList(request)
            if (response.isSuccessful) {
                handleMediaWithList(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Mutation operations

    suspend fun deleteMediaListEntry(
        id: Long,
        mediaId: Long? = null,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<DeleteState> = withContext(ioDispatcher) {
        runCatching {
            val request = DeleteMediaListEntry.request(id = id.toInt())
            val response = browseService.deleteMediaListEntry(request)
            if (response.isSuccessful) {
                val result = handleDeleteMediaListEntry(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore && result.isDeleted) {
                    val resolvedMediaId = mediaId ?: mediaListStore?.state?.value?.entriesById?.get(id)?.mediaId
                    mediaListStore?.apply(
                        MediaListStoreChange.EntryDeleted(
                            entryId = id,
                            mediaId = resolvedMediaId,
                            revision = revision,
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun deleteReview(
        id: Long,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<DeleteState> = withContext(ioDispatcher) {
        runCatching {
            val request = DeleteReview.request(id = id.toInt())
            val response = browseService.deleteReview(request)
            if (response.isSuccessful) {
                val result = handleDeleteReview(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore && result.isDeleted) {
                    reviewStore?.apply(
                        ReviewStoreChange.ReviewDeleted(
                            reviewId = id,
                            revision = revision,
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun saveMediaListEntry(
        id: Int? = null,
        mediaId: Long? = null,
        status: MediaListStatus? = null,
        scoreRaw: Int? = null,
        score: Double? = null,
        progress: Int? = null,
        progressVolumes: Int? = null,
        repeat: Int? = null,
        priority: Int? = null,
        private: Boolean = false,
        hiddenFromStatusLists: Boolean = false,
        customLists: List<String?>? = null,
        advancedScores: List<Double?>? = null,
        notes: String? = null,
        scoreFormat: ScoreFormat = ScoreFormat.POINT_100,
        startedAt: FuzzyDateInput? = null,
        completedAt: FuzzyDateInput? = null,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<MediaEntityList> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveMediaListEntry.request(
                id = id, mediaId = mediaId?.toInt(), status = status,
                scoreRaw = scoreRaw, score = score, progress = progress,
                progressVolumes = progressVolumes, repeat = repeat,
                priority = priority, privateValue = private,
                hiddenFromStatusLists = hiddenFromStatusLists,
                customLists = customLists, advancedScores = advancedScores,
                notes = notes, scoreFormat = scoreFormat,
                startedAt = startedAt, completedAt = completedAt,
            )
            val response = browseService.saveMediaListEntry(request)
            if (response.isSuccessful) {
                val result = handleSaveMediaListEntry(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore) {
                    mediaListStore?.apply(
                        MediaListStoreChange.EntryUpserted(
                            entry = result.toMediaListRecord(revision = revision),
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun rateReview(
        id: Long,
        rating: ReviewRating?,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<Review> = withContext(ioDispatcher) {
        runCatching {
            val request = RateReview.request(id = id.toInt(), rating = rating, asHtml = asHtml)
            val response = browseService.rateReview(request)
            if (response.isSuccessful) {
                val result = handleRateReview(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore) {
                    reviewStore?.apply(
                        ReviewStoreChange.ReviewRated(
                            review = result.toReviewRecord(revision = revision),
                            revision = revision,
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun saveReview(
        id: Int? = null,
        mediaId: Long,
        body: String?,
        summary: String? = null,
        score: Int? = null,
        private: Boolean? = null,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<Review> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveReview.request(id = id, mediaId = mediaId.toInt(), body = body, summary = summary, score = score, privateValue = private, asHtml = asHtml)
            val response = browseService.saveReview(request)
            if (response.isSuccessful) {
                val result = handleSaveReview(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore) {
                    reviewStore?.apply(
                        ReviewStoreChange.ReviewSaved(
                            review = result.toReviewRecord(revision = revision),
                            revision = revision,
                            isCreate = id == null,
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Response handlers: unwrap the generated GraphQL response envelope at the
    // repository boundary and delegate the generated-to-legacy mapping to
    // BrowseMapper. GraphQL errors and absent data throw through the shared
    // handleGraphQLResponse, and null generated roots (page/media/mutation
    // result) throw "Empty response body", keeping the exact semantics of the
    // legacy AniListContainer decoding. Store commits only ever see mapped
    // legacy entities after a successful response.

    private fun handleMediaBrowse(body: GraphQLResponse<MediaBrowseData>): PageContainer<MediaBase> {
        val data = handleGraphQLResponse(body)
        return data.toMediaBrowsePage()
    }

    private fun handleReviewBrowse(body: GraphQLResponse<ReviewBrowseData>): PageContainer<Review> {
        val data = handleGraphQLResponse(body)
        return data.toReviewBrowsePage()
    }

    private fun handleMediaListBrowse(body: GraphQLResponse<MediaListBrowseData>): PageContainer<MediaEntityList> {
        val data = handleGraphQLResponse(body)
        return data.toMediaListBrowsePage()
    }

    private fun handleMediaList(body: GraphQLResponse<MediaListData>): MediaEntityList {
        val data = handleGraphQLResponse(body)
        return data.toMediaListEntity()
    }

    private fun handleMediaWithList(body: GraphQLResponse<MediaWithListData>): MediaBase {
        val data = handleGraphQLResponse(body)
        return data.toMediaBaseEntity()
    }

    private fun handleDeleteMediaListEntry(body: GraphQLResponse<DeleteMediaListEntryData>): DeleteState {
        val data = handleGraphQLResponse(body)
        return data.toDeleteState()
    }

    private fun handleDeleteReview(body: GraphQLResponse<DeleteReviewData>): DeleteState {
        val data = handleGraphQLResponse(body)
        return data.toDeleteState()
    }

    private fun handleSaveMediaListEntry(body: GraphQLResponse<SaveMediaListEntryData>): MediaEntityList {
        val data = handleGraphQLResponse(body)
        return data.toMediaListEntity()
    }

    private fun handleRateReview(body: GraphQLResponse<RateReviewData>): Review {
        val data = handleGraphQLResponse(body)
        return data.toReview()
    }

    private fun handleSaveReview(body: GraphQLResponse<SaveReviewData>): Review {
        val data = handleGraphQLResponse(body)
        return data.toReview()
    }
}
