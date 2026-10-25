package com.mxt.anitrend.ui.media

import androidx.appcompat.R as AppCompatR
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.widget.TextViewCompat
import com.mxt.anitrend.R as AppR
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.binding.htmlText
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil

private const val RANK_CARD_TAG = "rank-row-card"
private const val RATED_ICON_TAG = "rank-row-rated-icon"
private const val POPULAR_ICON_TAG = "rank-row-popular-icon"

@Composable
internal fun RankRow(
    rank: MediaRank,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onTextClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val isRated = rank.type == KeyUtil.RATED
    val htmlText = rank.typeHtml
    val iconResource = if (isRated) AppR.drawable.ic_star_yellow_700_24dp else AppR.drawable.ic_favorite_red_700_24dp
    val iconTag = if (isRated) RATED_ICON_TAG else POPULAR_ICON_TAG
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(AppR.attr.colorSurfaceContainerLow)) }
    val cardElevation = dimensionResource(AppR.dimen.sm_margin)
    // Match CardViewBase's shadow inset: elevation horizontally and 1.5x vertically.
    val isPreview = LocalInspectionMode.current

    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = cardElevation, vertical = cardElevation * 1.5f)
            .testTag(RANK_CARD_TAG),
        shape = RoundedCornerShape(dimensionResource(AppR.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(AppR.dimen.xl_margin)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(iconResource),
                contentDescription = null,
                modifier =
                Modifier
                    .size(dimensionResource(AppR.dimen.menu_item_icon_size))
                    .testTag(iconTag),
            )
            Spacer(modifier = Modifier.width(dimensionResource(AppR.dimen.xl_margin)))
            AndroidView(
                factory = { context ->
                    RichMarkdownTextView(context).apply {
                        id = AppR.id.sub_container
                        isClickable = true
                        isFocusable = true
                        TextViewCompat.setTextAppearance(
                            this,
                            AppCompatR.style.Base_TextAppearance_AppCompat_Caption,
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                update = { textView ->
                    if (textView.tag != htmlText) {
                        textView.tag = htmlText
                        if (isPreview) {
                            textView.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        } else {
                            textView.htmlText(htmlText)
                        }
                    }
                    textView.setOnClickListener { onTextClick() }
                },
            )
        }
    }
}

@Composable
internal fun RankingsGrid(
    rankings: List<MediaRank>,
    modifier: Modifier = Modifier,
    onRankClick: (MediaRank) -> Unit,
    onRankLongClick: (MediaRank) -> Unit,
) {
    // The stats host is inside a NestedScrollView, so the lazy grid needs a finite viewport.
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(AppR.integer.grid_list_x2)),
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .nestedScroll(rememberNestedScrollInteropConnection()),
    ) {
        items(
            items = rankings,
            key = { "${it.type}:${it.rank}:${it.context}:${it.format}:${it.year}:${it.season}" },
            contentType = { it.type },
        ) { rank ->
            RankRow(
                rank = rank,
                onClick = { onRankClick(rank) },
                onTextClick = { onRankClick(rank) },
                onLongClick = { onRankLongClick(rank) },
            )
        }
    }
}

@Preview(name = "Ranking rows", showBackground = true, widthDp = 360)
@Composable
private fun RankRowPreview() {
    AniTrendTheme {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(AppR.dimen.lg_margin)),
        ) {
            RankRow(
                rank =
                MediaRank(
                    rank = 64,
                    type = KeyUtil.RATED,
                    format = KeyUtil.MANGA,
                    year = 2015,
                    season = KeyUtil.SUMMER,
                    context = "Top Rank",
                ),
                onClick = {},
                onTextClick = {},
                onLongClick = {},
            )
        }
    }
}
