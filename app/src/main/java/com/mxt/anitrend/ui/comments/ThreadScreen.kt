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
package com.mxt.anitrend.ui.comments

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.feed.CommentReplyRow
import com.mxt.anitrend.ui.feed.FeedStatusRow
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.ui.markdown.rememberMarkdownLinkHandler
import com.mxt.anitrend.ui.sheet.SpoilerSheet
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.viewmodel.CommentViewModel
import io.wax911.emojify.EmojiManager
import io.wax911.emojify.parser.parseToUnicode
import kotlinx.coroutines.launch

/**
 * Thread screen: one feed header followed by its replies, with a fixed bottom
 * reply composer (Back, Share in overflow, context chip, text field and Send)
 * replacing the ordinary action bar. Editing a feed or reply reuses the same
 * composer with an explicit Editing context and Cancel edit action; a failed
 * submission keeps the draft and a pending one disables duplicate sends.
 * A deleted parent returns to the caller.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThreadScreen(
    viewModel: CommentViewModel,
    feedId: Long,
    currentUserId: Long?,
    isGuest: Boolean,
    onBackClick: () -> Unit,
    onOpenProfile: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { createMarkwon(context, linkHandler) }
    val emojiManager = remember { koinOf<EmojiManager>() }

    var editingReplyId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isEditingFeed by rememberSaveable { mutableStateOf(false) }
    var isSendPending by rememberSaveable { mutableStateOf(false) }
    var sendErrorResId by rememberSaveable { mutableStateOf<Int?>(null) }
    var shareOverflowOpen by remember { mutableStateOf(false) }

    val editSeed: String? =
        when {
            isEditingFeed -> state.feedItem?.feedText
            editingReplyId != null -> state.replies.firstOrNull { it.id == editingReplyId }?.reply
            else -> null
        }

    var draft by rememberSaveable(editSeed, stateSaver = TextFieldValue.Saver) {
        val emojified = editSeed?.let { emojiManager.parseToUnicode(it) }.orEmpty()
        mutableStateOf(TextFieldValue(emojified))
    }

    LaunchedEffect(feedId, currentUserId) {
        viewModel.load(feedId = feedId, currentUserId = currentUserId)
    }

    val feedItem = state.feedItem
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBackClick()
    }

    fun submit() {
        val text = draft.text.trim()
        if (text.isEmpty()) {
            sendErrorResId = R.string.warning_empty_input
            return
        }
        if (isSendPending) return
        isSendPending = true
        sendErrorResId = null
        scope.launch {
            val replyId = editingReplyId
            val result =
                when {
                    isEditingFeed -> viewModel.editFeed(feedId = feedId, text = text)
                    else -> viewModel.submitReply(feedId = feedId, text = text, replyId = replyId)
                }
            isSendPending = false
            if (result is MutationResult.Success) {
                editingReplyId = null
                isEditingFeed = false
                draft = TextFieldValue("")
            } else {
                sendErrorResId = R.string.text_error_request
            }
        }
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            // The composer belongs to a rendered thread: a failed or absent
            // feed load shows only its empty state, not a reply affordance
            // for content that never loaded.
            if (feedItem != null) {
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back_white_24dp),
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                        Text(
                            text = stringResource(
                                when {
                                    isEditingFeed -> R.string.text_edit_mode
                                    editingReplyId != null -> R.string.text_edit_mode
                                    else -> R.string.title_activity_reply
                                },
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (isEditingFeed || editingReplyId != null) {
                            TextButton(
                                onClick = {
                                    editingReplyId = null
                                    isEditingFeed = false
                                    draft = TextFieldValue("")
                                },
                            ) {
                                Text(text = stringResource(R.string.Cancel), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(onClick = { shareOverflowOpen = true }) {
                                Text(text = stringResource(R.string.More), style = MaterialTheme.typography.labelLarge)
                            }
                            DropdownMenu(
                                expanded = shareOverflowOpen,
                                onDismissRequest = { shareOverflowOpen = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.drawer_title_share)) },
                                    onClick = {
                                        shareOverflowOpen = false
                                        state.feed?.siteUrl?.let { url ->
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    },
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { value ->
                                draft = value
                                sendErrorResId = null
                            },
                            enabled = !isGuest && !isSendPending,
                            placeholder = {
                                Text(
                                    text = stringResource(
                                        if (isEditingFeed || editingReplyId != null) {
                                            R.string.text_edit_mode
                                        } else {
                                            R.string.title_activity_reply
                                        },
                                    ),
                                )
                            },
                            minLines = 1,
                            maxLines = 6,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { submit() },
                            enabled = !isGuest && !isSendPending,
                        ) {
                            Text(text = stringResource(R.string.action_send), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (sendErrorResId != null) {
                        Text(
                            text = stringResource(sendErrorResId!!),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
                state.isDeleted || feedItem == null ->
                    EmptyState(
                        message = stringResource(R.string.layout_empty_response),
                        modifier = Modifier.fillMaxSize(),
                    )

                else ->
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item(key = "feed-header") {
                            FeedStatusRow(
                                model = feedItem,
                                createdAtText = DateUtil.getPrettyDateUnix(feedItem.createdAt),
                                isRecycled = false,
                                bindingEpoch = 0,
                                onToggleLike = {
                                    scope.launch { viewModel.toggleFeedLike(feedItem.id) }
                                },
                                onShowLikes = {},
                                onOpenComments = {},
                                onEdit = { isEditingFeed = true },
                                onDelete = { feedIdArg, _ ->
                                    scope.launch {
                                        if (viewModel.deleteFeed(feedIdArg) is MutationResult.Success) {
                                            onBackClick()
                                        }
                                    }
                                },
                                onOpenProfile = {
                                    feedItem.userId?.let(onOpenProfile)
                                },
                                markwon = markwon,
                                onLinkClick = linkHandler,
                                onLinkLongClick = {},
                            )
                        }
                        items(state.replies, key = { "reply:${it.id}" }) { reply ->
                            val isCurrentUser = currentUserId != null && reply.userId == currentUserId
                            CommentReplyRow(
                                model = reply,
                                createdAtText = DateUtil.getPrettyDateUnix(reply.createdAt),
                                isCurrentUser = isCurrentUser,
                                isRecycled = false,
                                onToggleLike = {
                                    scope.launch { viewModel.toggleReplyLike(reply.id) }
                                },
                                onDelete = {
                                    scope.launch { viewModel.deleteReply(reply.id) }
                                },
                                onEdit = { editingReplyId = reply.id },
                                onMention = {},
                                onShowLikes = {},
                                onOpenProfile = { reply.userId?.let(onOpenProfile) },
                                markwon = markwon,
                                onLinkClick = linkHandler,
                                onLinkLongClick = {},
                            )
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
