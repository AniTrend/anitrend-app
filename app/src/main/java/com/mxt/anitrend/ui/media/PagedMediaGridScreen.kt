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
package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.format.mediaFormatLabelRes
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState

/**
 * Shared See-all screen for paged media collections: relations,
 * recommendations, appearances, credits, favourites and studio media render
 * complete existing paged data through one surface. The optional [header]
 * carries the owning identity (for example a studio header).
 */
@Composable
internal fun PagedMediaGridScreen(
    media: List<MediaBase>,
    isLoading: Boolean,
    errorMessage: String?,
    canLoadMore: Boolean,
    onMediaClick: (MediaBase) -> Unit,
    onMediaLongClick: (MediaBase) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
) {
    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember(media.size, canLoadMore) {
        derivedStateOf {
            canLoadMore &&
                media.isNotEmpty() &&
                (gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= media.size - 3
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
                errorMessage != null && media.isEmpty() ->
                    ErrorState(
                        message = errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize(),
                    )

                isLoading && media.isEmpty() -> LoadingState(modifier = Modifier.fillMaxSize())
                media.isEmpty() -> EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                else ->
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(MediaGridMinTileWidth),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(media, key = { "media:${it.id}" }) { item ->
                            val title = item.title?.userPreferred ?: item.title?.romaji.orEmpty()
                            val formatLabel = stringResource(mediaFormatLabelRes(item.format))
                            val score = item.meanScore
                            val subtitle = if (score > 0) "$formatLabel · $score%" else formatLabel
                            MediaGridTile(
                                imageUrl = item.coverImage?.large,
                                title = title,
                                subtitle = subtitle,
                                contentDescription = title,
                                onClick = { onMediaClick(item) },
                                onLongClick = { onMediaLongClick(item) },
                            )
                        }
                    }
            }
        }
    }
}
