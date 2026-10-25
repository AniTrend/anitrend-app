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
package com.mxt.anitrend.ui.favourite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.media.PagedMediaGridScreen
import com.mxt.anitrend.ui.people.PagedPeopleScreen

/** Favourites entity types; in-screen presets, never navigation destinations. */
enum class FavouriteType { ANIME, MANGA, CHARACTERS, STAFF }

/**
 * Shared Favourites screen: one renderer with entity-type chips. Anime and
 * Manga favourites render as a paged grid; Characters and Staff as a paged
 * slot-based list. Chip switches are in-screen only and never add navigation
 * entries.
 */
@Composable
internal fun FavouritesScreen(
    media: List<MediaBase>,
    peopleCount: Int,
    isLoading: Boolean,
    errorMessage: String?,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onMediaClick: (MediaBase) -> Unit,
    onMediaLongClick: (MediaBase) -> Unit,
    onBackClick: () -> Unit,
    peopleRow: @Composable (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedType by rememberSaveable { mutableStateOf(FavouriteType.ANIME.name) }
    val type = FavouriteType.valueOf(selectedType)

    val typeChips: @Composable () -> Unit = {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chips =
                listOf(
                    FavouriteType.ANIME to R.string.label_media_type_anime,
                    FavouriteType.MANGA to R.string.label_media_type_manga,
                    FavouriteType.CHARACTERS to R.string.anime_page_title_characters,
                    FavouriteType.STAFF to R.string.anime_page_title_staff,
                )
            items(chips.size) { index ->
                val (chipType, labelRes) = chips[index]
                FilterChip(
                    selected = type == chipType,
                    onClick = { selectedType = chipType.name },
                    label = { Text(stringResource(labelRes)) },
                )
            }
        }
    }

    if (type == FavouriteType.CHARACTERS || type == FavouriteType.STAFF) {
        PagedPeopleScreen(
            itemCount = peopleCount,
            isLoading = isLoading,
            errorMessage = errorMessage,
            canLoadMore = canLoadMore,
            onLoadMore = onLoadMore,
            onRetry = onRetry,
            onBackClick = onBackClick,
            rowContent = peopleRow,
            modifier = modifier,
            header = typeChips,
        )
    } else {
        PagedMediaGridScreen(
            media = media,
            isLoading = isLoading,
            errorMessage = errorMessage,
            canLoadMore = canLoadMore,
            onMediaClick = onMediaClick,
            onMediaLongClick = onMediaLongClick,
            onLoadMore = onLoadMore,
            onRetry = onRetry,
            onBackClick = onBackClick,
            modifier = modifier,
            header = typeChips,
        )
    }
}
