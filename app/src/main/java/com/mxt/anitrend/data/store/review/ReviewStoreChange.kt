package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.ReviewRecord

sealed interface ReviewStoreChange {
    data class PageLoaded(
        val queryKey: ReviewQueryKey,
        val page: Int,
        val token: Long,
        val reviews: List<ReviewRecord>,
        val pageInfo: PageInfoRecord?,
    ) : ReviewStoreChange

    data class ReviewSaved(
        val review: ReviewRecord,
        val revision: Long,
        /**
         * True when the save created a new review (input id was null), false
         * when it updated an existing review. Only a create may be inserted at
         * a locally provable boundary; an update never is.
         */
        val isCreate: Boolean,
    ) : ReviewStoreChange

    data class ReviewRated(
        val review: ReviewRecord,
        val revision: Long,
    ) : ReviewStoreChange

    data class ReviewDeleted(
        val reviewId: Long,
        val revision: Long,
    ) : ReviewStoreChange
}
