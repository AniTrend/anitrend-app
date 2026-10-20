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
package com.mxt.anitrend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.migration.MigrationUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Startup state owner for the splash screen.
 *
 * Owns the retained startup sequence with the legacy order: silent update
 * scheduling, auth invalidation when an authenticated session lost its
 * cached user, the data migrations (skipped for fresh installs), and the
 * fresh-install routing decision. The sequence starts exactly once in
 * [viewModelScope] and runs on [ioDispatcher].
 */
class StartupViewModel(
    private val settings: Settings,
    private val userRepository: UserRepository,
    private val scheduler: JobSchedulerUtil,
    private val migrationUtil: MigrationUtil,
    applicationContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface StartupUiState {
        data object Initialising : StartupUiState

        /** Startup finished; [showOnboarding] routes to the first-run tour. */
        data class Ready(val showOnboarding: Boolean) : StartupUiState

        /** A required data migration failed; the user acknowledges and exits. */
        data object MigrationFailed : StartupUiState
    }

    private val appContext = applicationContext.applicationContext

    private val _state = MutableStateFlow<StartupUiState>(StartupUiState.Initialising)
    val state: StateFlow<StartupUiState> = _state.asStateFlow()

    init {
        start()
    }

    private fun start() {
        viewModelScope.launch {
            _state.value =
                withContext(ioDispatcher) {
                    // Legacy order: silent update check, then auth validation.
                    scheduler.startUpdateJob(appContext, silent = true)
                    if (settings.isAuthenticated && userRepository.cachedCurrentUser == null) {
                        WebTokenRequest.invalidateInstance(appContext)
                    }
                    delay(SPLASH_MIN_DELAY_MS)
                    // Fresh installs skip the data migrations entirely.
                    val migrationsApplied = settings.isFreshInstall || migrationUtil.applyMigration()
                    if (migrationsApplied) {
                        StartupUiState.Ready(showOnboarding = settings.isFreshInstall)
                    } else {
                        StartupUiState.MigrationFailed
                    }
                }
        }
    }

    private companion object {
        const val SPLASH_MIN_DELAY_MS = 500L
    }
}
