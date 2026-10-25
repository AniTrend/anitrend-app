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
package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.format.mediaFormatLabelRes
import com.mxt.anitrend.ui.format.mediaSeasonLabelRes
import com.mxt.anitrend.ui.format.mediaSourceLabelRes
import com.mxt.anitrend.ui.format.mediaStatusLabelRes
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.view.fragment.detail.MediaSection
import com.mxt.anitrend.viewmodel.MediaOverviewViewModel

/**
 * The unified media detail screen: hero identity first, then summary, structured
 * metadata and one in-content heading per [MediaSection] with a See all route to
 * the complete paged surface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaDetailScreen(
    mediaId: Long,
    title: String?,
    bannerImage: String?,
    coverImage: String?,
    isFavourite: Boolean,
    isOnList: Boolean,
    overview: MediaOverviewRecord?,
    display: MediaOverviewViewModel.MediaOverviewDisplayData?,
    synopsis: String?,
    isAuthenticated: Boolean,
    markwon: io.noties.markwon.Markwon,
    onLinkClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onManageClick: () -> Unit,
    onSeeAll: (MediaSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            MediaDetailBottomBar(
                isFavourite = isFavourite,
                isOnList = isOnList,
                onBackClick = onBackClick,
                onFavouriteClick = onFavouriteClick,
                onShareClick = onShareClick,
                onManageClick = onManageClick,
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item(key = "hero") {
                MediaHero(
                    title = title ?: overview?.titleUserPreferred.orEmpty(),
                    bannerImage = bannerImage ?: overview?.bannerImage,
                    coverImage = coverImage ?: overview?.coverImage?.large,
                    format = overview?.format,
                    status = overview?.status,
                    isAdult = overview?.isAdult,
                )
            }
            item(key = "score") {
                val score = overview?.meanScore ?: display?.meanScore
                if (score != null && score > 0) {
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            item(key = "synopsis") {
                if (!synopsis.isNullOrBlank()) {
                    MarkdownText(
                        markdown = synopsis,
                        markwon = markwon,
                        onLinkClick = onLinkClick,
                        onLinkLongClick = {},
                    )
                }
            }
            item(key = "metadata") {
                MediaMetadataGrid(
                    format = overview?.format,
                    status = overview?.status,
                    season = overview?.season,
                    source = display?.sourceText,
                    sourceRaw = null,
                    episodeCount = display?.episodeCount ?: overview?.episodes,
                    episodeDuration = display?.episodeDuration,
                    volumeCount = display?.volumeCount ?: overview?.volumes,
                    chapterCount = display?.chapterCount ?: overview?.chapters,
                    isAnime = display?.isAnime ?: true,
                )
            }
            if (!display?.genres.isNullOrEmpty()) {
                item(key = "genres") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(display.genres.mapNotNull { it.genre }, key = { "genre:$it" }) { genre ->
                            Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                                Text(
                                    text = genre,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            items(
                MediaSection.visibleSections(isAuthenticated).filter { it != MediaSection.OVERVIEW },
                key = { "section-${it.name}" },
            ) { section ->
                MediaSectionHeader(
                    section = section,
                    onSeeAll = { onSeeAll(section) },
                )
            }
        }
    }
}

@Composable
private fun MediaHero(
    title: String,
    bannerImage: String?,
    coverImage: String?,
    format: String?,
    status: String?,
    isAdult: Boolean?,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AniTrendImage(
            model = bannerImage,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.medium),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AniTrendImage(
                model = coverImage,
                contentDescription = title,
                modifier = Modifier.width(96.dp).aspectRatio(0.7f).clip(MaterialTheme.shapes.medium),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta =
                    buildList {
                        add(stringResource(mediaFormatLabelRes(format)))
                        add(stringResource(mediaStatusLabelRes(status)))
                        if (isAdult == true) add(stringResource(R.string.pref_key_display_adult_content))
                    }.joinToString(" · ")
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MediaMetadataGrid(
    format: String?,
    status: String?,
    season: String?,
    source: String?,
    sourceRaw: String?,
    episodeCount: Int?,
    episodeDuration: Int?,
    volumeCount: Int?,
    chapterCount: Int?,
    isAnime: Boolean,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetadataRow(label = stringResource(R.string.metadata_format), value = stringResource(mediaFormatLabelRes(format)))
            MetadataRow(label = stringResource(R.string.metadata_status), value = stringResource(mediaStatusLabelRes(status)))
            if (season != null) {
                MetadataRow(label = stringResource(R.string.metadata_season), value = stringResource(mediaSeasonLabelRes(season)))
            }
            if (sourceRaw != null) {
                MetadataRow(label = stringResource(R.string.metadata_source), value = stringResource(mediaSourceLabelRes(sourceRaw)))
            } else if (!source.isNullOrBlank()) {
                MetadataRow(label = stringResource(R.string.metadata_source), value = source)
            }
            if (isAnime) {
                if (episodeCount != null) {
                    MetadataRow(label = stringResource(R.string.metadata_episodes), value = episodeCount.toString())
                }
                if (episodeDuration != null) {
                    MetadataRow(label = stringResource(R.string.metadata_duration), value = episodeDuration.toString())
                }
            } else {
                if (volumeCount != null) {
                    MetadataRow(label = stringResource(R.string.metadata_volumes), value = volumeCount.toString())
                }
                if (chapterCount != null) {
                    MetadataRow(label = stringResource(R.string.metadata_chapters), value = chapterCount.toString())
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MediaSectionHeader(
    section: MediaSection,
    onSeeAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(section.titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(onClick = onSeeAll) {
            Text(text = stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun MediaDetailBottomBar(
    isFavourite: Boolean,
    isOnList: Boolean,
    onBackClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onManageClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onBackClick) {
            Icon(painter = painterResource(R.drawable.ic_arrow_back_white_24dp), contentDescription = stringResource(R.string.action_back))
        }
        IconButton(onClick = onFavouriteClick) {
            Icon(
                painter = painterResource(if (isFavourite) R.drawable.ic_favorite_white_24dp else R.drawable.ic_favorite_border_white_24dp),
                contentDescription = stringResource(if (isFavourite) R.string.description_favourite_action_remove else R.string.description_favourite_action_add),
            )
        }
        IconButton(onClick = onShareClick) {
            Icon(painter = painterResource(R.drawable.ic_share_grey_600_24dp), contentDescription = stringResource(R.string.drawer_title_share))
        }
        TextButton(onClick = onManageClick) {
            Text(
                text = stringResource(if (isOnList) R.string.nav_header_manage else R.string.action_add_to_list),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
