package com.mxt.anitrend.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

private const val SETTINGS_CATEGORY_CARD_TEST_TAG = "settings-category-card"

@Composable
internal fun SettingsCategoryCard(
    title: String,
    summary: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsCardSurface(
            modifier =
            Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable(
                    interactionSource = null,
                    indication = ripple(color = MaterialTheme.colorScheme.secondaryContainer),
                    role = Role.Button,
                    onClick = onClick,
                ).semantics(mergeDescendants = true) {
                    contentDescription = title
                }
                .testTag(SETTINGS_CATEGORY_CARD_TEST_TAG),
        ) {
            SettingsCardHeader(
                title = title,
                summary = summary,
                iconRes = iconRes,
                modifier =
                Modifier
                    .padding(dimensionResource(R.dimen.sheet_card_padding))
                    .heightIn(min = 48.dp),
                trailingIconRes = R.drawable.ic_chevron_right_grey_600_24dp,
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.sheet_section_margin)))
    }
}

@Preview(name = "Settings category card", showBackground = true)
@Composable
private fun SettingsCategoryCardPreview() {
    AniTrendTheme {
        SettingsCategoryCard(
            title = "Customize",
            summary = "Customize the look and feel of the app",
            iconRes = R.drawable.ic_format_color_fill_grey_600_24dp,
            onClick = {},
        )
    }
}
