package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.model.entity.anilist.Review

data class ReviewStoreRecord(
    val review: Review,
    val revision: Long,
)

data class ReviewStoreState(
    val reviewsById: Map<Long, ReviewStoreRecord> = emptyMap(),
    val queries: Map<ReviewQueryKey, ReviewQuerySnapshot> = emptyMap(),
)
