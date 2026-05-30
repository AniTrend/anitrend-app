package com.mxt.anitrend.ui.character

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.data.character.CharacterDetail
import com.mxt.anitrend.plugin.text.MarkdownText
import com.mxt.anitrend.ui.widget.FavouriteButton
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    characterId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToMedia: (Long) -> Unit,
    onToggleFavourite: () -> Unit = {},
) {
    val viewModel = koinViewModel<CharacterDetailViewModel>(parameters = { parametersOf(characterId.toInt()) })
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state is CharacterDetailUiState.Success) Text((state as CharacterDetailUiState.Success).character.name)
                    else Text("Character")
                },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    if (state is CharacterDetailUiState.Success) {
                        FavouriteButton(
                            isFavourite = (state as CharacterDetailUiState.Success).character.isFavourite,
                            onToggle = onToggleFavourite,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            is CharacterDetailUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is CharacterDetailUiState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(s.message, color = MaterialTheme.colorScheme.error) }
            is CharacterDetailUiState.Success -> CharacterContent(s.character, onNavigateToMedia, Modifier.padding(padding))
        }
    }
}

@Composable
private fun CharacterContent(char: CharacterDetail, onNavigateToMedia: (Long) -> Unit, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Box(Modifier.fillMaxWidth().height(160.dp).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text(char.name.take(1).uppercase(), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(16.dp))
            Text(char.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (char.description != null) {
                Spacer(Modifier.height(8.dp))
                MarkdownText(char.description, Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(16.dp))
            Text("Appears in", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        items(char.media, key = { it.id }) { media ->
            Row(Modifier.fillMaxWidth().clickable { onNavigateToMedia(media.id) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(media.title.take(1), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(media.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    listOfNotNull(media.type, media.format).joinToString(" · ").takeIf { it.isNotEmpty() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
