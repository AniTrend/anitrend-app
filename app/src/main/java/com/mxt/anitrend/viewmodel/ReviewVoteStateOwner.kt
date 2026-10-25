package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class ReviewVotePendingState(
    val upVoteToken: Long? = null,
    val downVoteToken: Long? = null,
) {
    val upVoteLoading: Boolean get() = upVoteToken != null
    val downVoteLoading: Boolean get() = downVoteToken != null

    fun isLoading(target: ReviewRating): Boolean = when (target) {
        ReviewRating.UP_VOTE -> upVoteLoading
        ReviewRating.DOWN_VOTE -> downVoteLoading
        else -> false
    }

    fun tokenFor(target: ReviewRating): Long? = when (target) {
        ReviewRating.UP_VOTE -> upVoteToken
        ReviewRating.DOWN_VOTE -> downVoteToken
        else -> null
    }

    fun withToken(target: ReviewRating, token: Long): ReviewVotePendingState = when (target) {
        ReviewRating.UP_VOTE -> copy(upVoteToken = token)
        ReviewRating.DOWN_VOTE -> copy(downVoteToken = token)
        else -> this
    }

    fun withoutToken(target: ReviewRating): ReviewVotePendingState = when (target) {
        ReviewRating.UP_VOTE -> copy(upVoteToken = null)
        ReviewRating.DOWN_VOTE -> copy(downVoteToken = null)
        else -> this
    }
}

internal data class ReviewVoteTicket(
    val reviewId: Long,
    val target: ReviewRating,
    val rating: ReviewRating,
    val token: Long,
)

internal enum class ReviewVoteActionResult {
    LOGIN_REQUIRED,
    BUSY,
    SUBMITTED,
}

internal class ReviewVoteStateOwner(
    private val scope: CoroutineScope,
) {
    private val lock = Any()
    private val mutableState = MutableStateFlow<Map<Long, ReviewVotePendingState>>(emptyMap())
    private val timeoutJobs = mutableMapOf<ReviewVoteTicket, Job>()
    private var nextToken = 0L

    val state: StateFlow<Map<Long, ReviewVotePendingState>> = mutableState.asStateFlow()

    fun beginVote(review: ReviewRecord, target: ReviewRating): ReviewVoteTicket? = synchronized(lock) {
        if (target != ReviewRating.UP_VOTE && target != ReviewRating.DOWN_VOTE) {
            return@synchronized null
        }
        val current = mutableState.value[review.id] ?: ReviewVotePendingState()
        if (current.isLoading(target)) {
            return@synchronized null
        }

        val token = ++nextToken
        val ticket = ReviewVoteTicket(
            reviewId = review.id,
            target = target,
            rating = if (review.userRating == target.name) ReviewRating.NO_VOTE else target,
            token = token,
        )
        mutableState.value = mutableState.value + (review.id to current.withToken(target, token))
        timeoutJobs[ticket] = scope.launch {
            delay(LOADING_FALLBACK_TIMEOUT_MS)
            clear(ticket, cancelTimeout = false)
        }
        ticket
    }

    fun complete(ticket: ReviewVoteTicket) {
        clear(ticket, cancelTimeout = true)
    }

    private fun clear(ticket: ReviewVoteTicket, cancelTimeout: Boolean) {
        synchronized(lock) {
            val current = mutableState.value[ticket.reviewId]
            if (current?.tokenFor(ticket.target) == ticket.token) {
                val updated = current.withoutToken(ticket.target)
                mutableState.value = if (updated.upVoteLoading || updated.downVoteLoading) {
                    mutableState.value + (ticket.reviewId to updated)
                } else {
                    mutableState.value - ticket.reviewId
                }
            }
            val job = timeoutJobs.remove(ticket)
            if (cancelTimeout) job?.cancel()
        }
    }

    private companion object {
        const val LOADING_FALLBACK_TIMEOUT_MS = 10_000L
    }
}
