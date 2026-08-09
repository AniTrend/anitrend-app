package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.ReviewRecord

data class ReviewQueryResult(
    val reviews: List<ReviewRecord>,
    val pageInfo: PageInfoRecord?,
    val loadedPages: Set<Int>,
    val stale: Boolean,
)
