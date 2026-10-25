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
package com.mxt.anitrend.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.format.mediaFormatLabelRes
import com.mxt.anitrend.ui.media.MediaGridMinTileWidth
import com.mxt.anitrend.ui.media.MediaGridTile
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.ui.util.rememberPresetSnapshots
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.viewmodel.MediaBrowseViewModel

private const val DiscoverPageLimit = 21

/**
 * The Discover screen: one renderer with media-type chips, in-screen preset
 * chips and the shared filter sheet. Query, filter, page and scroll state are
 * keyed by `(mediaType, preset)` and survive recreation; preset switches never
 * add navigation entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    viewModel: MediaBrowseViewModel,
    initialType: String?,
    allowAllTypes: Boolean,
    isGuest: Boolean,
    initialFilters: DiscoverFilters,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFiltersClick: () -> Unit,
    onMediaClick: (Long, String?) -> Unit,
    onMediaLongClick: (Long, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var mediaType by rememberSaveable { mutableStateOf(initialType) }
    var presetName by rememberSaveable { mutableStateOf(DiscoverPreset.BROWSE.name) }
    val preset = remember(presetName) { DiscoverPreset.valueOf(presetName) }
    val snapshots =
        rememberPresetSnapshots(
            initial = DiscoverPresetState(filters = initialFilters),
            serializer = DiscoverPresetState.serializer(),
        )
    val snapshotKey = "${mediaType.orEmpty()}/$presetName"
    val snapshot = snapshots.snapshotFor(snapshotKey)
    val gridState =
        rememberLazyGridState(
            initialFirstVisibleItemIndex = snapshot.firstVisibleIndex,
            initialFirstVisibleItemScrollOffset = snapshot.firstVisibleOffset,
        )

    fun runLoad(page: Int) {
        val query =
            discoverQuery(
                preset = preset,
                mediaType = mediaType,
                filters = snapshot.filters,
                favouriteGenres = viewModel.topFavouriteGenres(),
                favouriteTags = viewModel.topFavouriteTags(),
            )
        viewModel.load(
            type = query.type?.let { runCatching { MediaType.valueOf(it) }.getOrNull() },
            page = page,
            pageLimit = DiscoverPageLimit,
            season = query.season,
            sort = query.sort,
            isAdult = query.isAdult,
            format = query.format,
            seasonYear = query.seasonYear,
            startDateLike = query.startDateLike,
            status = query.status,
            genres = query.genres,
            tags = query.tags,
            onList = query.onList,
        )
    }

    LaunchedEffect(preset, mediaType) {
        runLoad(snapshot.page)
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            DiscoverBottomBar(
                onMenuClick = onMenuClick,
                onSearchClick = onSearchClick,
                onFiltersClick = onFiltersClick,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TypeChipStrip(
                selectedType = mediaType,
                allowAllTypes = allowAllTypes,
                onTypeSelected = { mediaType = it },
            )
            PresetChipStrip(
                selected = preset,
                showForYou = !isGuest && mediaType != KeyUtil.MANGA,
                onPresetSelected = { next ->
                    snapshots.record(
                        snapshotKey,
                        snapshot.copy(
                            firstVisibleIndex = gridState.firstVisibleItemIndex,
                            firstVisibleOffset = gridState.firstVisibleItemScrollOffset,
                        ),
                    )
                    presetName = next.name
                },
            )
            DiscoverResults(
                state = state,
                gridState = gridState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick,
                onRetry = { runLoad(snapshot.page) },
                onLoadMore = {
                    val nextPage = snapshot.page + 1
                    snapshots.record(snapshotKey, snapshot.copy(page = nextPage))
                    runLoad(nextPage)
                },
            )
        }
    }
}

@Composable
private fun TypeChipStrip(
    selectedType: String?,
    allowAllTypes: Boolean,
    onTypeSelected: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (allowAllTypes) {
            item(key = "type-all") {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text(stringResource(R.string.media_status_all)) },
                )
            }
        }
        item(key = "type-anime") {
            FilterChip(
                selected = selectedType == KeyUtil.ANIME,
                onClick = { onTypeSelected(KeyUtil.ANIME) },
                label = { Text(stringResource(R.string.label_media_type_anime)) },
            )
        }
        item(key = "type-manga") {
            FilterChip(
                selected = selectedType == KeyUtil.MANGA,
                onClick = { onTypeSelected(KeyUtil.MANGA) },
                label = { Text(stringResource(R.string.label_media_type_manga)) },
            )
        }
    }
}

@Composable
private fun PresetChipStrip(
    selected: DiscoverPreset,
    showForYou: Boolean,
    onPresetSelected: (DiscoverPreset) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val presets =
            buildList {
                add(DiscoverPreset.BROWSE to R.string.discover_preset_browse)
                add(DiscoverPreset.TRENDING to R.string.discover_preset_trending)
                add(DiscoverPreset.RECENT to R.string.discover_preset_recent)
                if (showForYou) add(DiscoverPreset.FOR_YOU to R.string.discover_preset_for_you)
            }
        presets.forEach { (preset, labelRes) ->
            item(key = "preset-${preset.name}") {
                FilterChip(
                    selected = selected == preset,
                    onClick = { onPresetSelected(preset) },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }
    }
}

@Composable
private fun DiscoverBottomBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFiltersClick: () -> Unit,
) {
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
        IconButton(onClick = onFiltersClick) {
            Icon(painter = painterResource(R.drawable.ic_filter_list_grey_600_24dp), contentDescription = stringResource(R.string.action_menu))
        }
    }
}

@Composable
private fun DiscoverResults(
    state: MediaBrowseViewModel.UiState,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onMediaClick: (Long, String?) -> Unit,
    onMediaLongClick: (Long, String?) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    when (state) {
        is MediaBrowseViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
        is MediaBrowseViewModel.UiState.Error -> ErrorState(message = state.message, onRetry = onRetry, modifier = Modifier.fillMaxSize())
        is MediaBrowseViewModel.UiState.Success -> {
            val media = state.content.pageData
            if (media.isEmpty()) {
                EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
            } else {
                val shouldLoadMore by remember(media) {
                    derivedStateOf {
                        val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisible >= media.size - 3
                    }
                }
                LaunchedEffect(shouldLoadMore, media.size) {
                    if (shouldLoadMore) onLoadMore()
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(MediaGridMinTileWidth),
                    state = gridState,
                    contentPadding = contentPadding,
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
                            onClick = { onMediaClick(item.id, item.type) },
                            onLongClick = { onMediaLongClick(item.id, item.type) },
                        )
                    }
                }
            }
        }
    }
}
