package com.mxt.anitrend.ui.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/**
 * Stateless tag grid rendering. The overview section owns the tag dialog and
 * navigation; this composable only renders rows and emits clicks.
 */
@Composable
internal fun TagGrid(
    tags: List<MediaTag>,
    modifier: Modifier = Modifier,
    onTagClick: (MediaTag) -> Unit,
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
            items = tags,
            key = { index, tag -> "tag:$index:${tag.name.orEmpty()}" },
            contentType = { _, _ -> "tag" },
        ) { _, tag ->
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onTagClick(tag) }
                    .padding(horizontal = cardElevation, vertical = cardElevation * 1.5f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            ) {
                MediaTagRow(
                    state =
                    MediaTagRowState(
                        name = tag.name.orEmpty(),
                        rankLabel = String.format("%d%%", tag.rank),
                        isSpoiler = tag.isMediaSpoiler,
                    ),
                    modifier = Modifier.padding(dimensionResource(R.dimen.xl_margin)),
                )
            }
        }
    }
}

@Preview(name = "Media tag grid", showBackground = true)
@Composable
private fun TagGridPreview() {
    AniTrendTheme {
        TagGrid(
            tags =
            listOf(
                MediaTag(name = "High School", rank = 80),
                MediaTag(name = "Mystery Story", rank = 45, isMediaSpoiler = true),
            ),
            onTagClick = {},
        )
    }
}
