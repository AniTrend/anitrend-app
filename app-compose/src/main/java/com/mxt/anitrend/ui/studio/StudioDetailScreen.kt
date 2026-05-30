package com.mxt.anitrend.ui.studio

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.ui.widget.FavouriteButton
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioDetailScreen(
    studioId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToMedia: (Long) -> Unit,
    onToggleFavourite: () -> Unit = {},
) {
    val vm = koinViewModel<StudioDetailViewModel>(parameters = { parametersOf(studioId.toInt()) })
    val state by vm.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state is StudioDetailUiState.Success) (state as StudioDetailUiState.Success).studio.name else "Studio") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    if (state is StudioDetailUiState.Success) {
                        FavouriteButton(
                            isFavourite = (state as StudioDetailUiState.Success).studio.isFavourite,
                            onToggle = onToggleFavourite,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            is StudioDetailUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is StudioDetailUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(s.message, color = MaterialTheme.colorScheme.error) }
            is StudioDetailUiState.Success -> StudioContent(s.studio, onNavigateToMedia, Modifier.padding(padding))
        }
    }
}

@Composable
private fun StudioContent(studio: com.mxt.anitrend.data.studio.StudioDetail, onNavigateToMedia: (Long) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(160.dp).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text(studio.name.take(1).uppercase(), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(16.dp))
            Text(studio.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (studio.isAnimationStudio) Text("Animation Studio", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("Works on", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        items(studio.media, key = { it.id }) { media ->
            Row(Modifier.fillMaxWidth().clickable { onNavigateToMedia(media.id) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { Text(media.title.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(media.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    listOfNotNull(media.type, media.format).joinToString(" · ").takeIf { it.isNotEmpty() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}
