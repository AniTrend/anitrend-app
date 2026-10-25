package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.feed.model.FeedReplyRecord

/**
 * Immutable render projection for a feed reply. Compose rows consume only this snapshot,
 * including server-operation state and the current user's precomputed like state.
 */
data class CommentReplyUiModel(
    val id: Long,
    val reply: String?,
    val createdAt: Long,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val likes: List<UserSummaryRecord>,
    val likeCount: Int,
    val isLikedByCurrentUser: Boolean,
    val isLikePending: Boolean,
    val isDeletePending: Boolean,
)

/**
 * Canonical projection from the feed store reply record. The likes list is
 * defensively copied so the resulting model never aliases mutable state held
 * by the caller.
 */
fun FeedReplyRecord.toCommentReplyUiModel(
    isLikePending: Boolean,
    isDeletePending: Boolean,
    currentUserId: Long? = null,
): CommentReplyUiModel = CommentReplyUiModel(
    id = id,
    reply = reply,
    createdAt = createdAt,
    userId = user?.id,
    userName = user?.name,
    userAvatar = user?.avatar,
    likes = likes.toList(),
    likeCount = likes.size,
    isLikedByCurrentUser = currentUserId != null && likes.any { it.id == currentUserId },
    isLikePending = isLikePending,
    isDeletePending = isDeletePending,
)

/**
 * Reverse projection used only by the composer interaction path (edit and
 * mention). The active comment state never carries `FeedReplyRecord` reverse
 * mappings; this bridges the immutable UI projection back into a record so
 * [com.mxt.anitrend.base.custom.view.editor.ComposerWidget] can consume it.
 */
fun CommentReplyUiModel.toFeedReplyRecord(
    activityId: Long = 0L,
    revision: Long = 0L,
): FeedReplyRecord = FeedReplyRecord(
    id = id,
    activityId = activityId,
    reply = reply,
    createdAt = createdAt,
    user = userId?.let { userId ->
        UserSummaryRecord(
            id = userId,
            name = userName,
            avatar = userAvatar,
            siteUrl = null,
        )
    },
    likes = likes.toList(),
    revision = revision,
)
