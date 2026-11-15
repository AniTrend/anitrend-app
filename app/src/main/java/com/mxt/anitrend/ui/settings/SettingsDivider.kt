package com.mxt.anitrend.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

private const val SETTINGS_DIVIDER_TEST_TAG = "settings-divider"

@Composable
internal fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth().testTag(SETTINGS_DIVIDER_TEST_TAG),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
    )
}

@Preview(name = "Settings divider light", showBackground = true)
@Composable
private fun SettingsDividerPreview() {
    AniTrendTheme {
        Surface {
            SettingsDivider()
        }
    }
}

@Preview(
    name = "Settings divider dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SettingsDividerDarkPreview() {
    AniTrendTheme {
        Surface {
            SettingsDivider()
        }
    }
}
