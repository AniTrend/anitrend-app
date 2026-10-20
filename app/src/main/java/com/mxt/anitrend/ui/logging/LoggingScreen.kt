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
package com.mxt.anitrend.ui.logging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.log.LogFilter
import com.mxt.anitrend.model.entity.log.LogUiState
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState

/**
 * Application log viewer: level filter chips over the persisted entries
 * with a support metadata card, and Back/Save/Share/Clear bottom actions.
 * The [LoggingState][com.mxt.anitrend.viewmodel.LoggingViewModel] owns
 * loading, filtering and mutations; this screen only renders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoggingScreen(
    state: LogUiState,
    versionLabel: String,
    deviceLabel: String,
    androidLabel: String,
    onFilterSelected: (LogFilter) -> Unit,
    onSaveLog: () -> Unit,
    onShareLog: () -> Unit,
    onClear: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = onBackClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_back),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onSaveLog,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.save_bug_report),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                TextButton(
                    onClick = onShareLog,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.drawer_title_share),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.Delete),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                LogUiState.Loading -> LoadingState()
                is LogUiState.Error ->
                    ErrorState(
                        message = state.message,
                        onRetry = onClear,
                    )
                is LogUiState.Success -> {
                    LogFilterStrip(
                        selected = state.filter,
                        onFilterSelected = onFilterSelected,
                    )
                    if (state.entries.isEmpty()) {
                        EmptyState(message = stringResource(R.string.log_filter_all))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            itemsIndexed(state.entries, key = { index, entry -> "log:$index:${entry.time}:${entry.message.hashCode()}" }) { _, entry ->
                                LogEntryRow(
                                    level = entry.level,
                                    message = entry.message,
                                    selectionState = rememberSelectionState(),
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
private fun LogFilterStrip(
    selected: LogFilter,
    onFilterSelected: (LogFilter) -> Unit,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == LogFilter.All,
            onClick = { onFilterSelected(LogFilter.All) },
            label = { Text(text = stringResource(R.string.log_filter_all)) },
        )
        FilterChip(
            selected = selected == LogFilter.Error,
            onClick = { onFilterSelected(LogFilter.Error) },
            label = { Text(text = stringResource(R.string.log_filter_error)) },
        )
        FilterChip(
            selected = selected == LogFilter.Warning,
            onClick = { onFilterSelected(LogFilter.Warning) },
            label = { Text(text = stringResource(R.string.log_filter_warning)) },
        )
        FilterChip(
            selected = selected == LogFilter.Info,
            onClick = { onFilterSelected(LogFilter.Info) },
            label = { Text(text = stringResource(R.string.log_filter_info)) },
        )
        FilterChip(
            selected = selected == LogFilter.Debug,
            onClick = { onFilterSelected(LogFilter.Debug) },
            label = { Text(text = stringResource(R.string.log_filter_debug)) },
        )
    }
}

/**
 * Support metadata card: version, device and Android build rows rendered
 * as a compact tonal header above the entries.
 */
@Composable
internal fun LoggingMetadataCard(
    versionLabel: String,
    deviceLabel: String,
    androidLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = versionLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = deviceLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = androidLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
