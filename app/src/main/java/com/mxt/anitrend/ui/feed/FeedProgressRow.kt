package com.mxt.anitrend.ui.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendPlatformTextStyle
import com.mxt.anitrend.ui.image.AniTrendAvatarImage
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.favourite.FavouriteAction
import com.mxt.anitrend.ui.favourite.FavouriteActionState
import com.mxt.anitrend.util.KeyUtil

/**
 * Composes progress and media-list feed rows from immutable feed state.
 */
@Composable
internal fun FeedProgressRow(
    model: FeedItemUiModel,
    createdAtText: String,
    isListVariant: Boolean,
    onToggleLike: () -> Unit,
    onShowLikes: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMedia: () -> Unit,
    onLongPressMedia: () -> Boolean,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val typography = MaterialTheme.typography
    val rowTextStyle =
        remember(typography) {
            typography.bodyMedium.copy(platformStyle = aniTrendPlatformTextStyle)
        }
    val smallTextStyle =
        remember(typography) {
            typography.bodySmall.copy(platformStyle = aniTrendPlatformTextStyle)
        }
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainer)) }
    val coverColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerHighest)) }
    val accentColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorAccent)) }
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val profileDescription =
        if (model.userName.isNullOrBlank()) {
            stringResource(R.string.profile_avatar_description)
        } else {
            stringResource(R.string.description_comment_open_profile, model.userName)
        }
    val mediaDescription =
        model.mediaTitleEnglish
            ?.takeIf(String::isNotBlank)
            ?: model.mediaTitleOriginal?.takeIf(String::isNotBlank)
            ?: model.headline.toString()
    val legacyActionHeight =
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
    val deleteCurrentFeed = remember(model, onDelete) { { onDelete(model.id) } }
    val cardShape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin))
    val contextShape = RoundedCornerShape(dimensionResource(R.dimen.mg_margin))

    // CardViewBase compatibility padding leaves a 3dp inset at zero elevation.
    val cardCompatInset = 3.dp
    Card(
        modifier = modifier.fillMaxWidth().padding(cardCompatInset),
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
                FeedProgressAvatar(
                    imageUrl = model.userAvatarUrl,
                    profileDescription = profileDescription,
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
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(contextShape)
                    .background(cardColor)
                    .border(dimensionResource(R.dimen.xs_margin), outlineColor, contextShape)
                    .padding(dimensionResource(R.dimen.lg_margin)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = dimensionResource(R.dimen.mg_margin)),
                ) {
                    Text(
                        text = model.headline.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = rowTextStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))
                    Text(
                        text = model.mediaTitleEnglish.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = rowTextStyle.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                    SelectionContainer {
                        Text(
                            text = model.mediaTitleOriginal.orEmpty(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = smallTextStyle,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                FeedProgressMediaImage(
                    imageUrl = model.mediaCoverImageUrl,
                    mediaContentDescription = mediaDescription,
                    backgroundColor = coverColor,
                    outlineColor = outlineColor,
                    isListVariant = isListVariant,
                    onMediaClick = onOpenMedia,
                    onMediaLongClick = onLongPressMedia,
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.mg_margin)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isListVariant) {
                    FeedActionIcon(
                        iconResource = R.drawable.ic_people_grey_600_18dp,
                        description = stringResource(R.string.title_view_likes),
                        onClick = onShowLikes,
                        minimumHeight = legacyActionHeight,
                        modifier = Modifier.testTag(R.id.widget_users.toString()),
                        tint = accentColor,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isListVariant) {
                    FavouriteAction(
                        state = favouriteState,
                        onClick = onToggleLike,
                        modifier = Modifier.testTag(R.id.widget_favourite.toString()),
                    )
                }
                FeedCommentAction(
                    replyCount = model.replyCount,
                    textStyle = rowTextStyle.copy(fontWeight = FontWeight.Bold),
                    description = stringResource(R.string.description_feed_open_comments),
                    onClick = onOpenComments,
                    minimumHeight = legacyActionHeight,
                    modifier =
                    Modifier
                        .padding(start = dimensionResource(R.dimen.lg_margin))
                        .testTag(R.id.widget_comment.toString()),
                    tint = accentColor,
                )
                if (model.canDelete) {
                    FeedDeleteAction(
                        state = deleteState,
                        onClick = deleteCurrentFeed,
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
private fun FeedProgressAvatar(
    imageUrl: String?,
    profileDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
        Modifier
            .size(dimensionResource(R.dimen.avatar_ripple_radius))
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

@Composable
private fun FeedProgressMediaImage(
    imageUrl: String?,
    mediaContentDescription: String,
    backgroundColor: Color,
    outlineColor: Color,
    isListVariant: Boolean,
    onMediaClick: () -> Unit,
    onMediaLongClick: () -> Boolean,
) {
    val mediaShape = RoundedCornerShape(16.dp)
    val mediaActions =
        if (isListVariant) {
            Modifier
        } else {
            Modifier.combinedClickable(
                role = Role.Button,
                onClick = onMediaClick,
                onLongClick = { onMediaLongClick() },
            )
        }

    Box(
        modifier =
        Modifier
            .width(dimensionResource(R.dimen.series_image_xs))
            .aspectRatio(1f / KeyUtil.AspectRatio)
            .clip(mediaShape)
            .background(backgroundColor)
            .border(dimensionResource(R.dimen.xs_margin), outlineColor, mediaShape)
            .then(mediaActions)
            .semantics(mergeDescendants = true) {
                contentDescription = mediaContentDescription
            },
    ) {
        AniTrendImage(
            model = imageUrl?.takeIf(String::isNotBlank),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Preview(name = "Feed progress row", showBackground = true, widthDp = 360)
@Composable
private fun FeedProgressRowPreview() {
    val previewItem =
        FeedItemUiModel(
            id = 42L,
            type = KeyUtil.ANIME,
            headline = "Watching episode 4 of: Sample Series",
            body = null,
            likeCount = 3,
            isLikedByCurrentUser = true,
            replyCount = 12,
            canEdit = false,
            canDelete = true,
            isLikePending = false,
            isDeletePending = false,
            createdAt = 1_600_000_000L,
            userAvatarUrl = null,
            userName = "AniTrend user",
            userId = 10L,
            messengerAvatarUrl = null,
            messengerName = null,
            messengerId = null,
            recipientAvatarUrl = null,
            recipientName = null,
            recipientId = null,
            mediaId = 142L,
            mediaType = KeyUtil.ANIME,
            mediaTitleEnglish = "Sample Series",
            mediaTitleOriginal = "サンプルシリーズ",
            mediaCoverImageUrl = null,
            hasLikes = true,
            likes = emptyList(),
            feedText = null,
        )

    AniTrendTheme {
        FeedProgressRow(
            model = previewItem,
            createdAtText = "25 minutes ago",
            isListVariant = false,
            onToggleLike = {},
            onShowLikes = {},
            onOpenComments = {},
            onOpenProfile = {},
            onOpenMedia = {},
            onLongPressMedia = { false },
            onDelete = {},
        )
    }
}
