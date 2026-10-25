package com.mxt.anitrend.ui.notification

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.NotificationItemUiModel
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.LazyGridLoadMoreEffect
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

internal const val NOTIFICATION_ROW_TAG = "notification-row-card"
internal const val NOTIFICATION_AVATAR_TAG = "notification-avatar"

/**
 * Immutable projection of one notification row, reproducing the retired adapter's
 * bindings (subject/header/content rules, unread chrome, image fallbacks, and the
 * row content description) outside any view holder.
 */
internal data class NotificationRowUiState(
    val id: Long,
    val isRead: Boolean,
    val subject: String,
    val header: String,
    val content: String,
    val time: String,
    val isMediaVariant: Boolean,
    val imageUrl: String?,
    val imageErrorResource: Int,
    val rowContentDescription: String,
) {
    companion object {
        private val MEDIA_TYPES =
            setOf(
                KeyUtil.AIRING,
                KeyUtil.RELATED_MEDIA_ADDITION,
                KeyUtil.MEDIA_DATA_CHANGE,
                KeyUtil.MEDIA_MERGE,
                KeyUtil.MEDIA_DELETION,
            )

        private val CONTEXT_TYPES =
            setOf(
                KeyUtil.ACTIVITY_MESSAGE,
                KeyUtil.FOLLOWING,
                KeyUtil.ACTIVITY_MENTION,
                KeyUtil.THREAD_COMMENT_MENTION,
                KeyUtil.THREAD_SUBSCRIBED,
                KeyUtil.THREAD_COMMENT_REPLY,
                KeyUtil.ACTIVITY_LIKE,
                KeyUtil.ACTIVITY_REPLY,
                KeyUtil.ACTIVITY_REPLY_SUBSCRIBED,
                KeyUtil.ACTIVITY_REPLY_LIKE,
                KeyUtil.THREAD_LIKE,
                KeyUtil.THREAD_COMMENT_LIKE,
            )

        private val FORUM_TYPES =
            setOf(
                KeyUtil.THREAD_COMMENT_MENTION,
                KeyUtil.THREAD_SUBSCRIBED,
                KeyUtil.THREAD_COMMENT_REPLY,
                KeyUtil.THREAD_LIKE,
                KeyUtil.THREAD_COMMENT_LIKE,
            )

        fun from(
            context: Context,
            model: NotificationItemUiModel,
        ): NotificationRowUiState {
            val record = model.record
            val isMediaVariant = record.type in MEDIA_TYPES
            val userName = record.user?.name.orEmpty()
            val header: String
            val content: String
            when (record.type) {
                in CONTEXT_TYPES -> {
                    header = userName
                    content = record.context.orEmpty()
                }
                KeyUtil.AIRING -> {
                    header = record.media?.titleUserPreferred.orEmpty()
                    content =
                        context.getString(
                            R.string.notification_episode,
                            record.episode?.toString().orEmpty(),
                            record.media?.titleUserPreferred,
                        )
                }
                KeyUtil.RELATED_MEDIA_ADDITION,
                KeyUtil.MEDIA_DATA_CHANGE,
                -> {
                    header = record.media?.titleUserPreferred.orEmpty()
                    content = record.context.orEmpty()
                }
                KeyUtil.MEDIA_MERGE -> {
                    header = record.deletedMediaTitles.joinToString(", ")
                    content = record.context.orEmpty()
                }
                KeyUtil.MEDIA_DELETION -> {
                    header = record.deletedMediaTitle.orEmpty()
                    content = record.context.orEmpty()
                }
                // Unknown types render a safe default subject with whatever user
                // or media identity is available.
                else -> {
                    header = userName.ifBlank { record.media?.titleUserPreferred.orEmpty() }
                    content = record.context.orEmpty()
                }
            }
            val descriptionHeader: String? =
                when (record.type) {
                    KeyUtil.AIRING,
                    KeyUtil.RELATED_MEDIA_ADDITION,
                    KeyUtil.MEDIA_DATA_CHANGE,
                    -> record.media?.titleUserPreferred
                    KeyUtil.MEDIA_MERGE -> record.deletedMediaTitles.joinToString(", ").ifBlank { null }
                    KeyUtil.MEDIA_DELETION -> record.deletedMediaTitle
                    else -> record.user?.name?.ifBlank { null } ?: record.media?.titleUserPreferred
                }
            val subject = context.getString(subjectResource(record.type))
            val time = DateUtil.getPrettyDateUnix(record.createdAt).orEmpty()
            val imageUrl =
                if (isMediaVariant) {
                    record.media?.coverImage
                } else {
                    record.user?.avatar?.takeIf { it.isNotBlank() } ?: record.media?.coverImage
                }
            val rowContentDescription =
                buildString {
                    append(subject)
                    if (!descriptionHeader.isNullOrBlank()) {
                        append(", ")
                        append(descriptionHeader)
                    }
                    if (!record.context.isNullOrBlank()) {
                        append(", ")
                        append(record.context)
                    }
                    append(", ")
                    append(time)
                    if (!model.isRead) {
                        append(", ")
                        append(context.getString(R.string.layout_unread_state))
                    }
                }
            return NotificationRowUiState(
                id = model.id,
                isRead = model.isRead,
                subject = subject,
                header = header,
                content = content,
                time = time,
                isMediaVariant = isMediaVariant,
                imageUrl = imageUrl,
                imageErrorResource =
                if (record.type in FORUM_TYPES) {
                    R.drawable.ic_mode_comment_grey_600_18dp
                } else {
                    R.drawable.ic_account_circle_grey_600_24dp
                },
                rowContentDescription = rowContentDescription,
            )
        }

        private fun subjectResource(type: String?): Int = when (type) {
            KeyUtil.ACTIVITY_MESSAGE -> R.string.notification_user_activity_message
            KeyUtil.FOLLOWING -> R.string.notification_user_follow_activity
            KeyUtil.ACTIVITY_MENTION -> R.string.notification_user_activity_mention
            KeyUtil.THREAD_COMMENT_MENTION,
            KeyUtil.THREAD_SUBSCRIBED,
            KeyUtil.THREAD_COMMENT_REPLY,
            -> R.string.notification_user_comment_forum
            KeyUtil.AIRING -> R.string.notification_series
            KeyUtil.ACTIVITY_LIKE -> R.string.notification_user_like_activity
            KeyUtil.ACTIVITY_REPLY,
            KeyUtil.ACTIVITY_REPLY_SUBSCRIBED,
            -> R.string.notification_user_reply_activity
            KeyUtil.ACTIVITY_REPLY_LIKE -> R.string.notification_user_like_reply
            KeyUtil.THREAD_LIKE -> R.string.notification_user_like_activity
            KeyUtil.THREAD_COMMENT_LIKE -> R.string.notification_user_like_comment
            KeyUtil.RELATED_MEDIA_ADDITION -> R.string.notification_media_added
            KeyUtil.MEDIA_DATA_CHANGE -> R.string.notification_media_data_change
            KeyUtil.MEDIA_MERGE -> R.string.notification_media_merge
            KeyUtil.MEDIA_DELETION -> R.string.notification_media_deletion
            else -> R.string.notification_default
        }
    }
}

/**
 * Stateless notification row reproducing the person and media card layouts. Row
 * clicks and avatar clicks are separate targets (the fragment routes them to
 * different destinations), and long clicks only fire on the row surface.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun NotificationRow(
    state: NotificationRowUiState,
    onClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .testTag(NOTIFICATION_ROW_TAG)
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.lg_margin))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .semantics { contentDescription = state.rowContentDescription },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (!state.isRead) {
                Box(
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.md_margin))
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
            if (state.isMediaVariant) {
                NotificationImage(
                    state = state,
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(1f / KeyUtil.AspectRatio),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(dimensionResource(R.dimen.xl_margin)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = state.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.lg_margin)),
                    )
                }
                Text(
                    text = state.header,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (state.isRead) FontWeight.Normal else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.lg_margin)),
                )
                Text(
                    text = state.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.md_margin)),
                )
            }
            if (!state.isMediaVariant) {
                NotificationImage(
                    state = state,
                    modifier = Modifier
                        .padding(
                            top = dimensionResource(R.dimen.xl_margin),
                            end = dimensionResource(R.dimen.xl_margin),
                            bottom = dimensionResource(R.dimen.xl_margin),
                        )
                        .width(dimensionResource(R.dimen.avatar_size))
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .testTag(NOTIFICATION_AVATAR_TAG)
                        .clickable(onClick = onAvatarClick),
                )
            }
        }
    }
}

/**
 * Notification row image: the retired rows always showed a fallback drawable when
 * no image URL existed, so the null-model branch paints the fallback explicitly
 * instead of collapsing.
 */
@Composable
private fun NotificationImage(
    state: NotificationRowUiState,
    modifier: Modifier = Modifier,
) {
    if (state.imageUrl == null) {
        Image(
            painter = painterResource(state.imageErrorResource),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        AniTrendImage(
            model = state.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            errorResource = state.imageErrorResource,
            painterContentShape = if (state.isMediaVariant) null else CircleShape,
            modifier = modifier,
        )
    }
}

/**
 * Stateless notifications list. The host forwards navigation, refresh, and
 * read-state actions.
 */
@Composable
internal fun NotificationList(
    items: List<NotificationItemUiModel>,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onRowClick: (NotificationItemUiModel) -> Unit,
    onAvatarClick: (NotificationItemUiModel) -> Unit,
    onRowLongClick: (NotificationItemUiModel) -> Unit,
    onLoadMore: () -> Unit,
) {
    LazyGridLoadMoreEffect(gridState = listState, onLoadMore = onLoadMore)

    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.single_list_x1)),
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> "notification:${item.id}" },
            contentType = { _, _ -> "notification" },
        ) { _, item ->
            val context = LocalContext.current
            val state = NotificationRowUiState.from(context, item)
            NotificationRow(
                state = state,
                onClick = { onRowClick(item) },
                onAvatarClick = { onAvatarClick(item) },
                onLongClick = { onRowLongClick(item) },
            )
        }
    }
}
