package com.mxt.anitrend.domain.model

import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase
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
        createdAt = createdAt,
        userAvatarUrl = user.avatarUrl(),
        userName = user?.name,
        userId = user?.id,
        messengerAvatarUrl = messenger.avatarUrl(),
        messengerName = messenger?.name,
        messengerId = messenger?.id,
        recipientAvatarUrl = recipient.avatarUrl(),
        recipientName = recipient?.name,
        recipientId = recipient?.id,
        mediaId = media?.id,
        mediaType = media?.type,
        mediaTitleEnglish = media?.title?.english,
        mediaTitleOriginal = media?.title?.original,
        mediaCoverImageUrl = media.coverImageUrl(),
        hasLikes = this.likes != null,
        likes = likes.map(UserBase::toUserSummaryRecord),
        feedText = text,
    )
}

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
        likes = likes,
        feedText = text,
    )
}

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

private fun UserBase?.avatarUrl(): String? = this?.avatar?.large ?: this?.avatar?.medium ?: this?.avatar?.extraLarge

private fun MediaBase?.coverImageUrl(): String? = this?.coverImage?.extraLarge ?: this?.coverImage?.large ?: this?.coverImage?.medium

private fun UserBase.toUserSummaryRecord(): UserSummaryRecord = UserSummaryRecord(
    id = id,
    name = name,
    avatar = avatarUrl(),
    siteUrl = null,
)
