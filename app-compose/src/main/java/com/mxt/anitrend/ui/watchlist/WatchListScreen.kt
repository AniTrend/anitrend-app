package com.mxt.anitrend.ui.watchlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.data.watchlist.WatchEntry
import com.mxt.anitrend.data.watchlist.WatchListGroup
import com.mxt.anitrend.data.watchlist.WatchMediaType
import com.mxt.anitrend.ui.widget.ScoreBadge
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private data class WatchTab(val label: String, val type: WatchMediaType)

private val watchTabs = listOf(
    WatchTab("Anime", WatchMediaType.ANIME),
    WatchTab("Manga", WatchMediaType.MANGA),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditList: (Long, String, Long?) -> Unit,
    viewModel: WatchListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { watchTabs.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Lists") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = watchTabs.indexOfFirst { it.type == selectedType }.coerceAtLeast(0)) {
                watchTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = tab.type == selectedType,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.selectType(tab.type)
                        },
                        text = { Text(tab.label) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (val state = uiState) {
                    is WatchListUiState.Loading -> WatchListLoading()
                    is WatchListUiState.Error -> WatchListError(state.message)
                    is WatchListUiState.Success -> {
                        if (state.groups.isEmpty()) {
                            WatchListEmpty()
                        } else {
                            WatchListContent(state.groups, onNavigateToEditList)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchListContent(groups: List<WatchListGroup>, onNavigateToEditList: (Long, String, Long?) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        groups.forEach { group ->
            item(key = "group_${group.name}") {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(group.entries.size, key = { "entry_${group.name}_${group.entries[it].id}" }) { idx ->
                val entry = group.entries[idx]
                WatchEntryCard(entry = entry, onClick = { onNavigateToEditList(entry.mediaId, entry.mediaTitle, entry.id) })
            }
        }
    }
}

@Composable
private fun WatchEntryCard(entry: WatchEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.mediaTitle.take(1),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.mediaTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val total = when (entry.mediaType) {
                "ANIME" -> entry.episodes ?: 0
                "MANGA" -> entry.chapters ?: 0
                else -> 0
            }
            val progress = entry.progress ?: 0
            if (total > 0) {
                val fraction = progress.toFloat() / total.toFloat()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$progress/$total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScoreBadge(score = entry.score)
                Spacer(modifier = Modifier.width(8.dp))
                entry.status?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (entry.nextAiringEpisode != null && entry.nextAiringEpisodeNumber != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ep ${entry.nextAiringEpisodeNumber} soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchListLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun WatchListError(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun WatchListEmpty() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "No list entries found. Log in and add some media.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
