package com.mxt.anitrend.ui.staff

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
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.LazyGridLoadMoreEffect

/**
 * Stateless staff grid for the search and favourites staff tabs. The host
 * fragment owns paging and state; this composable renders rows and emits clicks.
 */
@Composable
internal fun StaffGrid(
    items: List<StaffRowUiState>,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    onStaffClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerLow)) }
    val cardElevation = dimensionResource(R.dimen.sm_margin)

    LazyGridLoadMoreEffect(gridState, onLoadMore)
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_giphy_x3)),
        modifier = modifier.fillMaxSize(),
        state = gridState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> "staff:${item.id}" },
            contentType = { _, _ -> "staff" },
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
                StaffRow(
                    state = item,
                    modifier = Modifier.padding(dimensionResource(R.dimen.xl_margin)),
                    onClick = { onStaffClick(item.id) },
                )
            }
        }
    }
}

@Preview(name = "Staff grid", showBackground = true)
@Composable
private fun StaffGridPreview() {
    AniTrendTheme {
        StaffGrid(
            items = listOf(
                StaffRowUiState(id = 1L, name = "Staff One", image = null, language = "Japanese", isFavourite = true),
                StaffRowUiState(id = 2L, name = "Staff Two", image = null, language = null, isFavourite = false),
            ),
            onStaffClick = {},
            onLoadMore = {},
        )
    }
}
