/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.ui.markdown.rememberMarkdownLinkHandler
import com.mxt.anitrend.ui.sheet.SpoilerSheet
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.viewmodel.FeedListViewModel
import io.noties.markwon.Markwon

/** Feed activity modes rendered as in-screen preset chips, never tabs. */
internal enum class FeedActivityMode(
    val titleRes: Int,
    val isFollowing: Boolean?,
    val type: String?,
    val isMixed: Boolean?,
) {
    PROGRESS(R.string.feed_title_progress, isFollowing = true, type = KeyUtil.MEDIA_LIST, isMixed = null),
    STATUS(R.string.feed_title_status, isFollowing = true, type = KeyUtil.TEXT, isMixed = null),
    PUBLIC_STATUS(R.string.feed_title_public_status, isFollowing = null, type = null, isMixed = true),
}

/**
 * Feed root screen: one renderer over [FeedListViewModel] with activity-mode
 * chips configuring the current query, and row-local Like/Comments/Profile
 * actions dispatching through the existing ViewModel mutation methods.
 * Bottom actions: Menu, Search, Notifications (unread semantics) and a labeled
 * New post action for authenticated users.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedScreen(
    viewModel: FeedListViewModel,
    isGuest: Boolean,
    notificationCount: Int,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onNewPostClick: () -> Unit,
    onOpenProfile: (Long) -> Unit,
    onOpenMedia: (Long, String?) -> Unit,
    onOpenComments: (Long) -> Unit,
    onShowLikes: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var modeName by rememberSaveable { mutableStateOf(FeedActivityMode.PROGRESS.name) }
    val mode = remember(modeName) { FeedActivityMode.valueOf(modeName) }
    val context = LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { createMarkwon(context, linkHandler) }
    val listState = rememberLazyListState()

    fun loadPage(page: Int) {
        viewModel.load(
            page = page,
            pageLimit = KeyUtil.PAGING_LIMIT,
            isFollowing = mode.isFollowing,
            type = mode.type?.let { runCatching { com.mxt.anitrend.graphql.generated.ActivityType.valueOf(it) }.getOrNull() },
            isMixed = mode.isMixed,
            currentUserId = null,
        )
    }

    LaunchedEffect(mode) {
        loadPage(1)
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(painter = painterResource(R.drawable.ic_menu_grey_600_24dp), contentDescription = stringResource(R.string.action_menu))
                }
                IconButton(onClick = onSearchClick) {
                    Icon(painter = painterResource(R.drawable.ic_search_grey_600_24dp), contentDescription = stringResource(R.string.search_hint))
                }
                IconButton(onClick = onNotificationsClick) {
                    Icon(painter = painterResource(R.drawable.ic_notifications_active_white_24dp), contentDescription = stringResource(R.string.title_activity_notification))
                }
                if (!isGuest) {
                    TextButton(onClick = onNewPostClick) {
                        Text(text = stringResource(R.string.action_new_post), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(FeedActivityMode.entries, key = { it.name }) { modeEntry ->
                    FilterChip(
                        selected = mode == modeEntry,
                        onClick = { modeName = modeEntry.name },
                        label = { Text(stringResource(modeEntry.titleRes)) },
                    )
                }
            }
            when (val current = state) {
                is FeedListViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is FeedListViewModel.UiState.Error -> {
                    ErrorState(
                        message = current.message,
                        onRetry = { loadPage(1) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is FeedListViewModel.UiState.Success -> {
                    if (current.items.isEmpty()) {
                        EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                    } else {
                        val shouldLoadMore by remember(current.items.size) {
                            derivedStateOf {
                                (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= current.items.size - 3
                            }
                        }
                        LaunchedEffect(shouldLoadMore, current.items.size) {
                            if (shouldLoadMore) {
                                loadPage((current.loadedPages.maxOrNull() ?: 0) + 1)
                            }
                        }
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(current.items, key = { "feed:${it.id}:${it.createdAt}" }) { item ->
                                FeedRow(
                                    item = item,
                                    markwon = markwon,
                                    onLinkClick = linkHandler,
                                    onOpenProfile = { onOpenProfile(item.id) },
                                    onOpenMedia = { onOpenMedia(item.id, item.type) },
                                    onOpenComments = { onOpenComments(item.id) },
                                    onShowLikes = { onShowLikes(item.id) },
                                    onToggleLike = { viewModel.toggleLike(item.id) },
                                    onDelete = { feedId, _ -> viewModel.deleteFeed(feedId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
}

@Composable
internal fun FeedRow(
    item: FeedItemUiModel,
    markwon: Markwon,
    onLinkClick: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenComments: () -> Unit,
    onShowLikes: () -> Unit,
    onToggleLike: () -> Unit,
    onDelete: (Long, Int) -> Unit,
) {
    FeedStatusRow(
        model = item,
        createdAtText = DateUtil.getPrettyDateUnix(item.createdAt),
        isRecycled = false,
        bindingEpoch = 0,
        onToggleLike = onToggleLike,
        onShowLikes = onShowLikes,
        onOpenComments = onOpenComments,
        onEdit = {},
        onDelete = onDelete,
        onOpenProfile = onOpenProfile,
        markwon = markwon,
        onLinkClick = onLinkClick,
        onLinkLongClick = {},
    )
}
