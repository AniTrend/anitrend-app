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
package com.mxt.anitrend.ui.episode

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.viewmodel.EpisodesViewModel

/**
 * The external episode feed. Latest/Popular are in-screen presets over the
 * existing channel repository; this screen is never the Airing collection
 * source.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodesScreen(
    viewModel: EpisodesViewModel,
    onBackClick: () -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onEpisodeLongClick: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var popular by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(popular) {
        viewModel.load(popular = popular)
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
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_white_24dp),
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
                TextButton(onClick = { viewModel.load(popular = popular) }) {
                    Text(text = stringResource(R.string.action_refresh), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "episodes-latest") {
                    FilterChip(
                        selected = !popular,
                        onClick = { popular = false },
                        label = { Text(stringResource(R.string.airing_title_latest_episodes)) },
                    )
                }
                item(key = "episodes-popular") {
                    FilterChip(
                        selected = popular,
                        onClick = { popular = true },
                        label = { Text(stringResource(R.string.hub_title_most_popular)) },
                    )
                }
            }
            when (val current = state) {
                is EpisodesViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is EpisodesViewModel.UiState.Error -> {
                    ErrorState(
                        message = current.message,
                        onRetry = { viewModel.load(popular = popular) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is EpisodesViewModel.UiState.Success -> {
                    if (current.episodes.isEmpty()) {
                        EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(current.episodes, key = { "episode:${it.link ?: it.title}" }) { episode ->
                                EpisodeRow(
                                    title = episode.title,
                                    duration = episode.content?.duration.orEmpty(),
                                    imageUrl = episode.thumbnail.firstOrNull()?.url,
                                    onClick = { onEpisodeClick(episode) },
                                    onLongClick = { onEpisodeLongClick(episode) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
