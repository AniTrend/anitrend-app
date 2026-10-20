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
package com.mxt.anitrend.ui.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.format.mediaFormatLabelRes
import com.mxt.anitrend.ui.format.staffLanguageLabelRes
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.ui.media.MediaGridTile

/** Staff screen sections with complete See all surfaces. */
enum class StaffSection { ABOUT, VOICE_ACTING, CREDITS }

/**
 * Staff screen: unframed identity header, then About, Voice acting and Credits
 * previews. The staff's primary language is rendered through the shared
 * localized labels; complete paged data lives behind each See all route.
 */
@Composable
internal fun StaffScreen(
    name: String?,
    nativeName: String?,
    alternativeNames: String?,
    language: String?,
    description: String?,
    voiceActing: List<MediaEdge>,
    credits: List<MediaBase>,
    markwon: io.noties.markwon.Markwon,
    onLinkClick: (String) -> Unit,
    onSeeAll: (StaffSection) -> Unit,
    onMediaClick: (MediaBase) -> Unit,
    onCharacterClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackClick) {
                    Text(text = stringResource(R.string.action_back), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item(key = "identity") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = name ?: stringResource(R.string.label_unknown),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!nativeName.isNullOrBlank()) {
                        Text(
                            text = nativeName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!language.isNullOrBlank()) {
                        Text(
                            text = stringResource(staffLanguageLabelRes(language)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!alternativeNames.isNullOrBlank()) {
                        Text(
                            text = alternativeNames,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            item(key = "about") {
                SectionHeader(title = stringResource(R.string.anime_page_title_overview), onSeeAll = { onSeeAll(StaffSection.ABOUT) })
                MarkdownText(
                    markdown = description ?: stringResource(R.string.layout_empty_response),
                    markwon = markwon,
                    onLinkClick = onLinkClick,
                    onLinkLongClick = {},
                )
            }
            item(key = "voice-acting") {
                SectionHeader(title = stringResource(R.string.anime_page_title_characters), onSeeAll = { onSeeAll(StaffSection.VOICE_ACTING) })
                if (voiceActing.isEmpty()) {
                    EmptyPreview()
                } else {
                    voiceActing.forEach { edge ->
                        VoiceActingRow(
                            edge = edge,
                            onMediaClick = onMediaClick,
                            onCharacterClick = onCharacterClick,
                        )
                    }
                }
            }
            item(key = "credits") {
                SectionHeader(title = stringResource(R.string.drawer_title_anime), onSeeAll = { onSeeAll(StaffSection.CREDITS) })
                if (credits.isEmpty()) {
                    EmptyPreview()
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(credits, key = { "credit:${it.id}" }) { media ->
                            MediaGridTile(
                                imageUrl = media.coverImage?.large,
                                title = media.title?.userPreferred.orEmpty(),
                                subtitle = stringResource(mediaFormatLabelRes(media.format)),
                                contentDescription = media.title?.userPreferred,
                                onClick = { onMediaClick(media) },
                                onLongClick = { onMediaClick(media) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceActingRow(
    edge: MediaEdge,
    onMediaClick: (MediaBase) -> Unit,
    onCharacterClick: (Long) -> Unit,
) {
    val media = edge.node
    val characters = edge.characters.orEmpty()
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = media?.title?.userPreferred ?: stringResource(R.string.label_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            characters.forEach { character ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = character.name?.fullName ?: character.name?.original ?: stringResource(R.string.label_unknown),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (character.id != 0L) {
                        TextButton(onClick = { onCharacterClick(character.id) }) {
                            Text(text = stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            if (media != null) {
                TextButton(onClick = { onMediaClick(media) }) {
                    Text(text = stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(onClick = onSeeAll) {
            Text(text = stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun EmptyPreview() {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.layout_empty_response),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
