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
        val reviewsById = currentState.reviewsById.toMutableMap().apply {
            put(review.id, ReviewStoreRecord(review = review, revision = revision))
        }
        val queries = currentState.queries.mapValuesTo(linkedMapOf()) { (queryKey, snapshot) ->
            reduceUpsertedQuery(review, snapshot, kind, queryKey, isCreate, revision)
        }

        return currentState.copy(
            reviewsById = reviewsById,
            queries = queries,
        )
    }

    /**
     * A mutation never reorders a query: server-provided positions of existing
     * reviews are preserved. When the mutation's changed field is the query's
     * active sort key, the query is marked [ReviewQuerySnapshot.stale] instead
     * of being reordered locally, and [ReviewQuerySnapshot.staleSinceRevision]
     * records the mutation revision so pre-invalidation page loads can never
     * clear the stale state. A review already listed by the query is treated
     * as server-proven membership.
     *
     * Only a newly created absent review may be inserted, and only under the
     * ID sorts where placement is provable from canonical review IDs:
     * prepended for [ReviewSort.ID_DESC] and appended for [ReviewSort.ID],
     * with each bucket kept sorted by ID so mutation response arrival order
     * never changes the rendered order. CREATED_AT ordering depends on server
     * tie-breaking that cannot be proven locally, so creates under
     * [ReviewSort.CREATED_AT]/[ReviewSort.CREATED_AT_DESC] conservatively mark
     * the query stale instead of inserting. An update of an absent review is
     * never inserted at a boundary: neither membership nor position can be
     * proven locally, so the query is marked stale and page-one refresh is
     * required.
     */
    private fun reduceUpsertedQuery(
        review: ReviewRecord,
        snapshot: ReviewQuerySnapshot,
        kind: UpsertKind,
        queryKey: ReviewQueryKey,
        isCreate: Boolean,
        revision: Long,
    ): ReviewQuerySnapshot {
        if (snapshot.orderedReviewIds.contains(review.id)) {
            return if (kind.affectsSortKey(queryKey.sort)) {
                snapshot.copy(
                    stale = true,
                    staleSinceRevision = maxOf(snapshot.staleSinceRevision ?: Long.MIN_VALUE, revision),
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                )
            } else {
                snapshot
            }
        }

        if (!review.matches(queryKey)) {
            return snapshot
        }

        return when (kind) {
            UpsertKind.RATED -> snapshot
            UpsertKind.SAVED -> when {
                // An update cannot prove membership or position for a review
                // absent from the query: never insert at a boundary.
                !isCreate -> snapshot.copy(
                    stale = true,
                    staleSinceRevision = maxOf(snapshot.staleSinceRevision ?: Long.MIN_VALUE, revision),
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                )
                else -> when (queryKey.sort.insertPlacement()) {
                    InsertPlacement.PREPEND -> snapshot.copy(
                        prependedReviewIds = (snapshot.prependedReviewIds + review.id).sortedDescending(),
                        lastUpdatedAtMillis = System.currentTimeMillis(),
                    )
                    InsertPlacement.APPEND -> snapshot.copy(
                        appendedReviewIds = (snapshot.appendedReviewIds + review.id).sorted(),
                        lastUpdatedAtMillis = System.currentTimeMillis(),
                    )
                    InsertPlacement.UNKNOWN -> snapshot.copy(
                        stale = true,
                        staleSinceRevision = maxOf(snapshot.staleSinceRevision ?: Long.MIN_VALUE, revision),
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

    /**
     * Placement of a newly created review under a sort key. Only ID-based
     * sorts are locally provable from canonical review IDs:
     * [ReviewSort.ID_DESC] prepends with the bucket kept sorted descending,
     * [ReviewSort.ID] appends with the bucket kept sorted ascending.
     * CREATED_AT ordering depends on server tie-breaking, so it cannot be
     * proven from store data and falls through to the conservative stale path.
     */
    private fun ReviewSort.insertPlacement(): InsertPlacement = when (this) {
        ReviewSort.ID_DESC -> InsertPlacement.PREPEND
        ReviewSort.ID -> InsertPlacement.APPEND
        ReviewSort.CREATED_AT, ReviewSort.CREATED_AT_DESC,
        ReviewSort.RATING, ReviewSort.RATING_DESC,
        ReviewSort.SCORE, ReviewSort.SCORE_DESC,
        ReviewSort.UPDATED_AT, ReviewSort.UPDATED_AT_DESC,
        -> InsertPlacement.UNKNOWN
    }
}
