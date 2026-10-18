@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.ui.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.mediaCaptionTextStyle
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

private const val STAFF_ROW_TAG = "staff-row-card"
private const val STAFF_IMAGE_TAG = "staff-row-image"
private const val STAFF_FAVORITE_TAG = "staff-row-favorite"

internal data class StaffRowUiState(
    val name: String?,
    val image: ImageBase?,
    val language: String?,
    val isFavourite: Boolean,
) {
    companion object {
        fun from(model: StaffBase): StaffRowUiState = StaffRowUiState(
            name = model.name?.fullName,
            image = model.image,
            language = model.language?.takeIf { it.isNotBlank() }?.let(CompatUtil::capitalizeWords),
            isFavourite = model.isFavourite,
        )
    }
}

@Composable
internal fun StaffRow(
    state: StaffRowUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.cardColor)) }
    val typography = MaterialTheme.typography
    val nameStyle = remember(typography) {
        typography.bodyMedium.copy(platformStyle = PlatformTextStyle(includeFontPadding = true))
    }
    val captionStyle = mediaCaptionTextStyle()
    val image = state.image
    val imageUrl = image?.extraLarge ?: image?.large
    val isPreview = LocalInspectionMode.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag(STAFF_ROW_TAG),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.sm_margin)),
    ) {
        Box {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f / KeyUtil.AspectRatio)
                        .testTag(STAFF_IMAGE_TAG),
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
                    modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.lg_margin)),
                ) {
                    Text(
                        text = state.name.orEmpty(),
                        style = nameStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.language?.let { language ->
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.sm_margin)))
                        Text(
                            text = language,
                            style = captionStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (state.isFavourite) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_grey_600_24dp),
                    contentDescription = null,
                    modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(dimensionResource(R.dimen.lg_margin))
                        .size(dimensionResource(R.dimen.xl_margin))
                        .testTag(STAFF_FAVORITE_TAG),
                    tint = colorResource(R.color.colorStateYellow),
                )
            }
        }
    }
}

@Preview(name = "Staff row", showBackground = true, widthDp = 360)
@Composable
private fun StaffRowPreview() {
    val context = LocalContext.current
    val imageUrl = "android.resource://${context.packageName}/${R.drawable.reg_bg}"
    val staff =
        remember(imageUrl) {
            StaffBase().apply {
                id = 1L
                name = com.mxt.anitrend.model.entity.anilist.meta.TitleBase(
                    first = "Akira",
                    last = "Toriyama",
                    original = null,
                    alternative = null,
                )
                image = ImageBase(extraLarge = null, large = imageUrl, medium = null)
                language = "japanese"
                isFavourite = true
            }
        }

    AniTrendTheme {
        StaffRow(state = StaffRowUiState.from(staff), onClick = {})
    }
}
