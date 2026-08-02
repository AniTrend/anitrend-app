package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.ReviewRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ReviewStore {
    val state: StateFlow<ReviewStoreState>

    suspend fun apply(change: ReviewStoreChange)

    suspend fun clear()

    fun observeReview(reviewId: Long): Flow<ReviewRecord?>

    fun observeQuery(key: ReviewQueryKey): Flow<ReviewQueryResult>
}
