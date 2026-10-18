package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

@Composable
internal fun StudioRow(
    studioName: String,
    isFavourite: Boolean,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography
    val nameStyle =
        remember(typography) {
            typography.bodyMedium.copy(
                letterSpacing = 0.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
            )
        }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = studioName,
            modifier = Modifier.weight(1f),
            style = nameStyle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        if (isFavourite) {
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.lg_margin)))
            Icon(
                painter = painterResource(R.drawable.ic_star_grey_600_24dp),
                contentDescription = null,
                modifier =
                Modifier
                    .size(dimensionResource(R.dimen.xl_margin))
                    .testTag("studio-favourite-marker"),
                tint = colorResource(R.color.colorStateYellow),
            )
        }
    }
}

@Preview(name = "Studio rows", showBackground = true, widthDp = 180)
@Composable
private fun StudioRowPreview() {
    AniTrendTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.lg_margin))) {
            StudioRow(studioName = "Studio One", isFavourite = false)
            StudioRow(studioName = "Studio Two", isFavourite = true)
        }
    }
}
