package com.mxt.anitrend.domain.feed.model

import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord

data class FeedRecord(
    val id: Long,
    val type: String?,
    val status: String?,
    val text: String?,
    val createdAt: Long,
    val user: UserSummaryRecord?,
    val messenger: UserSummaryRecord?,
    val recipient: UserSummaryRecord?,
    val media: MediaSummaryRecord?,
    val hasLikes: Boolean = true,
    val likes: List<UserSummaryRecord>,
    val replyCount: Int,
    val siteUrl: String?,
    val revision: Long,
)
