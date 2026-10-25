package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.feed.model.FeedRecord
import java.util.Locale

/**
 * Immutable rendering snapshot for a feed row. Compose lists consume this projection,
 * including ownership and server-operation state, without reading mutable network entities.
 */
data class FeedItemUiModel(
    val id: Long,
    val type: String?,
    val headline: CharSequence,
    val body: CharSequence?,
    val likeCount: Int,
    val isLikedByCurrentUser: Boolean,
    val replyCount: Int,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val isLikePending: Boolean,
    val isDeletePending: Boolean,
    val createdAt: Long,
    val userAvatarUrl: String?,
    val userName: String?,
    val userId: Long?,
    val messengerAvatarUrl: String?,
    val messengerName: String?,
    val messengerId: Long?,
    val recipientAvatarUrl: String?,
    val recipientName: String?,
    val recipientId: Long?,
    val mediaId: Long?,
    val mediaType: String?,
    val mediaTitleEnglish: String?,
    val mediaTitleOriginal: String?,
    val mediaCoverImageUrl: String?,
    val hasLikes: Boolean,
    val likes: List<UserSummaryRecord>,
    val feedText: String?,
)

/**
 * Canonical projection from the feed store record. All store-driven feed
 * screens observe committed `FeedRecord` state and render through this path.
 * The likes list is defensively copied so the resulting model never aliases
 * mutable state held by the caller.
 */
fun FeedRecord.toFeedItemUiModel(
    isLikePending: Boolean,
    isDeletePending: Boolean,
    currentUserId: Long? = null,
): FeedItemUiModel {
    val messengerId = messenger?.id
    val canModify = when {
        currentUserId == null -> false
        messengerId != null -> messengerId == currentUserId
        else -> user?.id == currentUserId
    }

    return FeedItemUiModel(
        id = id,
        type = type,
        headline = buildHeadline(),
        body = text,
        likeCount = likes.size,
        isLikedByCurrentUser = currentUserId != null && likes.any { it.id == currentUserId },
        replyCount = replyCount,
        canEdit = canModify,
        canDelete = canModify,
        isLikePending = isLikePending,
        isDeletePending = isDeletePending,
        createdAt = createdAt,
        userAvatarUrl = user?.avatar,
        userName = user?.name,
        userId = user?.id,
        messengerAvatarUrl = messenger?.avatar,
        messengerName = messenger?.name,
        messengerId = messenger?.id,
        recipientAvatarUrl = recipient?.avatar,
        recipientName = recipient?.name,
        recipientId = recipient?.id,
        mediaId = media?.id,
        mediaType = media?.type,
        mediaTitleEnglish = media?.titleEnglish,
        mediaTitleOriginal = media?.titleOriginal,
        mediaCoverImageUrl = media?.coverImage,
        hasLikes = hasLikes,
        likes = likes.toList(),
        feedText = text,
    )
}

private fun FeedRecord.buildHeadline(): CharSequence {
    val title = media?.titleRomaji.orEmpty()
    return when {
        title.isBlank() -> status ?: text.orEmpty()
        text.isNullOrEmpty() -> String.format(Locale.getDefault(), "%s: %s", status, title)
        else -> String.format(Locale.getDefault(), "%s %s of: %s", status, text, title)
    }
}
