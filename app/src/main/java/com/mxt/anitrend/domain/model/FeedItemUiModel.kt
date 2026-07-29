package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import java.util.Locale

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
)

fun FeedList.toFeedItemUiModel(currentUserId: Long? = null): FeedItemUiModel {
    val likes = likes.orEmpty()
    val canModify = when {
        currentUserId == null -> false
        messenger?.id != null -> messenger?.id == currentUserId
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
        isLikePending = false,
        isDeletePending = false,
    )
}

fun FeedRecord.toFeedItemUiModel(
    isLikePending: Boolean,
    isDeletePending: Boolean,
): FeedItemUiModel = FeedItemUiModel(
    id = id,
    type = type,
    headline = buildHeadline(),
    body = text,
    likeCount = likes.size,
    isLikedByCurrentUser = false,
    replyCount = replyCount,
    canEdit = false,
    canDelete = false,
    isLikePending = isLikePending,
    isDeletePending = isDeletePending,
)

private fun FeedList.buildHeadline(): CharSequence {
    val title = media?.title?.romaji.orEmpty()
    return when {
        title.isBlank() -> status ?: text.orEmpty()
        text.isNullOrEmpty() -> String.format(Locale.getDefault(), "%s: %s", status, title)
        else -> String.format(Locale.getDefault(), "%s %s of: %s", status, text, title)
    }
}

private fun FeedRecord.buildHeadline(): CharSequence {
    val title = media?.titleRomaji.orEmpty()
    return when {
        title.isBlank() -> status ?: text.orEmpty()
        text.isNullOrEmpty() -> String.format(Locale.getDefault(), "%s: %s", status, title)
        else -> String.format(Locale.getDefault(), "%s %s of: %s", status, text, title)
    }
}
