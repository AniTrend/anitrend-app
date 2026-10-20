package com.mxt.anitrend.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

private const val SETTINGS_TOGGLE_ROW_TEST_TAG = "settings-toggle-row"

@Composable
internal fun SettingsToggleRow(
    title: String,
    summary: String?,
    checked: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleColor =
        if (enabled) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = SETTINGS_DISABLED_ALPHA)
        }
    val summaryColor =
        if (enabled) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SETTINGS_DISABLED_ALPHA)
        }

    val interactionModifier =
        if (enabled) {
            Modifier.toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
        } else {
            Modifier.semantics {
                role = Role.Switch
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                disabled()
            }
        }

    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(interactionModifier)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
            }
            .padding(vertical = dimensionResource(R.dimen.lg_margin))
            .testTag(SETTINGS_TOGGLE_ROW_TEST_TAG),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = settingsRowTitleStyle(),
                color = titleColor,
            )
            if (summary != null) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = summaryColor,
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.xl_margin)))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@Preview(name = "Settings toggle enabled", showBackground = true)
@Composable
private fun SettingsToggleRowPreview() {
    AniTrendTheme("light") {
        SettingsToggleRow(
            title = "New message notifications",
            summary = "Mark all messages as read",
            checked = true,
            enabled = true,
            contentDescription = "New message notifications, On",
            onCheckedChange = {},
        )
    }
}

@Preview(
    name = "Settings toggle disabled dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SettingsToggleRowDisabledPreview() {
    AniTrendTheme("dark") {
        SettingsToggleRow(
            title = "Notification workaround",
            summary = "Enable only if notifications are missing",
            checked = false,
            enabled = false,
            contentDescription = "Notification workaround, Off",
            onCheckedChange = {},
        )
    }
}
