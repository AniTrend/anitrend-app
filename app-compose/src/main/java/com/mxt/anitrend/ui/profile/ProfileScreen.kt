package com.mxt.anitrend.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.data.profile.UserProfile
import com.mxt.anitrend.ui.favourite.UserFavouritesScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private enum class ProfileTab { Overview, Stats, Favourites }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFavourites: () -> Unit = {},
    onNavigateToAiring: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToThreads: () -> Unit = {},
    onNavigateToGenres: () -> Unit = {},
    onNavigateToWatchList: () -> Unit = {},
    onNavigateToUserFavourites: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = ProfileTab.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var selectedTab by remember { mutableStateOf(ProfileTab.Overview) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                                selectedTab = tab
                            },
                            text = {
                                Text(
                                    when (tab) {
                                        ProfileTab.Overview -> "Overview"
                                        ProfileTab.Stats -> "Stats"
                                        ProfileTab.Favourites -> "Favourites"
                                    }
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProfileUiState.Success -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) { page ->
                    when (tabs[page]) {
                        ProfileTab.Overview -> ProfileOverviewTab(
                            profile = state.profile,
                            onNavigateToFavourites = onNavigateToFavourites,
                            onNavigateToAiring = onNavigateToAiring,
                            onNavigateToReviews = onNavigateToReviews,
                            onNavigateToThreads = onNavigateToThreads,
                            onNavigateToGenres = onNavigateToGenres,
                            onNavigateToWatchList = onNavigateToWatchList,
                            onNavigateToUserFavourites = onNavigateToUserFavourites,
                        )
                        ProfileTab.Stats -> ProfileStatsTab(state.profile)
                        ProfileTab.Favourites -> UserFavouritesScreen(
                            onNavigateBack = {},
                            onNavigateToMedia = onNavigateToDetail,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileOverviewTab(
    profile: UserProfile,
    onNavigateToFavourites: () -> Unit,
    onNavigateToAiring: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToThreads: () -> Unit,
    onNavigateToGenres: () -> Unit,
    onNavigateToWatchList: () -> Unit,
    onNavigateToUserFavourites: () -> Unit,
) {
    var airingNotifications by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = profile.name.take(1).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (profile.about != null) {
                Text(
                    text = profile.about,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                "Notification Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Airing Notifications",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Switch(
                    checked = airingNotifications,
                    onCheckedChange = { airingNotifications = it },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val watched = profile.watchedTime?.let { t -> "${t / 60}h" } ?: "-"
                val chapters = profile.chaptersRead?.toString() ?: "-"
                val notif = profile.unreadNotificationCount?.toString() ?: "-"
                ProfileStat("Watch time", watched)
                ProfileStat("Chapters", chapters)
                ProfileStat("Notifications", notif)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (profile.favouredGenres.isNotEmpty()) {
                Text(
                    "Favourite Genres",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                profile.favouredGenres.take(5).forEach { genre ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = genre.genre ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = genre.amount?.toString() ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (profile.donatorTier != null && profile.donatorTier > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AniList Supporter (Tier ${profile.donatorTier})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToFavourites)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "My Lists",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToWatchList)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.FormatListBulleted,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Watch List",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToUserFavourites)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Favourites",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToReviews)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Reviews & Recommendations",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAiring)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Airing Schedule",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToThreads)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Forum,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Forum",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToGenres)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Genres",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ProfileStatsTab(profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        if (profile.animeStatusDistribution.isNotEmpty()) {
            Text(
                "Anime Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            profile.animeStatusDistribution.forEach { dist ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = dist.status ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = dist.amount?.toString() ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (profile.animeStatusDistribution.isNotEmpty() && profile.mangaStatusDistribution.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (profile.mangaStatusDistribution.isNotEmpty()) {
            Text(
                "Manga Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            profile.mangaStatusDistribution.forEach { dist ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = dist.status ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = dist.amount?.toString() ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (profile.animeStatusDistribution.isEmpty() && profile.mangaStatusDistribution.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No stats available yet. Add anime or manga to your lists.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (profile.favouredYears.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Favourite Years",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            profile.favouredYears.forEach { year ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = year.year?.toString() ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = year.amount?.toString() ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ProfileStat(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
