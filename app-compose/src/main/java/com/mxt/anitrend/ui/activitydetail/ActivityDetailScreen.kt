package com.mxt.anitrend.ui.activitydetail

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.mxt.anitrend.data.social.ActivityDetail
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToMedia: (Long) -> Unit,
) {
    val viewModel = koinViewModel<ActivityDetailViewModel>(parameters = { parametersOf(activityId.toInt()) })
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is ActivityDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ActivityDetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ActivityDetailUiState.Success -> {
                ActivityDetailContent(
                    activity = state.activity,
                    onNavigateToMedia = onNavigateToMedia,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun ActivityDetailContent(
    activity: ActivityDetail,
    onNavigateToMedia: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = activity.userName?.take(1) ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activity.userName ?: "Unknown",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = formatTimestamp(activity.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val mainText = activity.text
                        ?: buildString {
                            if (!activity.status.isNullOrEmpty()) append(activity.status)
                            if (!activity.progress.isNullOrEmpty()) {
                                if (isNotEmpty()) append(" · ")
                                append(activity.progress)
                            }
                        }
                    if (mainText.isNotEmpty()) {
                        Text(
                            text = mainText,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    if (activity.mediaId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToMedia(activity.mediaId) },
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = activity.mediaTitle?.take(1) ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = activity.mediaTitle ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    activity.mediaType?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "${activity.replyCount} replies",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        if (activity.replies.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Replies",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            items(activity.replies, key = { it.id }) { reply ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = reply.userName?.take(1) ?: "?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reply.userName ?: "Unknown",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = formatTimestamp(reply.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reply.text ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (reply.likeCount > 0) {
                            Text(
                                text = "${reply.likeCount} like${if (reply.likeCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(unixSeconds: Int): String {
    val now = System.currentTimeMillis() / 1000
    val diff = now - unixSeconds
    return when {
        diff < 60 -> "just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        diff < 604800 -> "${diff / 86400}d ago"
        else -> "${diff / 604800}w ago"
    }
}
