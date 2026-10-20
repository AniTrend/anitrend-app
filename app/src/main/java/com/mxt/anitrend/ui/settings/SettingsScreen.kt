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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.view.fragment.settings.SettingsCategory
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry

/**
 * Settings root: one list of category cards over [SettingsCategoryRegistry]
 * with bottom actions for Back, Changelog and Logging. The AniList category
 * owns the guest sign-in card; the only Display adult content row lives
 * there too, gated by flavor resources at the route.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    isFirebaseVisible: Boolean,
    onOpenCategory: (String) -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenLogs: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories =
        remember(isFirebaseVisible) {
            SettingsCategoryRegistry.categories(
                isFirebaseVisible = isFirebaseVisible,
            )
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
                    Text(
                        text = stringResource(R.string.action_back),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                TextButton(onClick = onOpenChangelog) {
                    Text(
                        text = stringResource(R.string.menu_title_changelog),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                TextButton(onClick = onOpenLogs) {
                    Text(
                        text = stringResource(R.string.title_activity_logging),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(categories, key = { "settings-category:${it.id}" }) { category: SettingsCategory ->
                    SettingsCategoryCard(
                        title = stringResource(category.titleRes),
                        summary = stringResource(category.summaryRes),
                        iconRes = iconForCategory(category.id),
                        onClick = { onOpenCategory(category.id) },
                    )
                }
            }
        }
    }
}

@DrawableRes
private fun iconForCategory(categoryId: String): Int = when (categoryId) {
    SettingsCategoryRegistry.ACCOUNT -> R.drawable.ic_account_circle_grey_600_24dp
    SettingsCategoryRegistry.APPEARANCE -> R.drawable.ic_format_size_grey_600_24dp
    SettingsCategoryRegistry.GENERAL -> R.drawable.ic_build_grey_600_24dp
    SettingsCategoryRegistry.NOTIFICATIONS -> R.drawable.ic_notifications_active_grey_600_24dp
    SettingsCategoryRegistry.DATA_SYNC -> R.drawable.ic_sync_grey_600_24dp
    SettingsCategoryRegistry.PRIVACY -> R.drawable.ic_privacy_grey_600_24dp
    SettingsCategoryRegistry.ABOUT -> R.drawable.ic_help_outline_grey_600_24dp
    else -> R.drawable.ic_build_grey_600_24dp
}
