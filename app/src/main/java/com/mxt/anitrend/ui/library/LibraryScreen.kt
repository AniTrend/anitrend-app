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
package com.mxt.anitrend.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.format.mediaListStatusLabelRes
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.MediaListProgressControl
import com.mxt.anitrend.ui.media.MediaListProgressState
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.ui.util.rememberPresetSnapshots
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.viewmodel.MediaListViewModel
import kotlinx.serialization.Serializable

/** Saved per-preset Library state: status selection and scroll position. */
@Serializable
internal data class LibraryPresetState(
    val status: String? = null,
    val firstVisibleIndex: Int = 0,
    val firstVisibleOffset: Int = 0,
)

/**
 * The Library screen: one collection renderer with exactly three presets. The
 * preset shapes only the rendered projection; canonical store state stays
 * preset-free. Status selection and scroll are independent per preset and owner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    viewModel: MediaListViewModel,
    userId: Long,
    userName: String?,
    isGuest: Boolean,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
    onEpisodesClick: () -> Unit,
    onSignInClick: () -> Unit,
    onEntryClick: (MediaListItemUiModel) -> Unit,
    onManageClick: (MediaListItemUiModel) -> Unit,
    onIncrement: (MediaListItemUiModel) -> Unit,
    initialPreset: LibraryPreset = LibraryPreset.MY_ANIME,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var presetName by rememberSaveable { mutableStateOf(initialPreset.name) }
    val preset = remember(presetName) { LibraryPreset.valueOf(presetName) }
    val snapshots =
        rememberPresetSnapshots(
            initial = LibraryPresetState(),
            serializer = LibraryPresetState.serializer(),
        )
    val snapshotKey = "$userId/${userName.orEmpty()}/$presetName"
    val snapshot = snapshots.snapshotFor(snapshotKey)
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = snapshot.firstVisibleIndex,
            initialFirstVisibleItemScrollOffset = snapshot.firstVisibleOffset,
        )

    LaunchedEffect(preset, userId, userName, snapshot.status, isGuest) {
        if (!isGuest) {
            viewModel.load(
                userId = userId,
                userName = userName,
                mediaType = if (preset == LibraryPreset.MY_MANGA) KeyUtil.MANGA else KeyUtil.ANIME,
                statusIn = snapshot.status,
                preset = preset,
            )
        }
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            LibraryBottomBar(
                onMenuClick = onMenuClick,
                onSearchClick = onSearchClick,
                onSortClick = onSortClick,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LibraryPresetStrip(
                selected = preset,
                onPresetSelected = { next ->
                    snapshots.record(
                        snapshotKey,
                        snapshot.copy(
                            firstVisibleIndex = listState.firstVisibleItemIndex,
                            firstVisibleOffset = listState.firstVisibleItemScrollOffset,
                        ),
                    )
                    presetName = next.name
                },
            )
            if (preset != LibraryPreset.AIRING) {
                StatusChipStrip(
                    selectedStatus = snapshot.status,
                    onStatusSelected = { status ->
                        snapshots.record(snapshotKey, snapshot.copy(status = status))
                    },
                )
            }
            when {
                isGuest -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    ) {
                        EmptyState(message = stringResource(R.string.info_login_req))
                        TextButton(onClick = onSignInClick) {
                            Text(stringResource(R.string.drawer_title_sign_in), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                state is MediaListViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                state is MediaListViewModel.UiState.Error -> {
                    ErrorState(
                        message = (state as MediaListViewModel.UiState.Error).message,
                        onRetry = {
                            viewModel.load(
                                userId = userId,
                                userName = userName,
                                mediaType = if (preset == LibraryPreset.MY_MANGA) KeyUtil.MANGA else KeyUtil.ANIME,
                                statusIn = snapshot.status,
                                preset = preset,
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    val success = state as MediaListViewModel.UiState.Success
                    if (success.renderedItems.isEmpty()) {
                        EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(success.renderedItems, key = { "entry:${it.id}" }) { item ->
                                MediaListEntryRow(
                                    item = item,
                                    onClick = { onEntryClick(item) },
                                    onManageClick = { onManageClick(item) },
                                    onIncrement = { onIncrement(item) },
                                )
                            }
                            item(key = "episodes-action") {
                                TextButton(onClick = onEpisodesClick) {
                                    Text(
                                        text = stringResource(R.string.airing_title_latest_episodes),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryPresetStrip(
    selected: LibraryPreset,
    onPresetSelected: (LibraryPreset) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val presets =
            listOf(
                LibraryPreset.MY_ANIME to R.string.library_preset_my_anime,
                LibraryPreset.MY_MANGA to R.string.library_preset_my_manga,
                LibraryPreset.AIRING to R.string.library_preset_airing,
            )
        items(presets, key = { "library-preset-${it.first.name}" }) { (preset, labelRes) ->
            FilterChip(
                selected = selected == preset,
                onClick = { onPresetSelected(preset) },
                label = { Text(stringResource(labelRes)) },
            )
        }
    }
}

@Composable
private fun StatusChipStrip(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "status-all") {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusSelected(null) },
                label = { Text(stringResource(R.string.media_status_all)) },
            )
        }
        val statuses =
            listOf(
                KeyUtil.CURRENT,
                KeyUtil.PLANNING,
                KeyUtil.COMPLETED,
                KeyUtil.DROPPED,
                KeyUtil.PAUSED,
                KeyUtil.REPEATING,
            )
        items(statuses, key = { "status-$it" }) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(stringResource(mediaListStatusLabelRes(status))) },
            )
        }
    }
}

@Composable
private fun LibraryBottomBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit,
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
        IconButton(onClick = onSortClick) {
            Icon(painter = painterResource(R.drawable.ic_filter_list_grey_600_24dp), contentDescription = stringResource(R.string.menu_title_sorting))
        }
    }
}

@Composable
private fun MediaListEntryRow(
    item: MediaListItemUiModel,
    onClick: () -> Unit,
    onManageClick: () -> Unit,
    onIncrement: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AniTrendImage(
                model = item.mediaCoverImage,
                contentDescription = item.mediaTitle,
                modifier = Modifier.size(width = 64.dp, height = 90.dp).clip(MaterialTheme.shapes.medium),
            )
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.mediaTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val nextAiring = item.nextAiringEpisode
                if (nextAiring != null) {
                    Text(
                        text = stringResource(R.string.series_next_ep) + " " + nextAiring.episode,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MediaListProgressControl(
                    state =
                    MediaListProgressState(
                        progress = item.progress,
                        maxProgress = item.mediaEpisodes.takeIf { it > 0 } ?: item.mediaChapters,
                        isEnabled = item.canIncrement && !item.isIncrementPending,
                        isLoading = item.isIncrementPending,
                        status = item.mediaStatus,
                        mediaType = item.mediaType,
                    ),
                    onIncrement = onIncrement,
                    onBusyClick = {},
                    onLimitReached = onManageClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
