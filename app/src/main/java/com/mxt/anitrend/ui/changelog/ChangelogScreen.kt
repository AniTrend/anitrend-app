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
package com.mxt.anitrend.ui.changelog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.viewmodel.ChangelogViewModel.ChangelogUiState
import io.noties.markwon.Markwon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Structured release-notes timeline over the bundled [ChangelogDocument].
 *
 * The newest release renders expanded under the What's new heading; older
 * releases expand independently. Empty categories are omitted, known dates
 * use the current locale, and the acknowledgements render through the
 * shared markdown bridge. Expansion state survives recreation.
 */
@Composable
internal fun ChangelogScreen(
    state: ChangelogUiState,
    markwon: Markwon,
    onLinkClick: (String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AniTrendScaffold(
        modifier = modifier,
        snackbarHostState = remember { SnackbarHostState() },
        bottomBar = {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
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
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                ChangelogUiState.Loading -> LoadingState()
                ChangelogUiState.Error ->
                    ErrorState(
                        message = stringResource(R.string.changelog_error_message),
                        onRetry = onRetry,
                    )
                ChangelogUiState.Empty -> EmptyState(message = stringResource(R.string.changelog_empty_message))
                is ChangelogUiState.Content ->
                    ChangelogTimeline(
                        state = state,
                        markwon = markwon,
                        onLinkClick = onLinkClick,
                    )
            }
        }
    }
}

@Composable
private fun ChangelogTimeline(
    state: ChangelogUiState.Content,
    markwon: Markwon,
    onLinkClick: (String) -> Unit,
) {
    val releases = state.document.releases
    // Expanded flags for every release; index 0 (newest) starts expanded.
    var expandedFlags by rememberSaveable(releases.size) {
        mutableStateOf(List(releases.size) { index -> index == 0 })
    }

    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "changelog-heading") {
            Text(
                text = stringResource(R.string.changelog_whats_new),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
        }
        items(releases, key = { "changelog-release:${it.version}" }) { release ->
            val index = releases.indexOf(release)
            ChangelogReleaseCard(
                release = release,
                expanded = expandedFlags[index],
                onToggle = {
                    expandedFlags = expandedFlags.toMutableList().also { it[index] = !it[index] }
                },
            )
        }
        if (state.document.acknowledgements.isNotBlank()) {
            item(key = "changelog-acknowledgements") {
                Column {
                    Text(
                        text = stringResource(R.string.changelog_acknowledgements),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MarkdownText(
                        markdown = state.document.acknowledgements,
                        markwon = markwon,
                        onLinkClick = onLinkClick,
                        onLinkLongClick = onLinkClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangelogReleaseCard(
    release: ChangelogRelease,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.app_name) + " v" + release.version,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(modifier = Modifier.weight(1f))
            release.date?.let { date ->
                val formattedDate = remember(date) { formatDate(date) }
                if (formattedDate != null) {
                    Text(
                        text = formattedDate,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            ChangelogCategory(
                titleRes = R.string.changelog_category_added,
                entries = release.added,
            )
            ChangelogCategory(
                titleRes = R.string.changelog_category_improved,
                entries = release.improved,
            )
            ChangelogCategory(
                titleRes = R.string.changelog_category_fixed,
                entries = release.fixed,
            )
            ChangelogCategory(
                titleRes = R.string.changelog_category_known_issues,
                entries = release.knownIssues,
            )
        }
    }
}

@Composable
private fun ChangelogCategory(
    titleRes: Int,
    entries: List<String>,
) {
    if (entries.isEmpty()) return
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        entries.forEach { entry ->
            Text(
                text = entry,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            )
        }
    }
}

/** Formats an ISO `yyyy-MM-dd` date with the current locale, or null when unparsable. */
private fun formatDate(isoDate: String): String? = runCatching {
    LocalDate.parse(isoDate).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
}.getOrNull()
