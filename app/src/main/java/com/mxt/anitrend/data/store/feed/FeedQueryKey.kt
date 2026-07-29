package com.mxt.anitrend.data.store.feed

import com.mxt.anitrend.graphql.generated.ActivityType

enum class FeedScope {
    GLOBAL,
    USER,
    MEDIA,
    MESSAGE_INBOX,
    MESSAGE_OUTBOX,
}

data class FeedQueryKey(
    val scope: FeedScope,
    val userId: Long?,
    val mediaId: Long?,
    val activityType: ActivityType?,
    val isFollowing: Boolean?,
    val isMixed: Boolean?,
)
