package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.ReviewRecord

data class ReviewStoreRecord(
    val review: ReviewRecord,
    val revision: Long,
)

data class ReviewStoreState(
    val reviewsById: Map<Long, ReviewStoreRecord> = emptyMap(),
    val queries: Map<ReviewQueryKey, ReviewQuerySnapshot> = emptyMap(),
)
