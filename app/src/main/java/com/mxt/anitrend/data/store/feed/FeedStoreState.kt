package com.mxt.anitrend.data.store.feed

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord

data class FeedStoreState(
    val feedsById: Map<Long, FeedRecord> = emptyMap(),
    val repliesById: Map<Long, FeedReplyRecord> = emptyMap(),
    val replyIdsByFeedId: Map<Long, List<Long>> = emptyMap(),
    val queries: Map<FeedQueryKey, FeedQuerySnapshot> = emptyMap(),
)
