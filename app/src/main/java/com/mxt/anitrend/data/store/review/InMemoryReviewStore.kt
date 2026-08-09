package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewSort
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
                is ReviewStoreChange.ReviewSaved -> reduceReviewUpserted(change.review, change.revision, UpsertKind.SAVED, isCreate = change.isCreate)
                is ReviewStoreChange.ReviewRated -> reduceReviewUpserted(change.review, change.revision, UpsertKind.RATED)
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

    override fun observeReview(reviewId: Long): Flow<ReviewRecord?> = state.map { it.reviewsById[reviewId]?.review }.distinctUntilChanged()

    override fun observeQuery(key: ReviewQueryKey): Flow<ReviewQueryResult> = state.map { currentState ->
        val snapshot = currentState.queries[key]
        ReviewQueryResult(
            reviews = snapshot?.orderedReviewIds.orEmpty().mapNotNull { reviewId ->
                currentState.reviewsById[reviewId]?.review
            },
            pageInfo = snapshot?.pageInfo,
            loadedPages = snapshot?.loadedPages.orEmpty(),
            stale = snapshot?.stale ?: false,
        )
    }.distinctUntilChanged()

    private fun reducePageLoaded(change: ReviewStoreChange.PageLoaded): ReviewStoreState {
        val currentState = mutableState.value
        val existingSnapshot = currentState.queries[change.queryKey]
        if (existingSnapshot != null && change.token < existingSnapshot.token) {
            return currentState
        }
        // A page load older than the revision that marked the query stale was
        // issued before the invalidation: it must not clear the stale state or
        // overwrite server ordering and membership with pre-invalidation data.
        // Only a page-one load new enough to pass this guard may clear stale.
        val invalidationRevision = existingSnapshot?.staleSinceRevision
        if (invalidationRevision != null && change.token < invalidationRevision) {
            return currentState
        }

        val reviewsById = currentState.reviewsById.toMutableMap()
        val acceptedIds = mutableListOf<Long>()

        change.reviews.forEach { review ->
            val currentRevision = maxOf(
                reviewsById[review.id]?.revision ?: Long.MIN_VALUE,
                reviewDeletionRevisions[review.id] ?: Long.MIN_VALUE,
            )
            if (change.token >= currentRevision) {
                reviewDeletionRevisions.remove(review.id)
                reviewsById[review.id] = ReviewStoreRecord(review = review, revision = change.token)
            }
            if (reviewsById.containsKey(review.id)) {
                acceptedIds += review.id
            }
        }

        val pageReviewIds =
            if (change.page <= 1) {
                mapOf(change.page to acceptedIds.distinct())
            } else {
                existingSnapshot?.pageReviewIds.orEmpty() + (change.page to acceptedIds.distinct())
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
                    pageReviewIds = pageReviewIds,
                    prependedReviewIds = if (change.page <= 1) emptyList() else existingSnapshot?.prependedReviewIds.orEmpty(),
                    appendedReviewIds = if (change.page <= 1) emptyList() else existingSnapshot?.appendedReviewIds.orEmpty(),
                    pageInfo = change.pageInfo,
                    loadedPages = loadedPages,
                    token = change.token,
                    stale = if (change.page <= 1) false else existingSnapshot?.stale ?: false,
                    staleSinceRevision = if (change.page <= 1) null else existingSnapshot?.staleSinceRevision,
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
        review: ReviewRecord,
        revision: Long,
        kind: UpsertKind,
        isCreate: Boolean = false,
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
        val record = ReviewStoreRecord(review = review, revision = revision)
        val reviewsById = currentState.reviewsById.toMutableMap().apply {
            put(review.id, record)
        }
        val queries = currentState.queries.mapValuesTo(linkedMapOf()) { (queryKey, snapshot) ->
            reduceQueryForUpsertedReview(record, snapshot, kind, queryKey, isCreate)
        }

        return currentState.copy(
            reviewsById = reviewsById,
            queries = queries,
        )
    }

    private fun reduceQueryForUpsertedReview(
        record: ReviewStoreRecord,
        snapshot: ReviewQuerySnapshot,
        kind: UpsertKind,
        queryKey: ReviewQueryKey,
        isCreate: Boolean,
    ): ReviewQuerySnapshot {
        // A mutation never reorders a query: existing reviews keep their
        // server-proven positions. A review already listed by the query is
        // server-proven membership; only a mutation that touches the active
        // sort key disturbs it, so the query is marked stale and
        // staleSinceRevision keeps pre-invalidation page loads from clearing it.
        if (snapshot.orderedReviewIds.contains(record.review.id)) {
            return if (kind.affectsSortKey(queryKey.sort)) {
                snapshot.copy(
                    stale = true,
                    staleSinceRevision = maxOf(snapshot.staleSinceRevision ?: Long.MIN_VALUE, record.revision),
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                )
            } else {
                snapshot
            }
        }

        if (!record.review.matches(queryKey)) {
            return snapshot
        }

        return when (kind) {
            UpsertKind.RATED -> snapshot
            UpsertKind.SAVED -> when {
                // An update cannot prove membership or position for a review
                // absent from the query: never insert at a boundary.
                !isCreate -> snapshot.copy(
                    stale = true,
                    staleSinceRevision = maxOf(snapshot.staleSinceRevision ?: Long.MIN_VALUE, record.revision),
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                )
                // Only a newly created absent review may be inserted, and only
                // where the sort key proves placement (see createdReviewPlacement).
                else -> when (queryKey.sort.createdReviewPlacement()) {
                    InsertPlacement.PREPEND -> snapshot.copy(
                        prependedReviewIds = (snapshot.prependedReviewIds + record.review.id).sortedDescending(),
                        lastUpdatedAtMillis = System.currentTimeMillis(),
                    )
                    InsertPlacement.APPEND -> snapshot.copy(
                        appendedReviewIds = (snapshot.appendedReviewIds + record.review.id).sorted(),
                        lastUpdatedAtMillis = System.currentTimeMillis(),
                    )
                    InsertPlacement.UNKNOWN -> snapshot.copy(
                        stale = true,
                        staleSinceRevision = maxOf(snapshot.staleSinceRevision ?: Long.MIN_VALUE, record.revision),
                        lastUpdatedAtMillis = System.currentTimeMillis(),
                    )
                }
            }
        }
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
                pageReviewIds = snapshot.pageReviewIds.mapValues { (_, ids) -> ids.filterNot { it == reviewId } },
                prependedReviewIds = snapshot.prependedReviewIds.filterNot { it == reviewId },
                appendedReviewIds = snapshot.appendedReviewIds.filterNot { it == reviewId },
                lastUpdatedAtMillis = System.currentTimeMillis(),
            )
        }

        return currentState.copy(
            reviewsById = reviewsById,
            queries = queries,
        )
    }

    /**
     * Filter membership only: [ReviewQueryKey.mediaId] and
     * [ReviewQueryKey.mediaType]. Sort never decides membership.
     */
    private fun ReviewRecord.matches(queryKey: ReviewQueryKey): Boolean {
        if (queryKey.mediaId != null && media?.id != queryKey.mediaId) {
            return false
        }

        val reviewMediaType = media?.type ?: mediaType
        if (queryKey.mediaType != null && reviewMediaType != queryKey.mediaType.name) {
            return false
        }

        return true
    }

    private enum class UpsertKind(
        val affectsSortKey: (ReviewSort) -> Boolean,
    ) {
        /**
         * SaveReview changes score/body/summary/private and bumps the server
         * updatedAt, so SCORE and UPDATED_AT ordering can shift.
         */
        SAVED({ it == ReviewSort.SCORE || it == ReviewSort.SCORE_DESC || it == ReviewSort.UPDATED_AT || it == ReviewSort.UPDATED_AT_DESC }),

        /**
         * RateReview changes only rating state, so RATING ordering can shift.
         */
        RATED({ it == ReviewSort.RATING || it == ReviewSort.RATING_DESC }),
    }

    private enum class InsertPlacement {
        PREPEND,
        APPEND,
        UNKNOWN,
    }

    private fun ReviewSort.createdReviewPlacement(): InsertPlacement = when (this) {
        // Only ID-based sorts are locally provable from canonical review ids:
        // ID_DESC prepends (bucket kept sorted descending), ID appends (ascending).
        ReviewSort.ID_DESC -> InsertPlacement.PREPEND
        ReviewSort.ID -> InsertPlacement.APPEND
        // CREATED_AT ordering depends on server tie-breaking, so it cannot be
        // proven from store data and falls through to the conservative stale path.
        ReviewSort.CREATED_AT, ReviewSort.CREATED_AT_DESC,
        ReviewSort.RATING, ReviewSort.RATING_DESC,
        ReviewSort.SCORE, ReviewSort.SCORE_DESC,
        ReviewSort.UPDATED_AT, ReviewSort.UPDATED_AT_DESC,
        -> InsertPlacement.UNKNOWN
    }
}
