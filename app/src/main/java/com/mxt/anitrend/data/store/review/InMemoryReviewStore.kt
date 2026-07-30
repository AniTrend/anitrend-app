package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.model.entity.anilist.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryReviewStore : ReviewStore {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(ReviewStoreState())
    private val reviewDeletionRevisions = mutableMapOf<Long, Long>()

    override val state: StateFlow<ReviewStoreState> = mutableState.asStateFlow()

    override suspend fun apply(change: ReviewStoreChange) {
        mutex.withLock {
            mutableState.value = when (change) {
                is ReviewStoreChange.PageLoaded -> reducePageLoaded(change)
                is ReviewStoreChange.ReviewSaved -> reduceReviewUpserted(change.review, change.revision, insertIfMissing = true)
                is ReviewStoreChange.ReviewRated -> reduceReviewUpserted(change.review, change.revision, insertIfMissing = false)
                is ReviewStoreChange.ReviewDeleted -> reduceReviewDeleted(change.reviewId, change.revision)
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            reviewDeletionRevisions.clear()
            mutableState.value = ReviewStoreState()
        }
    }

    override fun observeReview(reviewId: Long): Flow<Review?> = state.map { it.reviewsById[reviewId]?.review?.copyForStore() }.distinctUntilChanged()

    override fun observeQuery(key: ReviewQueryKey): Flow<ReviewQueryResult> = state.map { currentState ->
        val snapshot = currentState.queries[key]
        ReviewQueryResult(
            reviews = snapshot?.orderedReviewIds.orEmpty().mapNotNull { reviewId ->
                currentState.reviewsById[reviewId]?.review?.copyForStore()
            },
            pageInfo = snapshot?.pageInfo,
            loadedPages = snapshot?.loadedPages.orEmpty(),
        )
    }.distinctUntilChanged()

    private fun reducePageLoaded(change: ReviewStoreChange.PageLoaded): ReviewStoreState {
        val currentState = mutableState.value
        val existingSnapshot = currentState.queries[change.queryKey]
        if (existingSnapshot != null && change.generation < existingSnapshot.generation) {
            return currentState
        }

        val reviewsById = currentState.reviewsById.toMutableMap()
        val acceptedIds = mutableListOf<Long>()

        change.reviews.forEach { review ->
            val currentRevision = maxOf(
                reviewsById[review.id]?.revision ?: Long.MIN_VALUE,
                reviewDeletionRevisions[review.id] ?: Long.MIN_VALUE,
            )
            if (0L >= currentRevision) {
                reviewDeletionRevisions.remove(review.id)
                reviewsById[review.id] = ReviewStoreRecord(review = review.copyForStore(), revision = 0L)
            }
            if (reviewsById.containsKey(review.id)) {
                acceptedIds += review.id
            }
        }

        val orderedReviewIds =
            if (change.page <= 1) {
                acceptedIds.distinct()
            } else {
                (existingSnapshot?.orderedReviewIds.orEmpty() + acceptedIds).distinct()
            }
        val loadedPages =
            if (change.page <= 1) {
                setOf(change.page)
            } else {
                existingSnapshot?.loadedPages.orEmpty() + change.page
            }
        val queries = currentState.queries.toMutableMap().apply {
            put(
                change.queryKey,
                ReviewQuerySnapshot(
                    orderedReviewIds = orderedReviewIds,
                    pageInfo = change.pageInfo,
                    loadedPages = loadedPages,
                    generation = change.generation,
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                ),
            )
        }

        return currentState.copy(
            reviewsById = reviewsById,
            queries = queries,
        )
    }

    private fun reduceReviewUpserted(
        review: Review,
        revision: Long,
        insertIfMissing: Boolean,
    ): ReviewStoreState {
        val currentState = mutableState.value
        val currentRevision = maxOf(
            currentState.reviewsById[review.id]?.revision ?: Long.MIN_VALUE,
            reviewDeletionRevisions[review.id] ?: Long.MIN_VALUE,
        )
        if (revision < currentRevision) {
            return currentState
        }

        reviewDeletionRevisions.remove(review.id)
        val reviewsById = currentState.reviewsById.toMutableMap().apply {
            put(review.id, ReviewStoreRecord(review = review.copyForStore(), revision = revision))
        }
        val queries = currentState.queries.mapValuesTo(linkedMapOf()) { (queryKey, snapshot) ->
            val alreadyPresent = snapshot.orderedReviewIds.contains(review.id)
            if (!insertIfMissing || alreadyPresent || !review.matches(queryKey)) {
                snapshot
            } else {
                snapshot.copy(
                    orderedReviewIds = listOf(review.id) + snapshot.orderedReviewIds,
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                )
            }
        }

        return currentState.copy(
            reviewsById = reviewsById,
            queries = queries,
        )
    }

    private fun reduceReviewDeleted(
        reviewId: Long,
        revision: Long,
    ): ReviewStoreState {
        val currentState = mutableState.value
        val currentRevision = maxOf(
            currentState.reviewsById[reviewId]?.revision ?: Long.MIN_VALUE,
            reviewDeletionRevisions[reviewId] ?: Long.MIN_VALUE,
        )
        if (revision < currentRevision) {
            return currentState
        }

        reviewDeletionRevisions[reviewId] = revision
        val reviewsById = currentState.reviewsById.toMutableMap().apply {
            remove(reviewId)
        }
        val queries = currentState.queries.mapValues { (_, snapshot) ->
            snapshot.copy(
                orderedReviewIds = snapshot.orderedReviewIds.filterNot { it == reviewId },
                lastUpdatedAtMillis = System.currentTimeMillis(),
            )
        }

        return currentState.copy(
            reviewsById = reviewsById,
            queries = queries,
        )
    }

    private fun Review.matches(queryKey: ReviewQueryKey): Boolean {
        if (queryKey.mediaId != null && media.id != queryKey.mediaId) {
            return false
        }

        val reviewMediaType = media.type ?: mediaType
        if (queryKey.mediaType != null && reviewMediaType != queryKey.mediaType.name) {
            return false
        }

        return true
    }

    private fun Review.copyForStore(): Review = Review().also { copy ->
        copy.id = id
        copy.summary = summary
        copy.mediaType = mediaType
        copy.body = body
        copy.rating = rating
        copy.ratingAmount = ratingAmount
        copy.userRating = userRating
        copy.score = score
        copy.isPrivate = isPrivate
        copy.createdAt = createdAt
        copy.user = user
        copy.media = media
    }
}
