package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.PageInfoRecord

/**
 * Committed snapshot of one review query.
 *
 * Server-loaded pages own their review IDs in [pageReviewIds], keyed by page
 * number. [prependedReviewIds] and [appendedReviewIds] carry locally inserted
 * reviews whose server page is unknown but whose head/tail placement is
 * provable from the sort key. [orderedReviewIds] is the deterministic
 * projection of those three structures: prepended, then pages in ascending
 * order, then appended, deduplicated.
 *
 * [stale] marks a query whose global placement is no longer locally
 * authoritative after a mutation touched the active sort key; it is cleared by
 * a sufficiently new page-one refresh.
 *
 * [staleSinceRevision] is the mutation revision that invalidated the stale
 * state. A page load whose token is older than it was issued before the
 * invalidation, so it must never clear [stale] or rewrite server ordering and
 * membership with pre-invalidation data. It is null while the query is not
 * stale.
 */
data class ReviewQuerySnapshot(
    val pageReviewIds: Map<Int, List<Long>>,
    val prependedReviewIds: List<Long>,
    val appendedReviewIds: List<Long>,
    val pageInfo: PageInfoRecord?,
    val loadedPages: Set<Int>,
    val token: Long,
    val stale: Boolean,
    val staleSinceRevision: Long? = null,
    val lastUpdatedAtMillis: Long,
) {
    val orderedReviewIds: List<Long>
        get() = (
            prependedReviewIds +
                pageReviewIds.toSortedMap().values.flatten() +
                appendedReviewIds
            ).distinct()
}
