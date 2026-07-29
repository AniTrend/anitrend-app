package com.mxt.anitrend.domain.feed.model

import com.mxt.anitrend.domain.model.UserSummaryRecord

data class FeedReplyRecord(
    val id: Long,
    val activityId: Long,
    val reply: String?,
    val createdAt: Long,
    val user: UserSummaryRecord?,
    val likes: List<UserSummaryRecord>,
    val revision: Long,
)
