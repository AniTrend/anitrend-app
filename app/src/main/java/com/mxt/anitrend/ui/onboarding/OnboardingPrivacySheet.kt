/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.settings.SettingsDivider
import com.mxt.anitrend.ui.settings.SettingsSectionCard
import com.mxt.anitrend.ui.settings.SettingsToggleRow
import com.mxt.anitrend.util.Settings

/**
 * Privacy choices sheet opened from the onboarding tour where Firebase is
 * present. It exposes the real crash-report and usage-analytics controls
 * with their existing values and defaults; changing nothing changes
 * nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OnboardingPrivacySheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val settings = Settings(context)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var crashReports by remember {
        mutableStateOf(
            settings.getBoolean(context.getString(R.string.pref_key_crash_reports), true),
        )
    }
    var usageAnalytics by remember {
        mutableStateOf(
            settings.getBoolean(context.getString(R.string.pref_key_usage_analytics), false),
        )
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        SettingsSectionCard(
            title = stringResource(R.string.pref_header_privacy),
            summary = stringResource(R.string.pref_header_privacy_summary),
            iconRes = R.drawable.ic_privacy_grey_600_24dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SettingsToggleRow(
                title = stringResource(R.string.pref_title_crash_reports),
                summary = stringResource(
                    if (crashReports) R.string.pref_crash_reports_summary_on else R.string.pref_crash_reports_summary_off,
                ),
                checked = crashReports,
                enabled = true,
                contentDescription = stringResource(R.string.pref_title_crash_reports),
                onCheckedChange = { newValue ->
                    crashReports = newValue
                    settings.edit {
                        putBoolean(context.getString(R.string.pref_key_crash_reports), newValue)
                    }
                },
            )
            SettingsDivider()
            SettingsToggleRow(
                title = stringResource(R.string.pref_title_usage_analytics),
                summary = stringResource(
                    if (usageAnalytics) R.string.pref_usage_analytics_summary_on else R.string.pref_usage_analytics_summary_off,
                ),
                checked = usageAnalytics,
                enabled = true,
                contentDescription = stringResource(R.string.pref_title_usage_analytics),
                onCheckedChange = { newValue ->
                    usageAnalytics = newValue
                    settings.edit {
                        putBoolean(context.getString(R.string.pref_key_usage_analytics), newValue)
                    }
                },
            )
        }
    }
}
