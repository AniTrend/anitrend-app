package com.mxt.anitrend.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

private const val SETTINGS_SECTION_CARD_TEST_TAG = "settings-section-card"

@Composable
internal fun SettingsSectionCard(
    title: String,
    summary: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsCardSurface(modifier = Modifier.testTag(SETTINGS_SECTION_CARD_TEST_TAG)) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.sheet_card_padding)),
            ) {
                SettingsCardHeader(
                    title = title,
                    summary = summary,
                    iconRes = iconRes,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))
                SettingsDivider()
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))
                content()
            }
        }
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.sheet_section_margin)))
    }
}

@Preview(name = "Settings section card", showBackground = true)
@Composable
private fun SettingsSectionCardPreview() {
    AniTrendTheme("light") {
        SettingsSectionCard(
            title = "Appearance",
            summary = "Theme, language and list view style",
            iconRes = R.drawable.ic_format_size_grey_600_24dp,
        ) {
            Column {
                androidx.compose.material3.Text(
                    text = "Application Theme",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
