package com.mxt.anitrend.ui.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.AiringTextView
import com.mxt.anitrend.base.custom.view.text.RatingTextView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.KeyUtil

internal const val LATEST_MEDIA_ROW_TAG = "latest-media-row"
internal const val LATEST_MEDIA_IMAGE_TAG = "latest-media-image"
internal const val LATEST_MEDIA_STATUS_TAG = "latest-media-status"
internal const val LATEST_MEDIA_RATING_TAG = "latest-media-rating"
internal const val LATEST_MEDIA_AIRING_TAG = "latest-media-airing"
internal const val LATEST_MEDIA_YEAR_TYPE_TAG = "latest-media-year-type"

internal data class LatestMediaRowUiState(
    val media: MediaBase,
    val title: String,
    val statusColorResource: Int,
    val airingText: String,
    val yearTypeText: String?,
) {
    companion object {
        fun from(
            context: android.content.Context,
            media: MediaBase,
        ): LatestMediaRowUiState {
            val isAnime = media.type == KeyUtil.ANIME
            return LatestMediaRowUiState(
                media = media,
                title = media.title?.userPreferred.orEmpty(),
                statusColorResource = SeriesStatusWidget.statusColorResource(media.status),
                airingText =
                if (isAnime) {
                    AiringTextView.formatAiringText(media).orEmpty()
                } else {
                    context.getString(R.string.label_latest_release)
                },
                yearTypeText = SeriesYearTypeTextView.formatText(context, media),
            )
        }
    }
}

@Composable
internal fun MediaLatestRow(
    state: LatestMediaRowUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val isPreview = LocalInspectionMode.current
    val imageUrl = state.media.coverImage?.let { it.extraLarge ?: it.large }
    val typography = MaterialTheme.typography
    val titleStyle =
        remember(typography) {
            typography.titleMedium.copy(
                fontFamily = typography.bodyMedium.fontFamily,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
            )
        }
    val airingStyle =
        remember(typography) {
            typography.labelMedium.copy(
                fontFamily = typography.bodyMedium.fontFamily,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
            )
        }
    val yearTypeStyle =
        remember(typography) {
            typography.bodyMedium.copy(platformStyle = PlatformTextStyle(includeFontPadding = true))
        }

    Surface(
        modifier =
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ).testTag(LATEST_MEDIA_ROW_TAG),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(colorResource(state.statusColorResource))
                    .testTag(LATEST_MEDIA_STATUS_TAG),
            )
            Box(
                modifier =
                Modifier
                    .width(104.dp)
                    .aspectRatio(1f / KeyUtil.AspectRatio)
                    .testTag(LATEST_MEDIA_IMAGE_TAG),
            ) {
                if (isPreview) {
                    if (!imageUrl.isNullOrBlank()) {
                        Image(
                            painter = painterResource(R.drawable.reg_bg),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                } else {
                    AniTrendImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Column(
                modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(dimensionResource(R.dimen.spacing_xs)),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = state.title,
                        modifier = Modifier.weight(1f),
                        style = titleStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isPreview) {
                        LatestMediaRatingPreview(
                            media = state.media,
                            modifier = Modifier.padding(start = dimensionResource(R.dimen.lg_margin)),
                        )
                    } else {
                        AndroidView(
                            factory = { ratingContext -> RatingTextView(ratingContext).apply { id = R.id.custom_rating_widget } },
                            modifier =
                            Modifier
                                .padding(start = dimensionResource(R.dimen.lg_margin))
                                .testTag(LATEST_MEDIA_RATING_TAG),
                            update = { ratingView -> ratingView.setAverageRating(state.media) },
                            onRelease = { ratingView -> ratingView.onViewRecycled() },
                        )
                    }
                }
                Text(
                    text = state.airingText,
                    modifier =
                    Modifier
                        .padding(top = dimensionResource(R.dimen.lg_margin))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(
                            horizontal = dimensionResource(R.dimen.lg_margin),
                            vertical = dimensionResource(R.dimen.md_margin),
                        )
                        .testTag(LATEST_MEDIA_AIRING_TAG),
                    style = airingStyle,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                state.yearTypeText?.let { yearType ->
                    Text(
                        text = yearType,
                        modifier =
                        Modifier
                            .padding(top = dimensionResource(R.dimen.lg_margin))
                            .testTag(LATEST_MEDIA_YEAR_TYPE_TAG),
                        style = yearTypeStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LatestMediaRatingPreview(
    media: MediaBase,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
        modifier
            .background(colorResource(R.color.colorTransparent), RoundedCornerShape(50.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag(LATEST_MEDIA_RATING_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = media.averageScore.toString(),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.md_margin)))
        Icon(
            painter = painterResource(R.drawable.ic_star_grey_600_24dp),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = if (media.isFavourite) colorResource(R.color.colorStateYellow) else Color.White,
        )
        if (media.mediaListEntry != null) {
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.md_margin)))
            Icon(
                painter = painterResource(R.drawable.ic_remove_red_eye_white_18dp),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.White,
            )
        }
    }
}

@Preview(name = "Latest anime row", showBackground = true, widthDp = 360)
@Composable
private fun MediaLatestRowPreview() {
    val context = LocalContext.current
    val imageUrl = "android.resource://${context.packageName}/${R.drawable.reg_bg}"
    val media =
        remember(imageUrl) {
            MediaBase().apply {
                id = 501L
                type = KeyUtil.ANIME
                format = KeyUtil.TV
                status = KeyUtil.RELEASING
                title = MediaTitle(null, null, null, "Bocchi the Rock!")
                startDate = FuzzyDate(day = 1, month = 4, year = 2024)
                averageScore = 87
                coverImage = ImageBase(extraLarge = null, large = imageUrl, medium = null)
                episodes = 12
                nextAiringEpisode = AiringSchedule(timeUntilAiring = 4L * 24 * 60 * 60, episode = 8)
                isFavourite = true
                mediaListEntry = MediaList().apply { status = KeyUtil.CURRENT }
            }
        }
    val state = remember(media) { LatestMediaRowUiState.from(context, media) }

    AniTrendTheme {
        MediaLatestRow(state = state, onClick = {}, onLongClick = {})
    }
}
