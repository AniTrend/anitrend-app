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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WidgetMutationCoordinator(
    private val baseRepository: BaseRepository,
    private val browseRepository: BrowseRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    val databaseHelper: DatabaseHelper,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun toggleLike(
        id: Long,
        type: LikeableType,
        onResult: (Result<List<UserBase>>) -> Unit,
    ) {
        scope.launch {
            onResult(baseRepository.toggleLike(id, type))
        }
    }

    fun rateReview(
        id: Long,
        rating: ReviewRating?,
        onResult: (Result<Review>) -> Unit,
    ) {
        scope.launch {
            onResult(browseRepository.rateReview(id, rating))
        }
    }

    fun toggleFollow(
        userId: Long,
        onResult: (Result<UserBase>) -> Unit,
    ) {
        scope.launch {
            onResult(userRepository.toggleFollow(userId))
        }
    }

    fun deleteActivity(
        id: Long,
        onResult: (Result<DeleteState>) -> Unit,
    ) {
        scope.launch {
            onResult(feedRepository.deleteActivity(id))
        }
    }

    fun deleteActivityReply(
        id: Long,
        onResult: (Result<DeleteState>) -> Unit,
    ) {
        scope.launch {
            onResult(feedRepository.deleteActivityReply(id))
        }
    }

    fun deleteMediaListEntry(
        id: Long,
        onResult: (Result<DeleteState>) -> Unit,
    ) {
        scope.launch {
            onResult(browseRepository.deleteMediaListEntry(id))
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
        val scoreFormat = runCatching {
            databaseHelper.currentUser?.mediaListOptions?.scoreFormat?.let {
                ScoreFormat.valueOf(it)
            }
        }.getOrNull() ?: ScoreFormat.POINT_100

        scope.launch {
            browseRepository.saveMediaListEntry(
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
            ).let { onResult(it) }
        }
    }
}
