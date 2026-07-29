package com.mxt.anitrend.data.store.feed

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord

sealed interface FeedStoreChange {
    data class PageLoaded(
        val queryKey: FeedQueryKey,
        val page: Int,
        val feeds: List<FeedRecord>,
        val pageInfo: PageInfoRecord?,
    ) : FeedStoreChange

    data class FeedUpserted(
        val feed: FeedRecord,
    ) : FeedStoreChange

    data class FeedDeleted(
        val feedId: Long,
        val revision: Long,
    ) : FeedStoreChange

    data class ReplyUpserted(
        val feedId: Long,
        val reply: FeedReplyRecord,
    ) : FeedStoreChange

    data class ReplyDeleted(
        val feedId: Long,
        val replyId: Long,
        val revision: Long,
    ) : FeedStoreChange

    data class FeedLikesReplaced(
        val feedId: Long,
        val likes: List<UserSummaryRecord>,
        val revision: Long,
    ) : FeedStoreChange

    data class ReplyLikesReplaced(
        val feedId: Long,
        val replyId: Long,
        val likes: List<UserSummaryRecord>,
        val revision: Long,
    ) : FeedStoreChange
}
