package com.mxt.anitrend.ui.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.data.search.SearchResult
import com.mxt.anitrend.ui.search.SearchViewModel
import com.mxt.anitrend.ui.search.TypedSearchUiState
import com.mxt.anitrend.ui.widget.ScoreBadge
import org.koin.androidx.compose.koinViewModel

private val seasons = listOf("WINTER", "SPRING", "SUMMER", "FALL")
private val formats = listOf("TV", "TV_SHORT", "MOVIE", "OVA", "ONA", "SPECIAL", "MUSIC")
private val genres = listOf("Action", "Adventure", "Comedy", "Drama", "Fantasy", "Horror", "Mecha", "Music", "Mystery", "Psychological", "Romance", "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrowseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    var selectedSeason by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }
    val mediaState by viewModel.mediaState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            item {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = seasonExpanded,
                    onExpandedChange = { seasonExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedSeason.ifEmpty { "Any Season" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Season") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = seasonExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = seasonExpanded,
                        onDismissRequest = { seasonExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Any Season") },
                            onClick = { selectedSeason = ""; seasonExpanded = false },
                        )
                        seasons.forEach { season ->
                            DropdownMenuItem(
                                text = { Text(season) },
                                onClick = { selectedSeason = season; seasonExpanded = false },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = selectedYear,
                    onValueChange = { selectedYear = it.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = formatExpanded,
                    onExpandedChange = { formatExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedFormat.ifEmpty { "Any Format" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Format") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = formatExpanded,
                        onDismissRequest = { formatExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Any Format") },
                            onClick = { selectedFormat = ""; formatExpanded = false },
                        )
                        formats.forEach { format ->
                            DropdownMenuItem(
                                text = { Text(format) },
                                onClick = { selectedFormat = format; formatExpanded = false },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "Genre",
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    genres.forEach { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { selectedGenre = if (selectedGenre == genre) "" else genre },
                            label = { Text(genre) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Button(
                    onClick = {
                        val query = listOfNotNull(
                            selectedSeason.takeIf { it.isNotEmpty() },
                            selectedYear.takeIf { it.isNotEmpty() },
                            selectedFormat.takeIf { it.isNotEmpty() },
                            selectedGenre.takeIf { it.isNotEmpty() },
                        ).joinToString(" ")
                        if (query.isNotEmpty()) {
                            viewModel.onQueryChange(query)
                            hasSearched = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Browse")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                when (val state = mediaState) {
                    is TypedSearchUiState.Idle -> {
                        if (!hasSearched) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Select filters and tap Browse",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    is TypedSearchUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is TypedSearchUiState.Error -> {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    is TypedSearchUiState.Results -> {}
                }
            }

            if (mediaState is TypedSearchUiState.Results) {
                val results = (mediaState as TypedSearchUiState.Results<SearchResult>).items
                items(results, key = { it.id }) { item ->
                    BrowseResultCard(item = item, onClick = { onNavigateToDetail(item.id) })
                }
            }
        }
    }
}

@Composable
private fun BrowseResultCard(item: SearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.title.take(1),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row {
                listOfNotNull(item.type, item.format, item.status).take(3).forEachIndexed { idx, value ->
                    if (idx > 0) {
                        Text(
                            text = " \u00B7 ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        ScoreBadge(score = item.meanScore, modifier = Modifier.padding(start = 8.dp))
    }
}
