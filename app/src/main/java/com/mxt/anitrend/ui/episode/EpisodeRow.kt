package com.mxt.anitrend.ui.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.KeyUtil

private const val EPISODE_IMAGE_TAG = "episode-row-image"

/** Displays an episode card with Compose overlays and preserves the legacy wide-image sizing formula. */
@Composable
internal fun EpisodeRow(
    title: String,
    duration: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.cardColor)) }
    val typography = MaterialTheme.typography
    val titleStyle =
        remember(typography) {
            typography.bodyMedium.copy(
                fontFamily = typography.titleSmall.fontFamily,
                letterSpacing = 0.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
            )
        }
    val durationStyle =
        remember(typography) {
            typography.bodyMedium.copy(
                letterSpacing = 0.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
            )
        }
    val imageHeightPixels =
        remember(context) {
            val maximumHeight = context.resources.getDimensionPixelSize(R.dimen.app_bar_height)
            val defaultMargin = context.resources.getDimensionPixelSize(R.dimen.sm_margin)
            ((maximumHeight - defaultMargin) * KeyUtil.WideAspectRatio).toInt()
        }
    val imageHeight = with(LocalDensity.current) { imageHeightPixels.toDp() }
    val gradientEnd = colorResource(R.color.colorGradientShadowEnd)
    val titleGradient =
        remember(gradientEnd) {
            Brush.verticalGradient(colors = listOf(Color.Transparent, gradientEnd))
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.sm_margin)),
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        ) {
            AniTrendImage(
                model =
                when {
                    imageUrl == null -> R.drawable.reg_bg
                    imageUrl.isBlank() -> R.drawable.ic_emoji_sweat
                    else -> imageUrl
                },
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(imageHeight).testTag(EPISODE_IMAGE_TAG),
                contentScale = ContentScale.Crop,
                crossfadeDurationMillis = 350,
            )
            Text(
                text = title,
                modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(titleGradient)
                    .padding(dimensionResource(R.dimen.lg_margin)),
                style = titleStyle,
                color = colorResource(R.color.white),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(dimensionResource(R.dimen.lg_margin))
                    .background(colorResource(R.color.black_transparent_image), CircleShape)
                    .padding(dimensionResource(R.dimen.episode_duration_padding)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = duration,
                    style = durationStyle,
                    color = colorResource(R.color.white),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.md_margin)))
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_time_white_18dp),
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview(name = "Episode row", showBackground = true, widthDp = 360)
@Composable
private fun EpisodeRowPreview() {
    val context = LocalContext.current
    val imageUrl = "android.resource://${context.packageName}/${R.drawable.reg_bg}"

    AniTrendTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.lg_margin))) {
            EpisodeRow(
                title = "Compose migration baseline episode",
                duration = "23:41",
                imageUrl = imageUrl,
                onClick = {},
                onLongClick = {},
            )
        }
    }
}
