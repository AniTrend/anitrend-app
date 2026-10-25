package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/**
 * Identity-keyed batch of latest-media models for one emission. Callers build a
 * fresh batch per emission: identity equality means skippable recomposition can
 * never treat a batch carrying same-id in-place model mutations as unchanged, so
 * every rendered field reflects the model's current values.
 */
internal class MediaItems(val value: List<MediaBase>)

/**
 * Stateless latest-media list rendering. The host fragment owns paging, refresh,
 * and navigation; this composable renders keyed rows and emits user actions.
 */
@Composable
internal fun LatestMediaList(
    items: MediaItems,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onMediaClick: (MediaBase) -> Unit,
    onMediaLongClick: (MediaBase) -> Unit,
    onLoadMore: () -> Unit,
) {
    val itemSpacing = dimensionResource(R.dimen.lg_margin)

    LazyGridLoadMoreEffect(gridState = listState, onLoadMore = onLoadMore)

    // Column count follows the legacy `single_list_x1` span resource (1 column
    // normally, 2 on large-landscape, 3 on xlarge-landscape) for layout parity.
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.single_list_x1)),
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        itemsIndexed(
            items = items.value,
            key = { _, media -> "media:${media.id}" },
            contentType = { _, _ -> "media" },
        ) { _, media ->
            val context = LocalContext.current
            val rowState = LatestMediaRowUiState.from(context, media)
            Box(
                modifier =
                Modifier.padding(
                    start = itemSpacing,
                    end = itemSpacing,
                    top = itemSpacing,
                ),
            ) {
                MediaLatestRow(
                    state = rowState,
                    onClick = { onMediaClick(media) },
                    onLongClick = { onMediaLongClick(media) },
                )
            }
        }
    }
}

@Preview(name = "Latest media list", showBackground = true)
@Composable
private fun LatestMediaListPreview() {
    AniTrendTheme {
        LatestMediaList(
            items = MediaItems(emptyList()),
            onMediaClick = {},
            onMediaLongClick = {},
            onLoadMore = {},
        )
    }
}
