package com.mxt.anitrend.ui.favourite

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.mxt.anitrend.data.favourite.FavEntity
import com.mxt.anitrend.data.favourite.FavMedia
import com.mxt.anitrend.data.favourite.FavStudio
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFavouritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMedia: (Long) -> Unit = {},
    onNavigateToCharacter: (Long) -> Unit = {},
    onNavigateToStaff: (Long) -> Unit = {},
    onNavigateToStudio: (Long) -> Unit = {},
    viewModel: UserFavouritesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val scope = rememberCoroutineScope()
    val tabs = FavouriteTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favourites") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is UserFavouritesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is UserFavouritesUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UserFavouritesUiState.Success -> {
                Column(modifier = Modifier.padding(padding)) {
                    TabRow(selectedTabIndex = selectedTab.ordinal) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                    viewModel.selectTab(tab)
                                },
                                text = { Text(tab.name) },
                            )
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (tabs[page]) {
                            FavouriteTab.Anime -> FavMediaList(state.favourites.anime, onNavigateToMedia)
                            FavouriteTab.Manga -> FavMediaList(state.favourites.manga, onNavigateToMedia)
                            FavouriteTab.Characters -> FavEntityList(state.favourites.characters, onNavigateToCharacter)
                            FavouriteTab.Staff -> FavStaffList(state.favourites.staff, onNavigateToStaff)
                            FavouriteTab.Studios -> FavStudioList(state.favourites.studios, onNavigateToStudio)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavMediaList(items: List<FavMedia>, onNavigateToMedia: (Long) -> Unit) {
    if (items.isEmpty()) {
        EmptyFavList("No favourite media yet")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(items, key = { it.id }) { item ->
                FavMediaCard(item, onClick = { onNavigateToMedia(item.id) })
            }
        }
    }
}

@Composable
private fun FavMediaCard(item: FavMedia, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                listOfNotNull(item.type, item.format).forEachIndexed { idx, v ->
                    if (idx > 0) {
                        Text(
                            " · ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        v,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item.meanScore?.let { score ->
            Text(
                "$score%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FavEntityList(items: List<FavEntity>, onNavigate: (Long) -> Unit) {
    if (items.isEmpty()) {
        EmptyFavList("No favourite characters yet")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(items, key = { it.id }) { item ->
                FavEntityCard(item.name, item.imageMedium, onClick = { onNavigate(item.id) })
            }
        }
    }
}

@Composable
private fun FavStaffList(items: List<FavEntity>, onNavigate: (Long) -> Unit) {
    if (items.isEmpty()) {
        EmptyFavList("No favourite staff yet")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(items, key = { it.id }) { item ->
                FavEntityCard(item.name, item.imageMedium, onClick = { onNavigate(item.id) })
            }
        }
    }
}

@Composable
private fun FavStudioList(items: List<FavStudio>, onNavigate: (Long) -> Unit) {
    if (items.isEmpty()) {
        EmptyFavList("No favourite studios yet")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(items, key = { it.id }) { item ->
                FavStudioCard(item, onClick = { onNavigate(item.id) })
            }
        }
    }
}

@Composable
private fun FavEntityCard(name: String, imageMedium: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(1),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FavStudioCard(item: FavStudio, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
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
            if (item.isAnimationStudio) {
                Text(
                    "Animation Studio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun EmptyFavList(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
