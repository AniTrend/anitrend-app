package com.mxt.anitrend.ui.media

import androidx.compose.foundation.combinedClickable
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
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/**
 * Stateless external-link grid rendering. The stats section owns link opening
 * and copy actions; this composable renders rows and emits both gestures.
 */
@Composable
internal fun LinkGrid(
    links: List<ExternalLink>,
    modifier: Modifier = Modifier,
    onLinkClick: (ExternalLink) -> Unit,
    onLinkLongClick: (ExternalLink) -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerLow)) }
    val cardElevation = dimensionResource(R.dimen.sm_margin)

    // The stats section is inside a NestedScrollView, so the lazy grid needs a finite viewport.
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_list_x2)),
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .nestedScroll(rememberNestedScrollInteropConnection()),
    ) {
        itemsIndexed(
            items = links,
            key = { index, link -> "link:$index:${link.url.orEmpty()}" },
            contentType = { _, _ -> "link" },
        ) { _, link ->
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onLinkClick(link) },
                        onLongClick = { onLinkLongClick(link) },
                    )
                    .padding(horizontal = cardElevation, vertical = cardElevation * 1.5f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            ) {
                MediaMetadataRow(
                    iconResource = R.drawable.ic_link_white_24dp,
                    label = link.site.orEmpty(),
                    modifier = Modifier.padding(dimensionResource(R.dimen.xl_margin)),
                )
            }
        }
    }
}

@Preview(name = "Media link grid", showBackground = true)
@Composable
private fun LinkGridPreview() {
    AniTrendTheme {
        LinkGrid(
            links = listOf(ExternalLink(url = "https://twitter.com", site = "Twitter")),
            onLinkClick = {},
            onLinkLongClick = {},
        )
    }
}
