package com.mxt.anitrend.ui.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.anilist.Genre

@Composable
internal fun GenreGrid(
    genres: List<Genre>,
    modifier: Modifier = Modifier,
    onOpenGenre: (String) -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerLow)) }
    val cardElevation = dimensionResource(R.dimen.sm_margin)

    // The overview is inside a NestedScrollView, so the lazy grid needs a finite viewport.
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_list_x2)),
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .nestedScroll(rememberNestedScrollInteropConnection()),
    ) {
        itemsIndexed(
            items = genres,
            key = { index, genre -> "genre:$index:${genre.genre.orEmpty()}" },
            contentType = { _, _ -> "genre" },
        ) { _, genre ->
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { genre.genre?.let(onOpenGenre) }
                    .padding(horizontal = cardElevation, vertical = cardElevation * 1.5f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            ) {
                MediaMetadataRow(
                    iconResource = R.drawable.ic_label_outline_white_24dp,
                    label = genre.genre.orEmpty(),
                    modifier = Modifier.padding(dimensionResource(R.dimen.xl_margin)),
                )
            }
        }
    }
}
