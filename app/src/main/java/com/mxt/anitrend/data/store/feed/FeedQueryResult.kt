package com.mxt.anitrend.data.store.feed

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.model.PageInfoRecord

data class FeedQueryResult(
    val feeds: List<FeedRecord>,
    val pageInfo: PageInfoRecord?,
)
