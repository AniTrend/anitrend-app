/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General General Public License as published by
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
package com.mxt.anitrend.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.ui.markdown.rememberMarkdownLinkHandler
import io.noties.markwon.Markwon

/**
 * Entry-owned likes/followers/following sheet: identity-only rows render
 * through the [rowContent] slot, never a full mutable entity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserListSheet(
    titleRes: Int,
    onDismiss: () -> Unit,
    rowContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            rowContent()
        }
    }
}

/**
 * Entry-owned spoiler reader sheet. The decoded spoiler text renders through
 * the shared [MarkdownText] bridge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SpoilerSheet(
    text: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // A spoiler body may itself contain a spoiler link: dispatch it to a
    // nested entry-owned sheet instead of the retired legacy sheet.
    var nestedSpoiler by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { nestedSpoiler = it }
    val markwon: Markwon = remember { createMarkwon(context, linkHandler) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.title_bottom_sheet_spoiler),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            MarkdownText(
                markdown = text ?: stringResource(R.string.layout_empty_response),
                markwon = markwon,
                onLinkClick = linkHandler,
                onLinkLongClick = {},
            )
        }
    }
    if (nestedSpoiler != null) {
        SpoilerSheet(
            text = nestedSpoiler,
            onDismiss = { nestedSpoiler = null },
        )
    }
}

/**
 * Entry-owned confirmation sheet for destructive actions. The confirm
 * action is armed: the first press confirms intent, the second invokes
 * [onConfirm]; dismissal never mutates committed state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfirmSheet(
    titleRes: Int,
    messageRes: Int,
    confirmLabelRes: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var armed by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.Cancel), style = MaterialTheme.typography.labelLarge)
                }
                TextButton(
                    onClick = {
                        if (armed) {
                            onConfirm()
                        } else {
                            armed = true
                        }
                    },
                ) {
                    Text(
                        text = stringResource(if (armed) R.string.Delete else confirmLabelRes),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
