package com.mxt.anitrend.ui.giphy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.LazyGridLoadMoreEffect

@Composable
internal fun GiphyGrid(
    items: List<Giphy>,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onSelect: (Giphy) -> Unit,
    onPreview: (Giphy) -> Unit,
    onLoadMore: () -> Unit,
) {
    LazyGridLoadMoreEffect(listState, onLoadMore)
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_giphy_x3)),
        state = listState,
        modifier = modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = items,
            // The nullable legacy DTO has no stable identity when id is absent.
            key = { index, item -> item.id?.let { "giphy:$it" } ?: "unidentified:$index" },
            contentType = { _, _ -> "giphy" },
        ) { _, item ->
            GiphyResultRow(giphy = item, onClick = { onSelect(item) }, onLongClick = { onPreview(item) })
        }
    }
}

@Preview(name = "GIF picker", widthDp = 400, heightDp = 500)
@Composable
private fun GiphyGridPreview() {
    AniTrendTheme {
        GiphyGrid(
            items = listOf(Giphy(id = "preview", title = "GIF preview")),
            onSelect = {},
            onPreview = {},
            onLoadMore = {},
        )
    }
}
