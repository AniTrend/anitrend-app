package com.mxt.anitrend.ui.character

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.mediaCaptionTextStyle
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

private const val MEDIA_HEADER_CARD_TAG = "media-header-card"
private const val MEDIA_HEADER_IMAGE_TAG = "media-header-image"
private const val MEDIA_HEADER_STATUS_TAG = "media-header-status"

internal data class CharacterMediaHeaderUiState(
    val coverImageUrl: String?,
    val status: String?,
    val title: String?,
    val subGroupTitle: String?,
    val format: String?,
) {
    companion object {
        fun from(media: MediaBase) = CharacterMediaHeaderUiState(
            coverImageUrl = media.coverImage?.let { it.extraLarge ?: it.large },
            status = media.status,
            title = media.title?.userPreferred,
            subGroupTitle = media.subGroupTitle,
            format = media.format,
        )
    }
}

/** Renders one media entry in the character-role grid; the adapter remains the action owner. */
@Composable
internal fun CharacterMediaHeaderRow(
    state: CharacterMediaHeaderUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.cardColor)) }
    val imageUrl = state.coverImageUrl
    val isPreview = LocalInspectionMode.current
    val titleStyle =
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
            platformStyle = PlatformTextStyle(includeFontPadding = true),
        )
    val captionStyle = mediaCaptionTextStyle()

    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            ).testTag(MEDIA_HEADER_CARD_TAG),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.sm_margin)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier =
                Modifier
                    .width(dimensionResource(R.dimen.avatar_size_xl))
                    .aspectRatio(1f / KeyUtil.AspectRatio)
                    .testTag(MEDIA_HEADER_IMAGE_TAG),
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
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.md_margin)))
            Column(
                modifier =
                Modifier
                    .weight(1f)
                    .padding(dimensionResource(R.dimen.lg_margin)),
                verticalArrangement = Arrangement.Top,
            ) {
                AndroidView(
                    factory = { statusContext -> SeriesStatusWidget(statusContext) },
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.sm_margin))
                        .testTag(MEDIA_HEADER_STATUS_TAG),
                    update = { statusView -> SeriesStatusWidget.setStatus(statusView, state.status) },
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                Text(
                    text = state.title.orEmpty(),
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                Text(
                    text = if (state.subGroupTitle.isNullOrBlank()) "" else CompatUtil.capitalizeWords(state.subGroupTitle),
                    style = captionStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                Text(
                    text = if (state.format.isNullOrBlank()) "" else CompatUtil.capitalizeWords(state.format),
                    style = captionStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(name = "Character media header", showBackground = true, widthDp = 120, heightDp = 160)
@Composable
private fun CharacterMediaHeaderRowPreview() {
    val context = LocalContext.current
    val imageUrl = "android.resource://${context.packageName}/${R.drawable.reg_bg}"
    val media =
        remember(imageUrl) {
            MediaBase().apply {
                id = 501L
                type = KeyUtil.ANIME
                format = KeyUtil.TV
                status = KeyUtil.RELEASING
                title = MediaTitle(null, null, null, "The Alternate Story")
                coverImage = ImageBase(null, imageUrl, null)
                subGroupTitle = "Supporting Role"
            }
        }

    AniTrendTheme {
        Box(modifier = Modifier.padding(dimensionResource(R.dimen.lg_margin))) {
            CharacterMediaHeaderRow(state = CharacterMediaHeaderUiState.from(media), onClick = {}, onLongClick = {})
        }
    }
}
