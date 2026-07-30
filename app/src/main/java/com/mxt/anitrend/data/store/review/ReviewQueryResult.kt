package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.model.entity.anilist.Review

data class ReviewQueryResult(
    val reviews: List<Review>,
    val pageInfo: PageInfoRecord?,
    val loadedPages: Set<Int>,
)
