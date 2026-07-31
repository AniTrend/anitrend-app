package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.feed.model.FeedReplyRecord

data class CommentReplyUiModel(
    val id: Long,
    val reply: String?,
    val createdAt: Long,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val likes: List<UserSummaryRecord>,
    val likeCount: Int,
    val isLikePending: Boolean,
    val isDeletePending: Boolean,
)

fun FeedReplyRecord.toCommentReplyUiModel(
    isLikePending: Boolean,
    isDeletePending: Boolean,
): CommentReplyUiModel = CommentReplyUiModel(
    id = id,
    reply = reply,
    createdAt = createdAt,
    userId = user?.id,
    userName = user?.name,
    userAvatar = user?.avatar,
    likes = likes,
    likeCount = likes.size,
    isLikePending = isLikePending,
    isDeletePending = isDeletePending,
)
