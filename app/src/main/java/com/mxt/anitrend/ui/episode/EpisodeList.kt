package com.mxt.anitrend.ui.episode

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.crunchy.Episode
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Immutable row projection for the episodes list. [id] keeps identity across
 * refetched feeds because [Episode] has no stable identifier.
 */
data class EpisodeRowUiState(
    val id: String,
    val title: String,
    val duration: String,
    val imageUrl: String?,
) {
    companion object {
        fun from(index: Int, model: Episode): EpisodeRowUiState = EpisodeRowUiState(
            // Episode has no stable domain id and RSS links are not guaranteed
            // unique, so position is the honest identity fallback.
            id = "episode:$index",
            title = model.title,
            duration = formatDuration(model.content?.duration),
            imageUrl = model.thumbnail.firstOrNull()?.url,
        )

        /** Formats RSS duration seconds as `m:ss`. */
        private fun formatDuration(duration: String?): String {
            val timeSpan = duration?.toLongOrNull() ?: return "00:00"
            val minutes = TimeUnit.SECONDS.toMinutes(timeSpan)
            val seconds = timeSpan - TimeUnit.MINUTES.toSeconds(minutes)
            return String.format(Locale.getDefault(), if (seconds < 10) "%d:0%d" else "%d:%d", minutes, seconds)
        }
    }
}

/**
 * Stateless episodes list host.
 * The ViewModel owns feed loading and row projection; the fragment forwards
 * actions by position while preserving the series-image action target.
 */
@Composable
internal fun EpisodeList(
    items: List<EpisodeRowUiState>,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onEpisodeClick: (Int) -> Unit,
    onEpisodeLongClick: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.single_list_x1)),
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id },
            contentType = { _, _ -> "episode" },
        ) { index, item ->
            EpisodeRow(
                title = item.title,
                duration = item.duration,
                imageUrl = item.imageUrl,
                // The retired adapter gave every item lg_margin side margins.
                modifier = Modifier.padding(dimensionResource(R.dimen.lg_margin)),
                onClick = { onEpisodeClick(index) },
                onLongClick = { onEpisodeLongClick(index) },
            )
        }
    }
}
