package com.mxt.anitrend.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R as AppR
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.anilist.MediaRank
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.util.KeyUtil
import io.noties.markwon.Markwon

private const val RANK_CARD_TAG = "rank-row-card"
private const val RATED_ICON_TAG = "rank-row-rated-icon"
private const val POPULAR_ICON_TAG = "rank-row-popular-icon"

@Composable
internal fun RankRow(
    rank: MediaRank,
    markwon: Markwon,
    onLinkClick: (String) -> Unit,
    onLinkLongClick: (String) -> Unit = {},
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
            MarkdownText(
                markdown = htmlText,
                markwon = markwon,
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
                modifier = Modifier.weight(1f),
                onViewClick = onTextClick,
            )
        }
    }
}

@Preview(name = "Ranking rows", showBackground = true, widthDp = 360)
@Composable
private fun RankRowPreview() {
    AniTrendTheme("light") {
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
                markwon = createMarkwon(LocalContext.current) {},
                onLinkClick = {},
            )
        }
    }
}
