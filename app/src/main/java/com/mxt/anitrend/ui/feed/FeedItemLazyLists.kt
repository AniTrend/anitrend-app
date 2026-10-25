package com.mxt.anitrend.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.StatusDeleteWidgetState
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterFeedMessageBinding
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.favourite.FavouriteActionController
import com.mxt.anitrend.ui.favourite.FavouriteActionState
import com.mxt.anitrend.ui.media.LOAD_MORE_VISIBLE_THRESHOLD
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import kotlinx.coroutines.flow.collect

internal data class FeedRowCallbacks(
    val onToggleLike: (Long) -> Unit,
    val onDelete: (Long) -> Unit,
    val onOpenMedia: (View, FeedItemUiModel) -> Unit,
    val onOpenComments: (Long) -> Unit,
    val onEdit: (FeedItemUiModel) -> Unit,
    val onShowLikes: (FeedItemUiModel) -> Unit,
    val onOpenProfile: (View, Long) -> Unit,
    val onLongPressMedia: (View, FeedItemUiModel) -> Boolean,
)

internal data class CommentReplyRowCallbacks(
    val onToggleLike: (Long) -> Unit,
    val onDelete: (Long) -> Unit,
    val onEdit: (Long) -> Unit,
    val onMention: (Long) -> Unit,
    val onShowLikes: (Long) -> Unit,
    val onOpenProfile: (View, Long) -> Unit,
)

/** Counts rows present in a complete store snapshot that were not in its prior snapshot. */
internal fun countNewFeedItems(
    previousIds: Set<Long>,
    snapshot: List<FeedItemUiModel>,
): Int {
    val seenIds = previousIds.toMutableSet()
    var newCount = 0
    snapshot.forEach { item ->
        if (seenIds.add(item.id)) newCount++
    }
    return newCount
}

@Composable
internal fun FeedItemLazyList(
    items: List<FeedItemUiModel>,
    listState: LazyListState,
    callbacks: FeedRowCallbacks,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    loadMoreRevision: Int = 0,
) {
    LazyListLoadMoreEffect(listState, loadMoreRevision, onLoadMore)
    val targetView = LocalView.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        items(items, key = { it.id }) { item ->
            FeedItemRow(
                model = item,
                targetView = targetView,
                callbacks = callbacks,
                modifier =
                if (item.type == KeyUtil.MESSAGE) {
                    Modifier
                } else {
                    Modifier.padding(
                        start = dimensionResource(R.dimen.lg_margin),
                        top = dimensionResource(R.dimen.lg_margin),
                        end = dimensionResource(R.dimen.lg_margin),
                    )
                },
            )
        }
    }
}

@Composable
internal fun CommentThreadLazyList(
    feed: FeedItemUiModel?,
    replies: List<CommentReplyUiModel>,
    currentUserId: Long?,
    listState: LazyListState,
    feedCallbacks: FeedRowCallbacks,
    replyCallbacks: CommentReplyRowCallbacks,
    modifier: Modifier = Modifier,
) {
    val targetView = LocalView.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        if (feed != null) {
            item(key = "origin:${feed.id}") {
                FeedItemRow(
                    model = feed,
                    targetView = targetView,
                    callbacks = feedCallbacks,
                    modifier =
                    if (feed.type == KeyUtil.MESSAGE) {
                        Modifier.testTag("comment-origin-${feed.id}")
                    } else {
                        Modifier
                            .padding(
                                start = dimensionResource(R.dimen.lg_margin),
                                top = dimensionResource(R.dimen.lg_margin),
                                end = dimensionResource(R.dimen.lg_margin),
                            ).testTag("comment-origin-${feed.id}")
                    },
                )
            }
        }
        items(replies, key = { "reply:${it.id}" }) { reply ->
            CommentReplyRow(
                model = reply,
                createdAtText = remember(reply.createdAt) { DateUtil.getPrettyDateUnix(reply.createdAt) },
                isCurrentUser = reply.userId != null && reply.userId == currentUserId,
                onToggleLike = { replyCallbacks.onToggleLike(reply.id) },
                onDelete = { replyCallbacks.onDelete(reply.id) },
                onEdit = { replyCallbacks.onEdit(reply.id) },
                onMention = { replyCallbacks.onMention(reply.id) },
                onShowLikes = { replyCallbacks.onShowLikes(reply.id) },
                onOpenProfile = { reply.userId?.let { replyCallbacks.onOpenProfile(targetView, it) } },
                modifier =
                Modifier
                    .padding(all = dimensionResource(R.dimen.lg_margin))
                    .testTag("comment-reply-${reply.id}"),
            )
        }
    }
}

@Composable
private fun FeedItemRow(
    model: FeedItemUiModel,
    targetView: View,
    callbacks: FeedRowCallbacks,
    modifier: Modifier,
) {
    val createdAtText = remember(model.createdAt) { DateUtil.getPrettyDateUnix(model.createdAt) }
    when {
        model.type == KeyUtil.TEXT -> {
            FeedStatusRow(
                model = model,
                createdAtText = createdAtText,
                onToggleLike = { callbacks.onToggleLike(model.id) },
                onShowLikes = { callbacks.onShowLikes(model) },
                onOpenComments = { callbacks.onOpenComments(model.id) },
                onEdit = { callbacks.onEdit(model) },
                onDelete = callbacks.onDelete,
                onOpenProfile = { model.userId?.let { callbacks.onOpenProfile(targetView, it) } },
                modifier = modifier,
            )
        }
        model.type == KeyUtil.MESSAGE -> {
            FeedMessageRow(
                model = model,
                createdAtText = createdAtText,
                callbacks = callbacks,
                modifier = modifier,
            )
        }
        else -> {
            FeedProgressRow(
                model = model,
                createdAtText = createdAtText,
                isListVariant = model.type == KeyUtil.MEDIA_LIST && !model.hasLikes,
                onToggleLike = { callbacks.onToggleLike(model.id) },
                onShowLikes = { callbacks.onShowLikes(model) },
                onOpenComments = { callbacks.onOpenComments(model.id) },
                onOpenProfile = { model.userId?.let { callbacks.onOpenProfile(targetView, it) } },
                onOpenMedia = { callbacks.onOpenMedia(targetView, model) },
                onLongPressMedia = { callbacks.onLongPressMedia(targetView, model) },
                onDelete = callbacks.onDelete,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun FeedMessageRow(
    model: FeedItemUiModel,
    createdAtText: String,
    callbacks: FeedRowCallbacks,
    modifier: Modifier,
) {
    val toggleLike = remember(model.id, callbacks.onToggleLike) {
        { callbacks.onToggleLike(model.id) }
    }
    val delete = remember(model, callbacks.onDelete) {
        {
            if (model.canDelete && !model.isDeletePending) callbacks.onDelete(model.id)
        }
    }
    val edit = remember(model, callbacks.onEdit) {
        { if (model.canEdit) callbacks.onEdit(model) }
    }
    val showLikes = remember(model, callbacks.onShowLikes) {
        { callbacks.onShowLikes(model) }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val binding = AdapterFeedMessageBinding.inflate(LayoutInflater.from(context))
            val rowViews = FeedMessageRowViews(binding, FavouriteActionController(binding.widgetFavourite))
            binding.root.tag = rowViews
            binding.root.layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            binding.root
        },
        update = { root ->
            val rowViews = root.tag as FeedMessageRowViews
            val binding = rowViews.binding
            binding.messengerAvatar.setImage(model.messengerAvatarUrl)
            binding.recipientAvatar.setImage(model.recipientAvatarUrl)
            binding.recipientUserName.visibility = View.VISIBLE
            binding.messengerUserName.visibility = View.VISIBLE
            binding.recipientUserName.text = model.recipientName.orEmpty()
            binding.messengerUserName.text = model.messengerName.orEmpty()
            binding.feedTime.text = createdAtText
            binding.widgetStatus.visibility = View.GONE
            val body = model.body?.toString()
            if (!rowViews.hasBoundBody || rowViews.boundBody != body) {
                rowViews.hasBoundBody = true
                rowViews.boundBody = body
                binding.widgetStatusText.tag = body
                binding.widgetStatusText.richMarkDown(body)
            }
            rowViews.favouriteActionController.render(
                FavouriteActionState(
                    count = model.likeCount,
                    isLiked = model.isLikedByCurrentUser,
                    isEnabled = !model.isLikePending,
                    isLoading = model.isLikePending,
                ),
                toggleLike,
            )
            binding.widgetComment.setReplyCount(model.replyCount)
            binding.widgetComment.setOnClickListener { callbacks.onOpenComments(model.id) }
            binding.widgetEdit.visibility = if (model.canEdit) View.VISIBLE else View.GONE
            binding.widgetEdit.setOnClickListener { edit() }
            binding.widgetUsers.visibility = if (model.hasLikes) View.VISIBLE else View.GONE
            binding.widgetUsers.text = model.likeCount.toString()
            binding.widgetUsers.setOnClickListener { callbacks.onShowLikes(model) }
            binding.messengerAvatar.setOnClickListener {
                model.messengerId?.let { userId -> callbacks.onOpenProfile(binding.messengerAvatar, userId) }
            }
            binding.recipientAvatar.setOnClickListener {
                model.recipientId?.let { userId -> callbacks.onOpenProfile(binding.recipientAvatar, userId) }
            }
            if (model.canDelete) {
                binding.widgetDelete.render(
                    StatusDeleteWidgetState(
                        isEnabled = !model.isDeletePending,
                        isLoading = model.isDeletePending,
                    ),
                )
                binding.widgetDelete.visibility = View.VISIBLE
                binding.widgetDelete.setOnDeleteListener(delete)
            } else {
                binding.widgetDelete.visibility = View.GONE
                binding.widgetDelete.setOnDeleteListener(null)
            }
        },
        onRelease = { root ->
            val rowViews = root.tag as? FeedMessageRowViews ?: return@AndroidView
            val binding = rowViews.binding
            rowViews.favouriteActionController.onViewRecycled()
            Glide.with(binding.root).clear(binding.messengerAvatar)
            Glide.with(binding.root).clear(binding.recipientAvatar)
            binding.widgetDelete.setOnDeleteListener(null)
            binding.widgetStatus.onViewRecycled()
            binding.widgetDelete.onViewRecycled()
            root.tag = null
        },
    )
}

private class FeedMessageRowViews(
    val binding: AdapterFeedMessageBinding,
    val favouriteActionController: FavouriteActionController,
) {
    var hasBoundBody = false
    var boundBody: String? = null
}

@Composable
private fun LazyListLoadMoreEffect(
    listState: LazyListState,
    loadMoreRevision: Int,
    onLoadMore: () -> Unit,
) {
    val currentOnLoadMore = rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState, loadMoreRevision) {
        snapshotFlow {
            val info = listState.layoutInfo
            LazyListLoadSample(
                totalItemCount = info.totalItemsCount,
                visibleItemCount = info.visibleItemsInfo.size,
                firstVisibleItem = info.visibleItemsInfo.firstOrNull()?.index ?: 0,
            )
        }.collect { sample ->
            if (
                sample.totalItemCount > 0 &&
                sample.totalItemCount - sample.visibleItemCount <=
                sample.firstVisibleItem + LOAD_MORE_VISIBLE_THRESHOLD
            ) {
                currentOnLoadMore.value()
            }
        }
    }
}

private data class LazyListLoadSample(
    val totalItemCount: Int,
    val visibleItemCount: Int,
    val firstVisibleItem: Int,
)
