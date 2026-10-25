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
package com.mxt.anitrend.ui.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState

/**
 * Shared See-all screen for paged people collections: characters, staff,
 * followers and following render complete existing paged data through one
 * slot-based surface. The optional [header] carries identity or type chips.
 */
@Composable
internal fun PagedPeopleScreen(
    itemCount: Int,
    isLoading: Boolean,
    errorMessage: String?,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    rowContent: @Composable (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(itemCount, canLoadMore) {
        derivedStateOf {
            canLoadMore &&
                itemCount > 0 &&
                (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= itemCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_white_24dp),
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            header()
            when {
                errorMessage != null && itemCount == 0 ->
                    ErrorState(
                        message = errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize(),
                    )

                isLoading && itemCount == 0 -> LoadingState(modifier = Modifier.fillMaxSize())
                itemCount == 0 -> EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                else ->
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(itemCount, key = { "people:$it" }) { index ->
                            rowContent(index)
                        }
                    }
            }
        }
    }
}
