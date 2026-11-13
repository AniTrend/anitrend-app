package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

@Composable
internal fun GroupTitleRow(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val captionStyle = mediaCaptionTextStyle()

    Box(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.CenterStart),
            style = captionStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        if (count > 0) {
            Text(
                text = count.toString(),
                modifier = Modifier.align(Alignment.CenterEnd),
                style = captionStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Group title states", showBackground = true, widthDp = 360)
@Composable
private fun GroupTitleRowPreview() {
    AniTrendTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.lg_margin))) {
            GroupTitleRow(title = "Adaptations", count = 20)
            GroupTitleRow(title = "1984", count = 0)
        }
    }
}
