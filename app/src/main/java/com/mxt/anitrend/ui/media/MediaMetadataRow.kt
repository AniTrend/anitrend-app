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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

@Composable
internal fun mediaCaptionTextStyle(): TextStyle {
    val typography = MaterialTheme.typography
    return remember(typography) {
        typography.bodySmall.copy(
            fontFamily = typography.bodyMedium.fontFamily,
            letterSpacing = 0.sp,
            platformStyle = PlatformTextStyle(includeFontPadding = true),
        )
    }
}

@Composable
internal fun MediaMetadataRow(
    iconResource: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val captionStyle = mediaCaptionTextStyle()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconResource),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.menu_item_icon_size)),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.xl_margin)))
        Text(
            text = label,
            style = captionStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Media metadata rows", showBackground = true, widthDp = 180)
@Composable
private fun MediaMetadataRowPreview() {
    AniTrendTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.lg_margin))) {
            MediaMetadataRow(
                iconResource = R.drawable.ic_label_outline_white_24dp,
                label = "Slice Of Life",
            )
            MediaMetadataRow(
                iconResource = R.drawable.ic_link_white_24dp,
                label = "Twitter",
            )
        }
    }
}
