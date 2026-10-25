package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/** Immutable studio row projection; [id] drives navigation actions. */
internal data class StudioRowUiState(
    val id: Long,
    val name: String,
    val isFavourite: Boolean,
) {
    companion object {
        fun from(model: StudioBase): StudioRowUiState = StudioRowUiState(
            id = model.id,
            name = model.name.orEmpty(),
            isFavourite = model.isFavourite,
        )
    }
}

/**
 * Stateless studio grid for the search and favourites studio tabs. The host
 * fragment owns paging and state; this composable renders rows and emits clicks.
 */
@Composable
internal fun StudioGrid(
    items: List<StudioRowUiState>,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    onStudioClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerLow)) }
    val cardElevation = dimensionResource(R.dimen.sm_margin)

    LazyGridLoadMoreEffect(gridState, onLoadMore)
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_list_x2)),
        modifier = modifier.fillMaxSize(),
        state = gridState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> "studio:${item.id}" },
            contentType = { _, _ -> "studio" },
        ) { _, item ->
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = cardElevation, vertical = cardElevation * 1.5f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            ) {
                StudioRow(
                    studioName = item.name,
                    isFavourite = item.isFavourite,
                    modifier = Modifier.padding(dimensionResource(R.dimen.xl_margin)),
                    onClick = { onStudioClick(item.id) },
                )
            }
        }
    }
}

@Preview(name = "Studio grid", showBackground = true)
@Composable
private fun StudioGridPreview() {
    AniTrendTheme {
        StudioGrid(
            items = listOf(
                StudioRowUiState(id = 1L, name = "Studio One", isFavourite = true),
                StudioRowUiState(id = 2L, name = "Studio Two", isFavourite = false),
            ),
            onStudioClick = {},
            onLoadMore = {},
        )
    }
}
