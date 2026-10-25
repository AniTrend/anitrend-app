package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

internal data class MediaTagRowState(
    val name: String,
    val rankLabel: String,
    val isSpoiler: Boolean,
)

@Composable
internal fun MediaTagRow(
    state: MediaTagRowState,
    modifier: Modifier = Modifier,
) {
    val iconSize = dimensionResource(R.dimen.menu_item_icon_size)
    val iconSpacing = dimensionResource(R.dimen.xl_margin)
    val rankSpacing = dimensionResource(R.dimen.lg_margin)
    val accentColor = MaterialTheme.colorScheme.primary
    val nameColor =
        if (state.isSpoiler) {
            colorResource(R.color.colorStateOrange)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    val captionStyle =
        MaterialTheme.typography.bodySmall.copy(
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
            letterSpacing = 0.sp,
        )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_loyalty_white_24dp),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = accentColor,
        )
        Spacer(modifier = Modifier.width(iconSpacing))
        Text(
            text = state.name,
            modifier = Modifier.weight(1f),
            style = captionStyle,
            color = nameColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(rankSpacing))
        Text(
            text = state.rankLabel,
            style = captionStyle,
            color = accentColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Media tag rows", showBackground = true)
@Composable
private fun MediaTagRowPreview() {
    AniTrendTheme("light") {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MediaTagRow(
                    state = MediaTagRowState("High School", "80%", isSpoiler = false),
                )
                MediaTagRow(
                    state = MediaTagRowState("Mystery Story", "45%", isSpoiler = true),
                )
            }
        }
    }
}
