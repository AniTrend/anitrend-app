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
package com.mxt.anitrend.ui.messages

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.mxt.anitrend.viewmodel.MessageFeedViewModel
import io.noties.markwon.Markwon

/** Message mailbox modes rendered as in-screen chips, never separate destinations. */
internal enum class MessagesMailbox(
    val titleRes: Int,
    val type: Int,
) {
    INBOX(R.string.messages_page_title_inbox, KeyUtil.MESSAGE_TYPE_INBOX),
    OUTBOX(R.string.messages_page_title_outbox, KeyUtil.MESSAGE_TYPE_OUTBOX),
}

/**
 * Messages screen: Inbox/Outbox chips above one message list. Messages render
 * as sender/recipient cards, never chat bubbles, and editing a message keeps
 * its original recipient. Bottom actions: Menu, Search and New message.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MessagesScreen(
    viewModel: MessageFeedViewModel,
    ownerId: Long,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNewMessageClick: () -> Unit,
    onOpenProfile: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var mailboxName by rememberSaveable { mutableStateOf(MessagesMailbox.INBOX.name) }
    val mailbox = remember(mailboxName) { MessagesMailbox.valueOf(mailboxName) }
    val context = LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { createMarkwon(context, linkHandler) }
    val listState = rememberLazyListState()

    fun loadPage(page: Int) {
        viewModel.load(
            userId = ownerId,
            page = page,
            pageLimit = KeyUtil.PAGING_LIMIT,
            messageType = mailbox.type,
            currentUserId = ownerId,
        )
    }

    LaunchedEffect(mailbox, ownerId) {
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
                TextButton(onClick = onMenuClick) {
                    Text(text = stringResource(R.string.action_menu), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = onSearchClick) {
                    Text(text = stringResource(R.string.action_search), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = onNewMessageClick) {
                    Text(text = stringResource(R.string.action_new_message), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(MessagesMailbox.entries, key = { it.name }) { entry ->
                    FilterChip(
                        selected = mailbox == entry,
                        onClick = { mailboxName = entry.name },
                        label = { Text(stringResource(entry.titleRes)) },
                    )
                }
            }
            when (val current = state) {
                is MessageFeedViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is MessageFeedViewModel.UiState.Error -> {
                    ErrorState(
                        message = current.message,
                        onRetry = { loadPage(1) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is MessageFeedViewModel.UiState.Success -> {
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
                            items(current.items, key = { "message:${it.id}:${it.createdAt}" }) { item ->
                                MessageCard(
                                    item = item,
                                    markwon = markwon,
                                    onLinkClick = linkHandler,
                                    onOpenProfile = onOpenProfile,
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
private fun MessageCard(
    item: FeedItemUiModel,
    markwon: Markwon,
    onLinkClick: (String) -> Unit,
    onOpenProfile: (Long) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = DateUtil.getPrettyDateUnix(item.createdAt),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.body?.toString() ?: stringResource(R.string.label_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SenderRecipientRow(
                    label = stringResource(R.string.message_from_label),
                    name = item.messengerName ?: stringResource(R.string.label_unknown),
                    imageUrl = item.messengerAvatarUrl,
                    userId = item.messengerId,
                    onOpenProfile = onOpenProfile,
                )
                SenderRecipientRow(
                    label = stringResource(R.string.message_to_label),
                    name = item.recipientName ?: stringResource(R.string.label_unknown),
                    imageUrl = item.recipientAvatarUrl,
                    userId = item.recipientId,
                    onOpenProfile = onOpenProfile,
                )
            }
        }
    }
}

@Composable
private fun SenderRecipientRow(
    label: String,
    name: String,
    imageUrl: String?,
    userId: Long?,
    onOpenProfile: (Long) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.material3.TextButton(
            onClick = { userId?.let(onOpenProfile) },
            enabled = userId != null,
        ) {
            Text(text = name, style = MaterialTheme.typography.labelLarge)
        }
    }
}
