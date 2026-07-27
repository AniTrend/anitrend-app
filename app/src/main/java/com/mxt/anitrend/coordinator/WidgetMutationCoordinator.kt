package com.mxt.anitrend.coordinator

import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetMutationCoordinator(
    private val baseRepository: BaseRepository,
    private val browseRepository: BrowseRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
    val databaseHelper: DatabaseHelper,
) {

    fun toggleLike(
        id: Long,
        type: LikeableType,
        onResult: (Result<List<UserBase>>) -> Unit,
    ) {
        coroutineScope.launch(ioDispatcher) {
            val result = baseRepository.toggleLike(id, type)
            withContext(mainDispatcher) {
                onResult(result)
            }
        }
    }

    fun rateReview(
        id: Long,
        rating: ReviewRating?,
        onResult: (Result<Review>) -> Unit,
    ) {
        coroutineScope.launch(ioDispatcher) {
            val result = browseRepository.rateReview(id, rating)
            withContext(mainDispatcher) {
                onResult(result)
            }
        }
    }

    fun toggleFollow(
        userId: Long,
        onResult: (Result<UserBase>) -> Unit,
    ) {
        coroutineScope.launch(ioDispatcher) {
            val result = userRepository.toggleFollow(userId)
            withContext(mainDispatcher) {
                onResult(result)
            }
        }
    }

    fun deleteActivity(
        id: Long,
        onResult: (Result<DeleteState>) -> Unit,
    ) {
        coroutineScope.launch(ioDispatcher) {
            val result = feedRepository.deleteActivity(id)
            withContext(mainDispatcher) {
                onResult(result)
            }
        }
    }

    fun deleteActivityReply(
        id: Long,
        onResult: (Result<DeleteState>) -> Unit,
    ) {
        coroutineScope.launch(ioDispatcher) {
            val result = feedRepository.deleteActivityReply(id)
            withContext(mainDispatcher) {
                onResult(result)
            }
        }
    }

    fun deleteMediaListEntry(
        id: Long,
        onResult: (Result<DeleteState>) -> Unit,
    ) {
        coroutineScope.launch(ioDispatcher) {
            val result = browseRepository.deleteMediaListEntry(id)
            withContext(mainDispatcher) {
                onResult(result)
            }
        }
    }

    @Suppress("LongParameterList")
    fun saveMediaListEntry(
        id: Int?,
        mediaId: Long?,
        status: MediaListStatus?,
        score: Double?,
        progress: Int?,
        progressVolumes: Int?,
        repeat: Int?,
        priority: Int?,
        private: Boolean,
        hiddenFromStatusLists: Boolean,
        customLists: List<String?>?,
        advancedScores: List<Double?>?,
        notes: String?,
        startedAt: FuzzyDateInput?,
        completedAt: FuzzyDateInput?,
        onResult: (Result<MediaList>) -> Unit,
    ) {
        coroutineScope.launch(ioDispatcher) {
            val scoreFormat = runCatching {
                databaseHelper.currentUser?.mediaListOptions?.scoreFormat?.let {
                    ScoreFormat.valueOf(it)
                }
            }.getOrNull() ?: ScoreFormat.POINT_100

            val result = browseRepository.saveMediaListEntry(
                id = id,
                mediaId = mediaId,
                status = status,
                score = score,
                progress = progress,
                progressVolumes = progressVolumes,
                repeat = repeat,
                priority = priority,
                private = private,
                hiddenFromStatusLists = hiddenFromStatusLists,
                customLists = customLists,
                advancedScores = advancedScores,
                notes = notes,
                scoreFormat = scoreFormat,
                startedAt = startedAt,
                completedAt = completedAt,
            )
            withContext(mainDispatcher) {
                onResult(result)
            }
        }
    }
}
