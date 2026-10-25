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
package com.mxt.anitrend.ui.studio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.media.PagedMediaGridScreen

/**
 * Studio screen: studio identity header over the shared paged media grid
 * (the studio's media credits).
 */
@Composable
internal fun StudioScreen(
    studioName: String,
    studioFavourites: Int,
    isFavourite: Boolean,
    media: List<MediaBase>,
    isLoading: Boolean,
    errorMessage: String?,
    canLoadMore: Boolean,
    onFavouriteToggle: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onMediaClick: (MediaBase) -> Unit,
    onMediaLongClick: (MediaBase) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        header = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = studioName.ifBlank { stringResource(R.string.label_unknown) },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (studioFavourites > 0) {
                        Text(
                            text = studioFavourites.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(onClick = onFavouriteToggle) {
                    Text(
                        text = stringResource(
                            if (isFavourite) R.string.description_favourite_action_remove else R.string.description_favourite_action_add,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    )
}
