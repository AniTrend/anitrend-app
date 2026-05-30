package com.mxt.anitrend.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.data.media.MediaCharacter
import com.mxt.anitrend.data.media.MediaRelation
import com.mxt.anitrend.data.media.MediaSocialItem
import com.mxt.anitrend.data.media.MediaStaffMember
import com.mxt.anitrend.data.media.Ranking
import com.mxt.anitrend.data.media.RecommendationItem
import com.mxt.anitrend.data.media.ScoreDistribution
import com.mxt.anitrend.plugin.text.MarkdownText
import com.mxt.anitrend.ui.widget.FavouriteButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val tabs = listOf("Overview", "Characters", "Staff", "Relations", "Stats", "Social", "Recommendations")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    mediaId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToTrailer: (String) -> Unit = {},
    onNavigateToEditList: ((Long, String) -> Unit)? = null,
    onToggleFavourite: () -> Unit = {},
) {
    val viewModel = koinViewModel<MediaDetailViewModel>(parameters = { parametersOf(mediaId.toInt()) })
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val characters by viewModel.characters.collectAsState()
    val staff by viewModel.staff.collectAsState()
    val relations by viewModel.relations.collectAsState()
    val scoreDistribution by viewModel.scoreDistribution.collectAsState()
    val rankings by viewModel.rankings.collectAsState()
    val social by viewModel.social.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is MediaDetailUiState.Success -> Text(
                            text = state.media.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        else -> Text("Detail")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is MediaDetailUiState.Success) {
                        FavouriteButton(
                            isFavourite = (uiState as MediaDetailUiState.Success).media.isFavourite,
                            onToggle = onToggleFavourite,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is MediaDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is MediaDetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is MediaDetailUiState.Success -> {
                Column(modifier = Modifier.padding(padding)) {
                    TabRow(selectedTabIndex = selectedTab.ordinal) {
                        tabs.forEachIndexed { index, title ->
                            val tab = MediaTab.entries[index]
                            Tab(
                                selected = selectedTab == tab,
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                    viewModel.selectTab(tab)
                                },
                                text = { Text(title) },
                            )
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        when (MediaTab.entries[page]) {
                            MediaTab.Overview -> OverviewTab(
                                state.media,
                                onNavigateToMedia = { /* already here */ },
                                onNavigateToTrailer = onNavigateToTrailer,
                                onNavigateToEditList = onNavigateToEditList,
                            )
                            MediaTab.Characters -> CharactersTab(characters)
                            MediaTab.Staff -> StaffTab(staff)
                            MediaTab.Relations -> RelationsTab(relations) { mediaId ->
                                viewModel.selectTab(MediaTab.Overview)
                            }
                            MediaTab.Stats -> StatsTab(scoreDistribution, rankings)
                            MediaTab.Social -> SocialTab(social)
                            MediaTab.Recommendations -> RecommendationsTab(recommendations)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(media: MediaDetail, onNavigateToMedia: (Long) -> Unit, onNavigateToTrailer: (String) -> Unit, onNavigateToEditList: ((Long, String) -> Unit)?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(media.title.take(1), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            if (media.englishTitle != null && media.englishTitle != media.title) {
                Text(media.englishTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }

            Row {
                listOfNotNull(media.type, media.format).forEachIndexed { idx, value ->
                    if (idx > 0) Text(" · ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                StatItem("Score", media.meanScore?.toString() ?: "-")
                StatItem("Popularity", formatNumber(media.popularity))
                StatItem("Favorites", formatNumber(media.favourites))
                StatItem("Status", media.status ?: "-")
            }

            Spacer(Modifier.height(16.dp))

            if (media.description != null) {
                Text("Synopsis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                MarkdownText(media.description, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            if (media.genres.isNotEmpty()) {
                Text("Genres", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row {
                    media.genres.take(5).forEach { genre ->
                        SuggestionChip(onClick = {}, label = { Text(genre) }, modifier = Modifier.padding(end = 4.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (media.studios.isNotEmpty()) {
                Text("Studios", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    media.studios.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))

            if (media.trailerId != null && media.trailerSite == "youtube") {
                FilledTonalButton(
                    onClick = { onNavigateToTrailer(media.trailerId) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Watch Trailer") }
                Spacer(Modifier.height(8.dp))
            }

            FilledTonalButton(
                onClick = {
                    media.siteUrl?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Open on AniList") }

            if (onNavigateToEditList != null) {
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { onNavigateToEditList(media.id, media.title) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Manage List") }
            }
        }
    }
}

@Composable
private fun CharactersTab(characters: List<MediaCharacter>) {
    if (characters.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(characters, key = { it.id }) { char ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Text(char.name.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(char.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        char.role?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffTab(staff: List<MediaStaffMember>) {
    if (staff.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(staff, key = { it.id }) { member ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Text(member.name.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        val subtitle = listOfNotNull(member.role, member.language).joinToString(" · ")
                        if (subtitle.isNotEmpty()) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelationsTab(relations: List<MediaRelation>, onNavigateToMedia: (Long) -> Unit) {
    if (relations.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(relations, key = { it.id }) { rel ->
                Row(
                    Modifier.fillMaxWidth().clickable { onNavigateToMedia(rel.id) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Text(rel.title.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(rel.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        val subtitle = listOfNotNull(rel.relationType, rel.type, rel.format).joinToString(" · ")
                        if (subtitle.isNotEmpty()) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsTab(scoreDistribution: List<ScoreDistribution>, rankings: List<Ranking>) {
    if (scoreDistribution.isEmpty() && rankings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            if (scoreDistribution.isNotEmpty()) {
                item {
                    Text("Score Distribution", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    val maxAmount = scoreDistribution.maxOfOrNull { it.amount ?: 0 } ?: 1
                    Column {
                        scoreDistribution.forEach { dist ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    (dist.score?.toString() ?: "?"),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(24.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(14.dp)
                                        .padding(horizontal = 4.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    )
                                    val fraction = (dist.amount ?: 0).toFloat() / maxAmount
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction)
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primary),
                                    )
                                }
                                Text(
                                    (dist.amount?.toString() ?: "0"),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(44.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (rankings.isNotEmpty()) {
                item {
                    Text("Rankings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                }
                items(rankings, key = { it.id ?: 0 }) { ranking ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "#${ranking.rank}" + (ranking.context?.let { " $it" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            ranking.type ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialTab(social: List<MediaSocialItem>) {
    if (social.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(social, key = { it.id }) { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Text(item.userName.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        val subtitle = buildString {
                            item.status?.let { append(it) }
                            item.progress?.let { if (isNotEmpty()) append(" · "); append(it) }
                        }
                        if (subtitle.isNotEmpty()) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            formatTimeAgo(item.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationsTab(recommendations: List<RecommendationItem>) {
    if (recommendations.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(recommendations, key = { it.id }) { rec ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Text((rec.title?.take(1) ?: "?"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(rec.title ?: "Unknown", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        val subtitle = listOfNotNull(rec.type, rec.format).joinToString(" · ")
                        if (subtitle.isNotEmpty()) {
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    rec.meanScore?.let {
                        Text("${it}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatItem(label: String, value: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatNumber(value: Int?): String {
    if (value == null) return "-"
    return when {
        value >= 1_000_000 -> "${value / 1_000_000}M"
        value >= 100_000 -> "${value / 1_000}.${(value % 1000) / 100}k"
        value >= 1_000 -> "${value / 1_000}k"
        else -> value.toString()
    }
}

private fun formatTimeAgo(timestamp: Int): String {
    val now = System.currentTimeMillis() / 1000
    val diff = now - timestamp
    return when {
        diff < 60 -> "just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        diff < 604800 -> "${diff / 86400}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp * 1000L))
        }
    }
}
