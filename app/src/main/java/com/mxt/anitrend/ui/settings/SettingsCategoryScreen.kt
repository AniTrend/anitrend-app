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
package com.mxt.anitrend.ui.settings

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings as SystemSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.checkNotificationPermission
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry
import com.mxt.anitrend.view.fragment.settings.SettingsRow

/**
 * Settings category screen: renders the registry's [SettingsRow] set for one
 * category inside a [SettingsSectionCard], reading current values from
 * [settings] and persisting immediately on change. Preference side effects
 * (scheduling, presenter refresh) stay route-owned through
 * [onPreferenceChanged]; live updates arrive via the shared preference
 * listener registered for the screen's lifetime.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsCategoryScreen(
    categoryId: String,
    settings: Settings,
    isFirebaseVisible: Boolean,
    isUpdateChannelVisible: Boolean,
    onPreferenceChanged: (key: String?) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val section =
        remember(categoryId, isFirebaseVisible, isUpdateChannelVisible) {
            SettingsCategoryRegistry.sectionFor(
                categoryId = categoryId,
                isFirebaseVisible = isFirebaseVisible,
                isUpdateChannelVisible = isUpdateChannelVisible,
            )
        }

    // Live re-render on preference changes while the screen is visible.
    var revision by rememberSaveable { mutableStateOf(0) }
    DisposableEffect(settings) {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                onPreferenceChanged(key)
                revision += 1
            }
        settings.registerOnSharedPreferenceChangeListener(listener)
        onDispose { settings.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    // Re-check the OS notification permission whenever the screen resumes
    // (for example after the system permission dialog or settings page).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        revision += 1
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackClick) {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.action_back),
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            val current = section ?: return@Column
            val visibleRows = remember(revision, current) { current.rows.filter { it.visible } }
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (current.id == SettingsCategoryRegistry.NOTIFICATIONS) {
                    item(key = "notification-permission-card") {
                        NotificationPermissionCard(revision = revision)
                    }
                }
                item(key = "settings-section-card") {
                    SettingsSectionCard(
                        title = stringResource(current.titleRes),
                        summary = stringResource(current.summaryRes),
                        iconRes = iconForSection(current.id),
                    ) {
                        visibleRows.forEachIndexed { index, row ->
                            when (row) {
                                is SettingsRow.Choice -> renderChoiceRow(row, settings, revision)
                                is SettingsRow.Toggle -> renderToggleRow(row, settings, revision)
                                is SettingsRow.Info -> renderInfoRow(row)
                            }
                            if (index < visibleRows.lastIndex) {
                                SettingsDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun renderChoiceRow(
    row: SettingsRow.Choice,
    settings: Settings,
    revision: Int,
) {
    val context = LocalContext.current
    androidx.compose.runtime.key(revision, row.keyRes) {
        val labels = context.resources.getStringArray(row.entriesRes)
        val values = context.resources.getStringArray(row.valuesRes)
        val preferenceKey = context.getString(row.keyRes)
        val currentValue = settings.getString(preferenceKey, row.defaultValue) ?: row.defaultValue
        val currentValueIndex = values.indexOf(currentValue).coerceAtLeast(0)
        val currentLabel = labels[currentValueIndex]
        val title = context.getString(row.titleRes)
        SettingsValueRow(
            title = title,
            summary = row.summaryRes?.let { context.getString(it) },
            value = currentLabel,
            enabled = row.enabled,
            // A11y parity with the toggle rows: the merged row announces
            // its title and current choice instead of only child texts.
            contentDescription =
            context.getString(
                R.string.settings_choice_content_description,
                title,
                currentLabel,
            ),
            showChevron = true,
            onClick = {
                val nextIndex = (currentValueIndex + 1) % values.size
                settings.edit { putString(preferenceKey, values[nextIndex]) }
            },
        )
    }
}

@Composable
private fun renderToggleRow(
    row: SettingsRow.Toggle,
    settings: Settings,
    revision: Int,
) {
    val context = LocalContext.current
    androidx.compose.runtime.key(revision, row.keyRes) {
        val preferenceKey = context.getString(row.keyRes)
        val checked = settings.getBoolean(preferenceKey, row.defaultValue)
        val accessibilityStateRes =
            if (checked) R.string.accessibility_switch_on else R.string.accessibility_switch_off
        SettingsToggleRow(
            title = context.getString(row.titleRes),
            summary = row.summaryRes(checked)?.let { context.getString(it) },
            checked = checked,
            enabled = row.enabled,
            contentDescription =
            context.getString(
                R.string.settings_toggle_content_description,
                context.getString(row.titleRes),
                context.getString(accessibilityStateRes),
            ),
            onCheckedChange = { newValue ->
                settings.edit { putBoolean(preferenceKey, newValue) }
            },
        )
    }
}

@Composable
private fun renderInfoRow(row: SettingsRow.Info) {
    val context = LocalContext.current
    SettingsValueRow(
        title = context.getString(row.titleRes),
        summary = context.getString(row.summaryRes),
        value = null,
        enabled = false,
        contentDescription = null,
        showChevron = false,
        onClick = null,
    )
}

/**
 * Truthful notification permission card for the Notifications category.
 *
 * Shown only while the OS denies notifications: it displays the real state,
 * requests POST_NOTIFICATIONS on API 33+ strictly from the explicit Enable
 * notifications action, and routes a denial to the system settings page
 * without ever claiming delivery is enabled.
 */
@Composable
private fun NotificationPermissionCard(revision: Int) {
    val context = LocalContext.current
    val permissionGranted = remember(revision) { context.checkNotificationPermission(null) }
    if (permissionGranted) return

    val openSystemSettings = {
        context.startActivity(
            Intent(SystemSettings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(SystemSettings.EXTRA_APP_PACKAGE, context.packageName),
        )
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) openSystemSettings()
        }

    SettingsSectionCard(
        title = stringResource(R.string.notification_permission_title),
        summary = stringResource(R.string.notification_permission_summary_denied),
        iconRes = R.drawable.ic_notifications_active_grey_600_24dp,
    ) {
        androidx.compose.material3.TextButton(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    openSystemSettings()
                }
            },
        ) {
            androidx.compose.material3.Text(
                text = stringResource(R.string.action_enable_notifications),
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@DrawableRes
private fun iconForSection(categoryId: String): Int = when (categoryId) {
    SettingsCategoryRegistry.ACCOUNT -> R.drawable.ic_account_circle_grey_600_24dp
    SettingsCategoryRegistry.APPEARANCE -> R.drawable.ic_format_size_grey_600_24dp
    SettingsCategoryRegistry.GENERAL -> R.drawable.ic_build_grey_600_24dp
    SettingsCategoryRegistry.NOTIFICATIONS -> R.drawable.ic_notifications_active_grey_600_24dp
    SettingsCategoryRegistry.DATA_SYNC -> R.drawable.ic_sync_grey_600_24dp
    SettingsCategoryRegistry.PRIVACY -> R.drawable.ic_privacy_grey_600_24dp
    else -> R.drawable.ic_build_grey_600_24dp
}
