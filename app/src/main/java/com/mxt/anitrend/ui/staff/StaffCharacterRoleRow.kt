package com.mxt.anitrend.ui.staff

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.mediaCaptionTextStyle
import com.mxt.anitrend.util.KeyUtil

private const val FAVORITE_INDICATOR_TAG = "staff-role-favorite-indicator"
private const val STAFF_ROLE_IMAGE_TAG = "staff-role-image"

@Composable
internal fun StaffCharacterRoleRow(
    characterName: String,
    mediaTitle: String,
    imageUrl: String?,
    isFavourite: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.cardColor)) }
    val captionStyle = mediaCaptionTextStyle()
    val isPreview = LocalInspectionMode.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
                        .testTag(STAFF_ROLE_IMAGE_TAG),
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
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.lg_margin)),
                ) {
                    Text(
                        text = characterName,
                        style = captionStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = mediaTitle,
                        style = captionStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (isFavourite) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_grey_600_24dp),
                    contentDescription = null,
                    modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(dimensionResource(R.dimen.lg_margin))
                        .size(dimensionResource(R.dimen.xl_margin))
                        .testTag(FAVORITE_INDICATOR_TAG),
                    tint = colorResource(R.color.colorStateYellow),
                )
            }
        }
    }
}

@Preview(name = "Staff character role", showBackground = true, widthDp = 360)
@Composable
private fun StaffCharacterRoleRowPreview() {
    val context = LocalContext.current
    val imageUrl = "android.resource://${context.packageName}/${R.drawable.reg_bg}"

    AniTrendTheme {
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.lg_margin)),
        ) {
            StaffCharacterRoleRow(
                characterName = "Staff character",
                mediaTitle = "Associated media title",
                imageUrl = imageUrl,
                isFavourite = true,
                onClick = {},
            )
        }
    }
}
