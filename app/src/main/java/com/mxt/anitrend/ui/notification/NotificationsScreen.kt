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
package com.mxt.anitrend.ui.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.viewmodel.NotificationViewModel

/**
 * Notifications screen: unread styling on notification rows, Mark all read
 * clearing the unread badge, and record-type navigation (activity, media,
 * thread or nothing) through [onOpenNotification]. The deleted-activity guard
 * keeps the row tappable but performs no navigation for removed records.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationsScreen(
    viewModel: NotificationViewModel,
    unreadCount: Int,
    onOpenActivity: (Long) -> Unit,
    onOpenMedia: (Long) -> Unit,
    onOpenThread: (Long) -> Unit,
    onMarkAllRead: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.load(page = 1)
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
                TextButton(onClick = onBackClick) {
                    Text(text = stringResource(R.string.action_back), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = onMarkAllRead) {
                    Text(text = stringResource(R.string.notification_mark_all_read), style = MaterialTheme.typography.labelLarge)
                }
                if (unreadCount > 0) {
                    Text(
                        text = unreadCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                is NotificationViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is NotificationViewModel.UiState.Error -> {
                    ErrorState(
                        message = current.message,
                        onRetry = { viewModel.load(page = 1) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is NotificationViewModel.UiState.Success -> {
                    if (current.content.notifications.isEmpty()) {
                        EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                    } else {
                        val shouldLoadMore by remember(current.content.notifications.size) {
                            derivedStateOf {
                                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                lastVisible >= current.content.notifications.size - 3
                            }
                        }
                        LaunchedEffect(shouldLoadMore, current.content.notifications.size) {
                            if (shouldLoadMore) {
                                viewModel.load(
                                    page =
                                    (
                                        current.content.pageInfo?.currentPage
                                            ?: (current.content.notifications.size / 21)
                                        ) + 1,
                                )
                            }
                        }
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                current.content.notifications,
                                key = { "notification:${it.id}" },
                            ) { notification ->
                                NotificationRow(
                                    notification = notification,
                                    onOpenActivity = onOpenActivity,
                                    onOpenMedia = onOpenMedia,
                                    onOpenThread = onOpenThread,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationRecord,
    onOpenActivity: (Long) -> Unit,
    onOpenMedia: (Long) -> Unit,
    onOpenThread: (Long) -> Unit,
) {
    androidx.compose.material3.Card(
        shape = MaterialTheme.shapes.large,
        colors =
        androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = DateUtil.getPrettyDateUnix(notification.createdAt),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val userName = notification.user?.name
            if (!userName.isNullOrBlank()) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (!notification.context.isNullOrBlank()) {
                Text(
                    text = notification.context.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                notification.activityId?.let { id ->
                    TextButton(onClick = { onOpenActivity(id) }) {
                        Text(text = stringResource(R.string.notification_open_activity), style = MaterialTheme.typography.labelLarge)
                    }
                }
                notification.media?.id?.let { mediaId ->
                    TextButton(onClick = { onOpenMedia(mediaId) }) {
                        Text(text = stringResource(R.string.drawer_title_anime), style = MaterialTheme.typography.labelLarge)
                    }
                }
                notification.threadId?.let { threadId ->
                    TextButton(onClick = { onOpenThread(threadId) }) {
                        Text(text = stringResource(R.string.notification_open_thread), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
