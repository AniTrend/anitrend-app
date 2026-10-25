package com.mxt.anitrend.ui.feed

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendCondensedFontFamily
import com.mxt.anitrend.ui.designsystem.aniTrendPlatformTextStyle
import com.mxt.anitrend.ui.favourite.FavouriteAction
import com.mxt.anitrend.ui.favourite.FavouriteActionState
import com.mxt.anitrend.ui.image.AniTrendAvatarImage

/**
 * Compose layout for a feed reply. Rich Markdown and delete-confirmation widgets remain Android views so
 * their parsing and dialog contracts stay unchanged.
 */
@Composable
internal fun CommentReplyRow(
    model: CommentReplyUiModel,
    createdAtText: String,
    isCurrentUser: Boolean,
    onToggleLike: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onMention: () -> Unit,
    onShowLikes: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val typography = MaterialTheme.typography
    val rowTextStyle =
        remember(typography) {
            typography.bodyMedium.copy(
                fontFamily = aniTrendCondensedFontFamily,
                platformStyle = aniTrendPlatformTextStyle,
            )
        }
    val profileDescription =
        if (model.userName.isNullOrBlank()) {
            stringResource(R.string.profile_avatar_description)
        } else {
            stringResource(R.string.description_comment_open_profile, model.userName)
        }
    val likesDescription = stringResource(R.string.title_view_likes)
    val editDescription = stringResource(R.string.description_comment_edit_reply)
    val mentionDescription = stringResource(R.string.description_comment_mention)
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerLow)) }
    val cardElevation = dimensionResource(R.dimen.sm_margin)
    // Preserve the measured 35dp legacy TextView action height.
    val legacyTextActionHeight =
        dimensionResource(R.dimen.widget_spinner_compact_size) +
            dimensionResource(R.dimen.lg_margin) +
            dimensionResource(R.dimen.lg_margin) +
            dimensionResource(R.dimen.xs_margin)

    val favouriteState =
        remember(model.likeCount, model.isLikedByCurrentUser, model.isLikePending) {
            FavouriteActionState(
                count = model.likeCount,
                isLiked = model.isLikedByCurrentUser,
                isEnabled = !model.isLikePending,
                isLoading = model.isLikePending,
            )
        }
    val deleteState =
        remember(model.isDeletePending) {
            StatusDeleteWidgetState(
                isEnabled = !model.isDeletePending,
                isLoading = model.isDeletePending,
            )
        }

    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = cardElevation, vertical = cardElevation * 1.5f),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.xl_margin)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CommentReplyAvatar(
                    imageUrl = model.userAvatar,
                    avatarLabel = model.userName?.firstOrNull()?.toString().orEmpty(),
                    profileDescription = profileDescription,
                    isPreview = isPreview,
                    onClick = onOpenProfile,
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.lg_margin)))
                Text(
                    text = model.userName.orEmpty(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = rowTextStyle,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = createdAtText,
                    modifier = Modifier.weight(1.8f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = rowTextStyle,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))
            CommentReplyBody(
                reply = model.reply,
                textStyle = rowTextStyle,
                isPreview = isPreview,
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))

            Box(modifier = Modifier.fillMaxWidth()) {
                CommentActionIcon(
                    iconResource = R.drawable.ic_people_grey_600_18dp,
                    description = likesDescription,
                    onClick = onShowLikes,
                    modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .heightIn(min = legacyTextActionHeight)
                        .testTag(R.id.widget_users.toString()),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    verticalAlignment = Alignment.Top,
                ) {
                    FavouriteAction(
                        state = favouriteState,
                        onClick = onToggleLike,
                        modifier = Modifier.testTag(R.id.widget_favourite.toString()),
                    )
                    if (isCurrentUser) {
                        CommentActionIcon(
                            iconResource = R.drawable.ic_edit_green_600_18dp,
                            description = editDescription,
                            onClick = onEdit,
                            modifier =
                            Modifier
                                .padding(start = dimensionResource(R.dimen.lg_margin))
                                .heightIn(min = legacyTextActionHeight)
                                .testTag(R.id.widget_edit.toString()),
                        )
                        FeedDeleteAction(
                            state = deleteState,
                            onClick = onDelete,
                            modifier = Modifier.padding(start = dimensionResource(R.dimen.lg_margin)),
                        )
                    } else {
                        CommentActionIcon(
                            iconResource = R.drawable.ic_reply_blue_600_18dp,
                            description = mentionDescription,
                            onClick = onMention,
                            modifier = Modifier.padding(start = dimensionResource(R.dimen.lg_margin)).testTag(R.id.widget_mention.toString()),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentReplyAvatar(
    imageUrl: String?,
    avatarLabel: String,
    profileDescription: String,
    isPreview: Boolean,
    onClick: () -> Unit,
) {
    val avatarSize = dimensionResource(R.dimen.avatar_ripple_radius)
    if (isPreview) {
        Box(
            modifier =
            Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(
                    interactionSource = null,
                    indication = ripple(),
                    role = Role.Button,
                    onClick = onClick,
                ).semantics(mergeDescendants = true) {
                    contentDescription = profileDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = avatarLabel,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    } else {
        Box(
            modifier =
            Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .clickable(
                    interactionSource = null,
                    indication = ripple(),
                    role = Role.Button,
                    onClick = onClick,
                ).semantics(mergeDescendants = true) {
                    contentDescription = profileDescription
                },
        ) {
            AniTrendAvatarImage(
                imageUrl = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun CommentReplyBody(
    reply: String?,
    textStyle: androidx.compose.ui.text.TextStyle,
    isPreview: Boolean,
) {
    if (isPreview) {
        Text(
            text = reply ?: "No content available",
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.md_margin)),
            color = MaterialTheme.colorScheme.onSurface,
            style = textStyle,
        )
    } else {
        key(reply) {
            AndroidView(
                factory = { context ->
                    RichMarkdownTextView(context).apply {
                        id = R.id.widget_status_text
                        val padding = resources.getDimensionPixelSize(R.dimen.md_margin)
                        setPadding(padding, padding, padding, padding)
                        tag = reply
                        richMarkDown(reply)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                update = { textView ->
                    if (textView.tag != reply) {
                        textView.tag = reply
                        textView.richMarkDown(reply)
                    }
                },
                onRelease = RichMarkdownTextView::onViewRecycled,
            )
        }
    }
}

@Composable
private fun CommentActionIcon(
    iconResource: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Box(
        modifier =
        modifier
            .clickable(
                interactionSource = null,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            ).padding(dimensionResource(R.dimen.lg_margin))
            .semantics(mergeDescendants = true) {
                contentDescription = description
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResource),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.widget_spinner_compact_size)),
            tint = tint,
        )
    }
}

@Preview(name = "Comment reply row", showBackground = true, widthDp = 360)
@Composable
private fun CommentReplyRowPreview() {
    val reply =
        CommentReplyUiModel(
            id = 77L,
            reply = "A considered reply with a little more text to show the row height.",
            createdAt = 1L,
            userId = 10L,
            userName = "AniTrend user",
            userAvatar = null,
            likes = emptyList(),
            likeCount = 3,
            isLikedByCurrentUser = true,
            isLikePending = false,
            isDeletePending = false,
        )
    AniTrendTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.lg_margin))) {
            CommentReplyRow(
                model = reply.copy(isLikedByCurrentUser = false),
                createdAtText = "1 day ago",
                isCurrentUser = false,
                onToggleLike = {},
                onDelete = {},
                onEdit = {},
                onMention = {},
                onShowLikes = {},
                onOpenProfile = {},
            )
            CommentReplyRow(
                model = reply.copy(isDeletePending = true),
                createdAtText = "1 day ago",
                isCurrentUser = true,
                onToggleLike = {},
                onDelete = {},
                onEdit = {},
                onMention = {},
                onShowLikes = {},
                onOpenProfile = {},
            )
        }
    }
}
