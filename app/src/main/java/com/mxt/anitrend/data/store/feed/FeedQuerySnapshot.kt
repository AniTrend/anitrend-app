package com.mxt.anitrend.data.store.feed

import com.mxt.anitrend.domain.model.PageInfoRecord

data class FeedQuerySnapshot(
    val orderedFeedIds: List<Long>,
    val pageInfo: PageInfoRecord?,
    val loadedPages: Set<Int>,
    val generation: Int,
    val lastUpdatedAtMillis: Long,
)
