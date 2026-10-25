package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCard
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState

/** Stateless poster grid for studio detail and favourite media sections. */
@Composable
internal fun MediaSeriesGrid(
    items: List<MediaSeriesCardUiState>,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    columnsResource: Int = R.integer.grid_giphy_x3,
    onMediaClick: (MediaSeriesCardUiState) -> Unit,
    onMediaLongClick: (MediaSeriesCardUiState) -> Unit,
    onLoadMore: () -> Unit,
) {
    LazyGridLoadMoreEffect(listState, onLoadMore)
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(columnsResource)),
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> "media:${item.id}" },
            contentType = { _, _ -> "media-series" },
        ) { _, item ->
            MediaSeriesCard(
                state = item,
                onMediaClick = { onMediaClick(item) },
                onMediaLongClick = { onMediaLongClick(item) },
            )
        }
    }
}

/** Every page, including page one, must satisfy the lazy grid's unique-id invariant. */
internal fun appendMediaSeriesPage(
    existing: List<MediaSeriesCardUiState>,
    incoming: List<MediaSeriesCardUiState>,
): List<MediaSeriesCardUiState> {
    val ids = HashSet<Long>(existing.size + incoming.size)
    val result = ArrayList<MediaSeriesCardUiState>(existing.size + incoming.size)
    existing.forEach { item ->
        ids.add(item.id)
        result.add(item)
    }
    incoming.forEach { item ->
        if (ids.add(item.id)) result.add(item)
    }
    return result
}
