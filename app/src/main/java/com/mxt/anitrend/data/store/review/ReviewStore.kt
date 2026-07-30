package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.model.entity.anilist.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ReviewStore {
    val state: StateFlow<ReviewStoreState>

    suspend fun apply(change: ReviewStoreChange)

    fun observeReview(reviewId: Long): Flow<Review?>

    fun observeQuery(key: ReviewQueryKey): Flow<ReviewQueryResult>
}
