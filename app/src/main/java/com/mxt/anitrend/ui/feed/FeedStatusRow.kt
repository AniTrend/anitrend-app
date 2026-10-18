package com.mxt.anitrend.ui.feed

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidget
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendCondensedFontFamily
import com.mxt.anitrend.ui.designsystem.aniTrendPlatformTextStyle
import com.mxt.anitrend.ui.favourite.FavouriteAction
import com.mxt.anitrend.ui.favourite.FavouriteActionState
import com.mxt.anitrend.ui.image.AniTrendAvatarImage
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.WidgetState

/**
 * Compose layout for a text-status feed row. Rich Markdown and delete confirmation stay in their
 * existing Android views so their rendering and lifecycle behavior remain unchanged.
 */
@Composable
internal fun FeedStatusRow(
    model: FeedItemUiModel,
    createdAtText: String,
    isRecycled: Boolean,
    bindingEpoch: Int,
    onToggleLike: () -> Unit,
    onShowLikes: () -> Unit,
    onOpenComments: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (Long, Int) -> Unit,
    onOpenProfile: () -> Unit,
    onDeleteWidgetCreated: (StatusDeleteWidget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val typography = MaterialTheme.typography
    val rowTextStyle =
        remember(typography) {
            typography.bodyMedium.copy(
                fontFamily = aniTrendCondensedFontFamily,
                platformStyle = aniTrendPlatformTextStyle,
            )
        }
    val smallTextStyle =
        remember(typography) {
            typography.bodySmall.copy(
                fontFamily = aniTrendCondensedFontFamily,
                platformStyle = aniTrendPlatformTextStyle,
            )
        }
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainer)) }
    val bodySurface = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerLow)) }
    val accentColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorAccent)) }
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val profileDescription =
        if (model.userName.isNullOrBlank()) {
            stringResource(R.string.profile_avatar_description)
        } else {
            stringResource(R.string.description_comment_open_profile, model.userName)
        }
    val likesDescription = stringResource(R.string.title_view_likes)
    val commentsDescription = stringResource(R.string.description_feed_open_comments)
    val editDescription = stringResource(R.string.edit_status_title)
    val legacyActionHeight =
        dimensionResource(R.dimen.widget_spinner_compact_size) +
            dimensionResource(R.dimen.lg_margin) +
            dimensionResource(R.dimen.lg_margin) +
            dimensionResource(R.dimen.xs_margin)
    val favouriteState =
        remember(model.likeCount, model.isLikedByCurrentUser, model.isLikePending, isRecycled) {
            if (isRecycled) {
                FavouriteActionState(count = 0, isLiked = false, isEnabled = true, isLoading = false)
            } else {
                FavouriteActionState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser,
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                )
            }
        }
    val deleteState =
        remember(model.isDeletePending, isRecycled) {
            if (isRecycled) {
                StatusDeleteWidgetState(isEnabled = true, isLoading = false)
            } else {
                StatusDeleteWidgetState(
                    isEnabled = !model.isDeletePending,
                    isLoading = model.isDeletePending,
                )
            }
        }
    val deleteCurrentFeed =
        remember(model, bindingEpoch, onDelete) {
            { onDelete(model.id, bindingEpoch) }
        }
    val cardShape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin))
    val bodyShape = RoundedCornerShape(dimensionResource(R.dimen.mg_margin))
    val cardCompatInset = dimensionResource(R.dimen.sm_margin)

    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(
                horizontal = cardCompatInset,
                vertical = cardCompatInset * 1.5f,
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(dimensionResource(R.dimen.xs_margin), outlineColor),
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
                FeedStatusAvatar(
                    imageUrl = model.userAvatarUrl.takeUnless { isRecycled },
                    userName = model.userName,
                    profileDescription = profileDescription,
                    isPreview = isPreview,
                    onClick = onOpenProfile,
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.lg_margin)))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.userName.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = rowTextStyle.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.sm_margin)))
                    Text(
                        text = createdAtText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = smallTextStyle,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.mg_margin)))
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(bodyShape)
                    .background(bodySurface)
                    .border(dimensionResource(R.dimen.xs_margin), outlineColor, bodyShape)
                    .padding(dimensionResource(R.dimen.lg_margin)),
            ) {
                FeedStatusBody(
                    body = model.body?.toString(),
                    textStyle = rowTextStyle,
                    isPreview = isPreview,
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.mg_margin)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FeedActionIcon(
                    iconResource = R.drawable.ic_people_grey_600_18dp,
                    description = likesDescription,
                    onClick = onShowLikes,
                    minimumHeight = legacyActionHeight,
                    modifier = Modifier.testTag(R.id.widget_users.toString()),
                    tint = accentColor,
                )
                Spacer(modifier = Modifier.weight(1f))
                FavouriteAction(
                    state = favouriteState,
                    onClick = onToggleLike,
                    modifier = Modifier.testTag(R.id.widget_favourite.toString()),
                )
                FeedCommentAction(
                    replyCount = model.replyCount,
                    textStyle = rowTextStyle.copy(fontWeight = FontWeight.Bold),
                    description = commentsDescription,
                    onClick = onOpenComments,
                    minimumHeight = legacyActionHeight,
                    modifier =
                    Modifier
                        .padding(start = dimensionResource(R.dimen.lg_margin))
                        .testTag(R.id.widget_comment.toString()),
                    tint = accentColor,
                )
                if (model.canEdit) {
                    FeedActionIcon(
                        iconResource = R.drawable.ic_edit_green_600_18dp,
                        description = editDescription,
                        onClick = onEdit,
                        minimumHeight = legacyActionHeight,
                        modifier =
                        Modifier
                            .padding(start = dimensionResource(R.dimen.lg_margin))
                            .testTag(R.id.widget_edit.toString()),
                    )
                }
                if (model.canDelete) {
                    FeedDeleteAction(
                        isRecycled = isRecycled,
                        state = deleteState,
                        onClick = deleteCurrentFeed,
                        onWidgetCreated = onDeleteWidgetCreated,
                        modifier =
                        Modifier
                            .padding(start = dimensionResource(R.dimen.lg_margin))
                            .testTag(R.id.widget_delete.toString()),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedStatusAvatar(
    imageUrl: String?,
    userName: String?,
    profileDescription: String,
    isPreview: Boolean,
    onClick: () -> Unit,
) {
    val avatarSize = dimensionResource(R.dimen.avatar_ripple_radius)
    val avatarModifier =
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
            }

    if (isPreview) {
        Box(
            modifier = avatarModifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = userName?.firstOrNull()?.toString().orEmpty(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    } else {
        Box(modifier = avatarModifier) {
            AniTrendAvatarImage(
                imageUrl = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun FeedStatusBody(
    body: String?,
    textStyle: TextStyle,
    isPreview: Boolean,
) {
    if (isPreview) {
        Text(
            text = body ?: "No content available",
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = dimensionResource(R.dimen.md_margin),
                    bottom = dimensionResource(R.dimen.sm_margin),
                ),
            color = MaterialTheme.colorScheme.onSurface,
            style = textStyle,
        )
    } else {
        key(body) {
            AndroidView(
                factory = { context ->
                    RichMarkdownTextView(context).apply {
                        id = R.id.widget_status_text
                        setPadding(
                            0,
                            resources.getDimensionPixelSize(R.dimen.md_margin),
                            0,
                            resources.getDimensionPixelSize(R.dimen.sm_margin),
                        )
                        tag = body
                        richMarkDown(body)
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                            )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                update = { textView ->
                    if (textView.tag != body) {
                        textView.tag = body
                        textView.richMarkDown(body)
                    }
                },
                onRelease = RichMarkdownTextView::onViewRecycled,
            )
        }
    }
}

/** Accessible icon button shared by feed-row action bars. */
@Composable
internal fun FeedActionIcon(
    iconResource: Int,
    description: String,
    onClick: () -> Unit,
    minimumHeight: Dp,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Box(
        modifier =
        modifier
            .heightIn(min = minimumHeight)
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

/** Accessible reply-count action shared by feed-row action bars. */
@Composable
internal fun FeedCommentAction(
    replyCount: Int,
    textStyle: TextStyle,
    description: String,
    onClick: () -> Unit,
    minimumHeight: Dp,
    modifier: Modifier = Modifier,
    tint: Color,
) {
    Row(
        modifier =
        modifier
            .heightIn(min = minimumHeight)
            .clickable(
                interactionSource = null,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            ).padding(dimensionResource(R.dimen.lg_margin))
            .semantics(mergeDescendants = true) {
                contentDescription = description
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_mode_comment_grey_600_18dp),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.widget_spinner_compact_size)),
            tint = tint,
        )
        Text(
            text = WidgetState.convertToText(replyCount),
            color = MaterialTheme.colorScheme.onSurface,
            style = textStyle,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Recycler-safe delete affordance shared by feed rows. */
@Composable
internal fun FeedDeleteAction(
    isRecycled: Boolean,
    state: StatusDeleteWidgetState,
    onClick: () -> Unit,
    onWidgetCreated: (StatusDeleteWidget) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        StatusDeleteAction(
            isEnabled = state.isEnabled,
            isLoading = state.isLoading,
            onClick = onClick,
            modifier = modifier,
        )
    } else {
        AndroidView(
            factory = { context ->
                StatusDeleteWidget(context).apply {
                    id = R.id.widget_delete
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    onWidgetCreated(this)
                }
            },
            modifier = modifier,
            update = { widget ->
                if (isRecycled) {
                    widget.setOnDeleteListener(null)
                    widget.onViewRecycled()
                } else {
                    widget.render(state)
                    widget.setOnDeleteListener(onClick)
                }
            },
            onRelease = StatusDeleteWidget::onViewRecycled,
        )
    }
}

@Preview(name = "Feed status row", showBackground = true, widthDp = 360)
@Composable
private fun FeedStatusRowPreview() {
    val previewItem =
        FeedItemUiModel(
            id = 42L,
            type = KeyUtil.TEXT,
            headline = "AniTrend user",
            body = "A status with bold text and a link.",
            likeCount = 3,
            isLikedByCurrentUser = true,
            replyCount = 2,
            canEdit = true,
            canDelete = true,
            isLikePending = false,
            isDeletePending = false,
            createdAt = 1L,
            userAvatarUrl = null,
            userName = "AniTrend user",
            userId = 10L,
            messengerAvatarUrl = null,
            messengerName = null,
            messengerId = null,
            recipientAvatarUrl = null,
            recipientName = null,
            recipientId = null,
            mediaId = null,
            mediaType = null,
            mediaTitleEnglish = null,
            mediaTitleOriginal = null,
            mediaCoverImageUrl = null,
            hasLikes = true,
            likes = emptyList(),
            feedText = "A status with bold text and a link.",
        )
    AniTrendTheme {
        FeedStatusRow(
            model = previewItem,
            createdAtText = "1 day ago",
            isRecycled = false,
            bindingEpoch = 0,
            onToggleLike = {},
            onShowLikes = {},
            onOpenComments = {},
            onEdit = {},
            onDelete = { _, _ -> },
            onOpenProfile = {},
            onDeleteWidgetCreated = {},
        )
    }
}
