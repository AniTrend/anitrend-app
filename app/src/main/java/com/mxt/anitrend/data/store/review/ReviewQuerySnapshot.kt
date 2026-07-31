package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.PageInfoRecord

data class ReviewQuerySnapshot(
    val orderedReviewIds: List<Long>,
    val pageInfo: PageInfoRecord?,
    val loadedPages: Set<Int>,
    val token: Long,
    val lastUpdatedAtMillis: Long,
)
