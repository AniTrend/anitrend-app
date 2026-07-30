package com.mxt.anitrend.coordinator

import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.graphql.generated.ReviewRating
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

// TODO Phase 7: delete this coordinator after FeedAdapter, ReviewAdapter, and UserAdapter
// forward actions to screen-owned ViewModels instead of widget callbacks.
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
}
