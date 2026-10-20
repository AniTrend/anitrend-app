package com.mxt.anitrend.ui.giphy

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.giphy.Gif
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.KeyUtil

private const val GIPHY_BADGE_TAG_PREFIX = "giphy-result-badge"

@Composable
internal fun GiphyResultRow(
    giphy: Giphy,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val image = giphy.images[KeyUtil.GIPHY_PREVIEW] ?: giphy.images[KeyUtil.GIPHY_ORIGINAL_ANIMATED]
    val imageUrl = image?.url
    val description = giphy.title?.takeIf(String::isNotBlank) ?: stringResource(R.string.title_giphy_preview)

    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.md_margin))
            .aspectRatio(4f / 3.3f)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            }
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        AniTrendImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            crossfadeDurationMillis = 250,
        )
        Box(
            modifier =
            Modifier
                .align(Alignment.TopEnd)
                .padding(dimensionResource(R.dimen.lg_margin))
                .testTag("$GIPHY_BADGE_TAG_PREFIX-${giphy.id}")
                .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(2.dp))
                .padding(4.dp),
        ) {
            Text(
                text = "GIPHY",
                modifier = Modifier.clearAndSetSemantics {},
                color = MaterialTheme.colorScheme.surface,
                style =
                TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    lineHeight = 8.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
            )
        }
    }
}

@Preview(name = "Giphy result", showBackground = true, widthDp = 240)
@Composable
private fun GiphyResultRowPreview() {
    val context = LocalContext.current
    val imageUrl = "android.resource://${context.packageName}/${R.drawable.reg_bg}"

    AniTrendTheme("light") {
        GiphyResultRow(
            giphy =
            Giphy(
                id = "preview",
                title = "Dancing cat GIF",
                images =
                hashMapOf(
                    KeyUtil.GIPHY_PREVIEW to Gif(url = imageUrl, width = "4", height = "3"),
                ),
            ),
            onClick = {},
            onLongClick = {},
        )
    }
}
