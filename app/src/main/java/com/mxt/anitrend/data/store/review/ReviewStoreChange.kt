package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.model.entity.anilist.Review

sealed interface ReviewStoreChange {
    data class PageLoaded(
        val queryKey: ReviewQueryKey,
        val page: Int,
        val generation: Int,
        val reviews: List<Review>,
        val pageInfo: PageInfoRecord?,
    ) : ReviewStoreChange

    data class ReviewSaved(
        val review: Review,
        val revision: Long,
    ) : ReviewStoreChange

    data class ReviewRated(
        val review: Review,
        val revision: Long,
    ) : ReviewStoreChange

    data class ReviewDeleted(
        val reviewId: Long,
        val revision: Long,
    ) : ReviewStoreChange
}
