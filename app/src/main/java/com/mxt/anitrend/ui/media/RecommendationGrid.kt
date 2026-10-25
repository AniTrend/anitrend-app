package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.paging.LoadStateFooter
import com.mxt.anitrend.ui.paging.LoadStateFooterUiState
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCard
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState

@Composable
internal fun RecommendationGrid(
    items: LazyPagingItems<RecommendationItemUiModel>,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onOpenMedia: (RecommendationItemUiModel) -> Unit,
    onLongPressMedia: (RecommendationItemUiModel) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_giphy_x3)),
        state = listState,
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            count = items.itemCount,
            key = { index -> items.peek(index)?.let { "recommendation:${it.id}" } ?: "placeholder:$index" },
            contentType = { "media-series" },
        ) { index ->
            items[index]?.let { item ->
                MediaSeriesCard(
                    state = MediaSeriesCardUiState.from(LocalContext.current, item),
                    onMediaClick = { onOpenMedia(item) },
                    onMediaLongClick = { onLongPressMedia(item) },
                )
            }
        }
        val append = items.loadState.append
        if (append is LoadState.Loading || append is LoadState.Error) {
            item(key = "recommendation-load-state") {
                LoadStateFooter(LoadStateFooterUiState.from(append), onRetry = items::retry)
            }
        }
    }
}
