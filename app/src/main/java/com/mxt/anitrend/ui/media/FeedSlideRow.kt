package com.mxt.anitrend.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.KeyUtil

private const val FEED_SLIDE_IMAGE_TAG = "feed-slide-image"
private const val FEED_SLIDE_IMAGE_CONTENT_TAG = "feed-slide-image-content"
private const val FEED_SLIDE_PLAY_TAG = "feed-slide-playback"

@Composable
internal fun FeedSlideRow(
    imageUrl: String?,
    centerCrop: Boolean,
    showPlayback: Boolean,
    roundedCorners: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val roundedCornerPixels =
        remember(context, roundedCorners) {
            if (roundedCorners) context.resources.getDimensionPixelSize(R.dimen.md_margin) else 0
        }
    val imageHeightPixels =
        remember(context) {
            val maximumHeight = context.resources.getDimensionPixelSize(R.dimen.app_bar_height)
            val defaultMargin = context.resources.getDimensionPixelSize(R.dimen.sm_margin)
            ((maximumHeight - defaultMargin) * KeyUtil.WideAspectRatio).toInt()
        }
    val imageHeight = with(density) { imageHeightPixels.toDp() }
    val roundedCornerSize = with(density) { roundedCornerPixels.toDp() }
    val roundedShape = remember(roundedCornerSize) { RoundedCornerShape(roundedCornerSize) }
    val imageModifier =
        Modifier
            .fillMaxWidth()
            .height(imageHeight)
            .then(
                if (roundedCornerPixels > 0) {
                    Modifier.clip(roundedShape)
                } else {
                    Modifier
                },
            )
            .testTag(FEED_SLIDE_IMAGE_CONTENT_TAG)

    Box(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).testTag(FEED_SLIDE_IMAGE_TAG),
    ) {
        Box(modifier = imageModifier) {
            AniTrendImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = if (centerCrop) ContentScale.Crop else ContentScale.Inside,
                crossfadeDurationMillis = 250,
                painterContentShape = if (roundedCornerPixels > 0) roundedShape else null,
            )
        }
        if (showPlayback) {
            Box(
                modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(dimensionResource(R.dimen.feed_slide_playback_size))
                    .background(colorResource(R.color.black_transparent_image), CircleShape)
                    .padding(dimensionResource(R.dimen.xl_margin))
                    .testTag(FEED_SLIDE_PLAY_TAG),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_play_circle_outline_white_24dp),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview(name = "Feed slide playback", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FeedSlideRowPreview() {
    val context = LocalContext.current
    val imageUrl = "android.resource://${context.packageName}/${R.drawable.reg_bg}"

    AniTrendTheme {
        FeedSlideRow(
            imageUrl = imageUrl,
            centerCrop = true,
            showPlayback = true,
            roundedCorners = false,
            onClick = {},
        )
    }
}
