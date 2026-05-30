package com.mxt.anitrend.ui.search

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.mxt.anitrend.data.search.CharacterResult
import com.mxt.anitrend.data.search.SearchResult
import com.mxt.anitrend.data.search.StaffResult
import com.mxt.anitrend.data.search.StudioResult
import com.mxt.anitrend.data.search.UserResult
import com.mxt.anitrend.ui.widget.ScoreBadge
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val searchTabs = listOf("Media", "Characters", "Staff", "Studios", "Users")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCharacter: (Long) -> Unit = {},
    onNavigateToStaff: (Long) -> Unit = {},
    onNavigateToStudio: (Long) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val mediaState by viewModel.mediaState.collectAsState()
    val characterState by viewModel.characterState.collectAsState()
    val staffState by viewModel.staffState.collectAsState()
    val studioState by viewModel.studioState.collectAsState()
    val userState by viewModel.userState.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { searchTabs.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Search...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedType.ordinal) {
                searchTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedType.ordinal == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.selectType(SearchType.entries[index])
                        },
                        text = { Text(title) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (SearchType.entries[page]) {
                    SearchType.Media -> SearchResultList(mediaState, onNavigateToDetail)
                    SearchType.Characters -> CharacterResultList(characterState, onNavigateToCharacter)
                    SearchType.Staff -> StaffResultList(staffState, onNavigateToStaff)
                    SearchType.Studios -> StudioResultList(studioState, onNavigateToStudio)
                    SearchType.Users -> UserResultList(userState, onNavigateToProfile)
                }
            }
        }
    }
}

@Composable
private fun SearchResultList(
    state: TypedSearchUiState<SearchResult>,
    onNavigateToDetail: (Long) -> Unit,
) {
    when (state) {
        is TypedSearchUiState.Idle -> IdleMessage("Type at least 3 characters to search")
        is TypedSearchUiState.Loading -> LoadingIndicator()
        is TypedSearchUiState.Error -> ErrorMessage(state.message)
        is TypedSearchUiState.Results -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    SearchResultCard(item = item, onClick = { onNavigateToDetail(item.id) })
                }
            }
        }
    }
}

@Composable
private fun CharacterResultList(
    state: TypedSearchUiState<CharacterResult>,
    onNavigateToCharacter: (Long) -> Unit,
) {
    when (state) {
        is TypedSearchUiState.Idle -> IdleMessage("Type at least 3 characters to search")
        is TypedSearchUiState.Loading -> LoadingIndicator()
        is TypedSearchUiState.Error -> ErrorMessage(state.message)
        is TypedSearchUiState.Results -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    CharacterResultCard(item = item, onClick = { onNavigateToCharacter(item.id) })
                }
            }
        }
    }
}

@Composable
private fun StaffResultList(
    state: TypedSearchUiState<StaffResult>,
    onNavigateToStaff: (Long) -> Unit,
) {
    when (state) {
        is TypedSearchUiState.Idle -> IdleMessage("Type at least 3 characters to search")
        is TypedSearchUiState.Loading -> LoadingIndicator()
        is TypedSearchUiState.Error -> ErrorMessage(state.message)
        is TypedSearchUiState.Results -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    StaffResultCard(item = item, onClick = { onNavigateToStaff(item.id) })
                }
            }
        }
    }
}

@Composable
private fun StudioResultList(
    state: TypedSearchUiState<StudioResult>,
    onNavigateToStudio: (Long) -> Unit,
) {
    when (state) {
        is TypedSearchUiState.Idle -> IdleMessage("Type at least 3 characters to search")
        is TypedSearchUiState.Loading -> LoadingIndicator()
        is TypedSearchUiState.Error -> ErrorMessage(state.message)
        is TypedSearchUiState.Results -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    StudioResultCard(item = item, onClick = { onNavigateToStudio(item.id) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(item: SearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
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
                            text = " · ",
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
            if (item.studios.isNotEmpty()) {
                Text(
                    text = item.studios.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        ScoreBadge(score = item.meanScore, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun CharacterResultCard(item: CharacterResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.name.take(1),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.isFavourite) {
                Text(
                    text = "Favourite",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun StaffResultCard(item: StaffResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.name.take(1),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            item.language?.let { lang ->
                Text(
                    text = lang,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StudioResultCard(item: StudioResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.name.take(1),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.isAnimationStudio) {
                Text(
                    text = "Animation Studio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun IdleMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun UserResultList(
    state: TypedSearchUiState<UserResult>,
    onNavigateToProfile: () -> Unit,
) {
    when (state) {
        is TypedSearchUiState.Idle -> IdleMessage("Type at least 3 characters to search")
        is TypedSearchUiState.Loading -> LoadingIndicator()
        is TypedSearchUiState.Error -> ErrorMessage(state.message)
        is TypedSearchUiState.Results -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    UserResultCard(item = item, onClick = onNavigateToProfile)
                }
            }
        }
    }
}

@Composable
private fun UserResultCard(item: UserResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.name.take(1),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.about?.let { about ->
                Text(
                    text = about,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
