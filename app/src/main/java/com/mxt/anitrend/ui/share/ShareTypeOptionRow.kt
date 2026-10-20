package com.mxt.anitrend.ui.share

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

@Composable
internal fun ShareTypeOptionRow(
    label: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
) {
    val rowPadding = dimensionResource(R.dimen.mg_margin)
    val iconSize = dimensionResource(R.dimen.xl_margin)
    val itemSpacing = dimensionResource(R.dimen.lg_margin)
    val foregroundColor = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier.padding(rowPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = foregroundColor,
        )
        Spacer(modifier = Modifier.width(itemSpacing))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
            color = foregroundColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(itemSpacing))
    }
}

@Preview(name = "Share type option", showBackground = true)
@Composable
private fun ShareTypeOptionRowPreview() {
    AniTrendTheme("light") {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ShareTypeOptionRow(
                label = "Plain Text",
                iconRes = R.drawable.ic_textsms_white_24dp,
            )
        }
    }
}
