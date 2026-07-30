package com.mxt.anitrend.data.store.feed

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FeedStore {
    val state: StateFlow<FeedStoreState>

    suspend fun apply(change: FeedStoreChange)

    suspend fun clear()

    fun observeFeed(feedId: Long): Flow<FeedRecord?>

    fun observeReplies(feedId: Long): Flow<List<FeedReplyRecord>>

    fun observeQuery(key: FeedQueryKey): Flow<FeedQueryResult>
}
