package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.DeleteMediaListEntry
import com.mxt.anitrend.graphql.generated.DeleteReview
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaBrowse
import com.mxt.anitrend.graphql.generated.MediaFormat
import com.mxt.anitrend.graphql.generated.MediaList
import com.mxt.anitrend.graphql.generated.MediaListBrowse
import com.mxt.anitrend.graphql.generated.MediaListCollection
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaSeason
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.MediaWithList
import com.mxt.anitrend.graphql.generated.RateReview
import com.mxt.anitrend.graphql.generated.ReviewBrowse
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.graphql.generated.SaveMediaListEntry
import com.mxt.anitrend.graphql.generated.SaveReview
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.UpdateMediaListEntries
import com.mxt.anitrend.model.api.retro.anilist.BrowseModel
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.anilist.MediaList as MediaEntityList
import com.mxt.anitrend.model.entity.anilist.MediaListCollection as MediaListCollectionEntity

sealed class BrowseMutation {
    data class MediaListSaved(val entry: MediaEntityList) : BrowseMutation()
    data class MediaListDeleted(val id: Long) : BrowseMutation()
    data class MediaListsUpdated(val entries: List<MediaEntityList>) : BrowseMutation()
    data class ReviewRated(val review: Review) : BrowseMutation()
    data class ReviewSaved(val review: Review) : BrowseMutation()
    data class ReviewDeleted(val id: Long) : BrowseMutation()
}

class BrowseRepository(
    private val browseService: BrowseModel,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository<BrowseMutation>(ioDispatcher) {

    suspend fun getMediaListCollection(
        userId: Long? = null,
        userName: String? = null,
        type: MediaType? = null,
        forceSingleCompletedList: Boolean? = null,
        sort: List<MediaListSort>? = null,
        statusIn: List<MediaListStatus>? = null,
        scoreFormat: ScoreFormat = ScoreFormat.POINT_100,
    ): Result<PageContainer<MediaListCollectionEntity>> = withContext(ioDispatcher) {
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
            val response = browseService.getMediaListCollection(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
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
        genres: List<String?>? = null,
        genresExclude: List<String?>? = null,
        isAdult: Boolean? = null,
        sort: List<MediaSort>? = null,
        onList: Boolean? = null,
        status: MediaStatus? = null,
        tags: List<String?>? = null,
        tagsExclude: List<String?>? = null,
    ): Result<PageContainer<com.mxt.anitrend.model.entity.base.MediaBase>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaBrowse.request(
                id = id, page = page, perPage = perPage,
                seasonYear = seasonYear, type = type, format = format,
                startDateLike = startDateLike, endDateLike = endDateLike,
                season = season, genres = genres, genresExclude = genresExclude,
                isAdult = isAdult, sort = sort, onList = onList,
                status = status, tags = tags, tagsExclude = tagsExclude,
            )
            val response = browseService.getMediaBrowse(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
    ): Result<PageContainer<Review>> = withContext(ioDispatcher) {
        runCatching {
            val request = ReviewBrowse.request(page = page, perPage = perPage, mediaId = mediaId?.toInt(), type = type, sort = sort, asHtml = asHtml)
            val response = browseService.getReviewBrowse(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
            val response = browseService.getMediaListBrowse(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
            val response = browseService.getMediaList(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
    ): Result<com.mxt.anitrend.model.entity.base.MediaBase> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaWithList.request(id = id.toInt(), type = type, onList = onList, scoreFormat = scoreFormat)
            val response = browseService.getMediaWithList(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Mutation operations

    suspend fun deleteMediaListEntry(id: Long): Result<DeleteState> = withContext(ioDispatcher) {
        runCatching {
            val request = DeleteMediaListEntry.request(id = id.toInt())
            val response = browseService.deleteMediaListEntry(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                _mutationEvents.emit(BrowseMutation.MediaListDeleted(id))
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun deleteReview(id: Long): Result<DeleteState> = withContext(ioDispatcher) {
        runCatching {
            val request = DeleteReview.request(id = id.toInt())
            val response = browseService.deleteReview(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                _mutationEvents.emit(BrowseMutation.ReviewDeleted(id))
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
            val response = browseService.saveMediaListEntry(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                _mutationEvents.emit(BrowseMutation.MediaListSaved(result))
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun updateMediaListEntries(
        ids: List<Int?>?,
        scoreFormat: ScoreFormat = ScoreFormat.POINT_100,
    ): Result<List<MediaEntityList>> = withContext(ioDispatcher) {
        runCatching {
            val request = UpdateMediaListEntries.request(ids = ids, scoreFormat = scoreFormat)
            val response = browseService.updateMediaListEntries(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                _mutationEvents.emit(BrowseMutation.MediaListsUpdated(result))
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
    ): Result<Review> = withContext(ioDispatcher) {
        runCatching {
            val request = RateReview.request(id = id.toInt(), rating = rating, asHtml = asHtml)
            val response = browseService.rateReview(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                _mutationEvents.emit(BrowseMutation.ReviewRated(result))
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
    ): Result<Review> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveReview.request(id = id, mediaId = mediaId.toInt(), body = body, summary = summary, score = score, privateValue = private, asHtml = asHtml)
            val response = browseService.saveReview(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                _mutationEvents.emit(BrowseMutation.ReviewSaved(result))
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
