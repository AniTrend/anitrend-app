package com.mxt.anitrend.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

internal const val SETTINGS_DISABLED_ALPHA = 0.38f

private const val SETTINGS_VALUE_ROW_TEST_TAG = "settings-value-row"
private const val SETTINGS_VALUE_TEST_TAG = "settings-value-row-value"
private const val SETTINGS_CHEVRON_TEST_TAG = "settings-value-row-chevron"

@Composable
internal fun settingsRowTitleStyle(): TextStyle {
    val bodyLarge = MaterialTheme.typography.bodyLarge
    return remember(bodyLarge) { bodyLarge.copy(lineHeight = 22.sp) }
}

@Composable
internal fun SettingsValueRow(
    title: String,
    summary: String?,
    value: String?,
    enabled: Boolean,
    contentDescription: String?,
    showChevron: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val clickModifier = if (enabled && onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    val disabledTextModifier = if (enabled) Modifier else Modifier.semantics { disabled() }

    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(clickModifier)
            .alpha(if (enabled) 1f else SETTINGS_DISABLED_ALPHA)
            .semantics(mergeDescendants = contentDescription != null) {
                contentDescription?.let { this.contentDescription = it }
                if (!enabled) disabled()
            }
            .testTag(SETTINGS_VALUE_ROW_TEST_TAG)
            .padding(vertical = dimensionResource(R.dimen.lg_margin)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Match the legacy TextView's roughly 22dp BodyLarge line box; default Compose BodyLarge made summary rows 2dp taller.
            val titleStyle = settingsRowTitleStyle()
            Text(
                text = title,
                modifier = disabledTextModifier,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            summary?.let { summaryText ->
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                Text(
                    text = summaryText,
                    modifier = disabledTextModifier,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        value?.let { valueText ->
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.xl_margin)))
            Text(
                text = valueText,
                modifier =
                disabledTextModifier.testTag(SETTINGS_VALUE_TEST_TAG),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (showChevron) {
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.md_margin)))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right_grey_600_24dp),
                contentDescription = null,
                modifier = Modifier.size(20.dp).testTag(SETTINGS_CHEVRON_TEST_TAG),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Settings choice row", showBackground = true)
@Composable
private fun SettingsChoiceRowPreview() {
    AniTrendTheme {
        Surface {
            SettingsValueRow(
                title = "Application Theme",
                summary = null,
                value = "Light",
                enabled = true,
                contentDescription = "Application Theme, currently Light",
                showChevron = true,
                onClick = {},
            )
        }
    }
}

@Preview(
    name = "Settings info row dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SettingsInfoRowDarkPreview() {
    AniTrendTheme {
        Surface {
            SettingsValueRow(
                title = "Auto-play trailers",
                summary = "Coming soon",
                value = null,
                enabled = false,
                contentDescription = null,
                showChevron = false,
                onClick = null,
            )
        }
    }
}
