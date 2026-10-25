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
package com.mxt.anitrend.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.AppNavKey
import com.mxt.anitrend.navigation.AppRoot
import com.mxt.anitrend.navigation.ExternalEntry
import com.mxt.anitrend.navigation.StackKey

/**
 * The Compose application shell: one Navigation 3 back stack per root,
 * a saveable active-root selection and the static route drawer.
 *
 * Push wraps a route with the next visit id, ignoring a consecutive
 * duplicate of the current top (launchSingleTop). Nonconsecutive visits
 * to the same entity keep distinct ids, saveable state and entry
 * ViewModels. Back on a single-entry stack returns to the root; back on
 * a root honoring a cold external ingress finishes the external task.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AniTrendApp(
    isAuthenticated: Boolean,
    startEntry: ExternalEntry,
    onOpenSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onFinishExternalTask: () -> Unit,
    modifier: Modifier = Modifier,
    entryNonce: Long = 0L,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val discoverStack = rememberNavBackStack(StackKey(entryId = 0L, route = AppNavKey.DiscoverRoot))
    val libraryStack = rememberNavBackStack(StackKey(entryId = 1L, route = AppNavKey.LibraryRoot))
    val socialStack = rememberNavBackStack(StackKey(entryId = 2L, route = AppNavKey.SocialRoot))

    var activeRoot by rememberSaveable { mutableStateOf(startEntry.root ?: AppRoot.DISCOVER) }
    var nextEntryId by rememberSaveable { mutableStateOf(3L) }
    var externalFinishArmed by rememberSaveable { mutableStateOf(startEntry.finishOnBack) }
    var drawerOpen by rememberSaveable { mutableStateOf(false) }
    var exitArmed by rememberSaveable { mutableStateOf(false) }

    // One saveable-state holder for every root's entries: root switches
    // swap the active back stack, and only shell-initiated pops may clear
    // entry state (see pop and the entry decorator below).
    val saveableHolder = rememberSaveableStateHolder()

    val backStack =
        when (activeRoot) {
            AppRoot.DISCOVER -> discoverStack
            AppRoot.LIBRARY -> libraryStack
            AppRoot.SOCIAL -> socialStack
        }

    // Resolves the active stack at call time: an ingress effect selects a
    // new root before pushing its keys, and a captured composition-local
    // `backStack` would still point at the previous root's stack.
    fun currentBackStack() = when (activeRoot) {
        AppRoot.DISCOVER -> discoverStack
        AppRoot.LIBRARY -> libraryStack
        AppRoot.SOCIAL -> socialStack
    }

    fun push(route: AppNavKey) {
        val stack = currentBackStack()
        val top = stack.lastOrNull() as? StackKey
        if (top?.route == route) return
        val entryId = nextEntryId
        nextEntryId += 1
        stack.add(StackKey(entryId = entryId, route = route))
    }

    fun pop() {
        val stack = currentBackStack()
        if (stack.size > 1) {
            val removed = stack.removeAt(stack.lastIndex)
            saveableHolder.removeState(removed)
        } else if (externalFinishArmed) {
            externalFinishArmed = false
            onFinishExternalTask()
        }
    }

    fun selectRoot(root: AppRoot) {
        if (root != activeRoot) activeRoot = root
    }

    fun openDrawer() {
        drawerOpen = true
    }

    // Applies the resolved ingress: the requested root becomes active and
    // its keys are pushed over the root entry. The nonce lets the activity
    // re-apply a fresh warm-delivery or notice entry on the same stacks.
    LaunchedEffect(startEntry, entryNonce) {
        startEntry.root?.let(::selectRoot)
        // NFR-003: a cold single-key external landing becomes the base of
        // its stack, so back from it finishes the external task instead of
        // surfacing a seeded root the user never chose.
        if (startEntry.finishOnBack && startEntry.keys.size == 1) {
            currentBackStack().clear()
        }
        startEntry.keys.forEach(::push)
        // NFR-003: an ingress chain (the `/user/<name>/animelist`
        // Profile + MediaList push) clears the external finish semantics;
        // only a single direct landing keeps them.
        if (startEntry.keys.size > 1) externalFinishArmed = false
    }

    // Root back keeps the legacy exit-confirm contract: the first press
    // warns, the second finishes the task. A cold external ingress on the
    // task root finishes on the first press instead.
    androidx.activity.compose.BackHandler(enabled = backStack.size <= 1) {
        when {
            externalFinishArmed -> {
                externalFinishArmed = false
                onFinishExternalTask()
            }
            exitArmed -> onFinishExternalTask()
            else -> {
                exitArmed = true
                android.widget.Toast
                    .makeText(
                        context,
                        com.mxt.anitrend.R.string.text_confirm_exit,
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    // Nav3's stock saveable decorator clears an entry's state whenever its
    // key leaves the active back stack. Root switches swap the entire list,
    // which would discard every root's saved UI state, so this shell keeps
    // the holder itself and only clears state for keys it pops explicitly.
    val saveableDecorator =
        remember(saveableHolder) {
            NavEntryDecorator<NavKey>(
                onPop = {},
                decorate = { entry ->
                    saveableHolder.SaveableStateProvider(entry.contentKey) {
                        entry.Content()
                    }
                },
            )
        }
    val viewModelDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()

    Box(modifier = modifier.fillMaxSize()) {
        @Suppress("DEPRECATION") // The single-scene overload matches the shell's one-pane model.
        NavDisplay(
            backStack = backStack,
            onBack = ::pop,
            entryDecorators = listOf(saveableDecorator, viewModelDecorator),
            entryProvider = { key: NavKey ->
                NavEntry(key) {
                    val stackKey = key as StackKey
                    AppEntryContent(
                        route = stackKey.route,
                        isAuthenticated = isAuthenticated,
                        push = ::push,
                        onOpenDrawer = ::openDrawer,
                        onBack = ::pop,
                    )
                }
            },
        )

        if (drawerOpen) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { drawerOpen = false },
                sheetState = sheetState,
            ) {
                NavigationDrawerSheet(
                    activeRoot = activeRoot,
                    isAuthenticated = isAuthenticated,
                    onRootSelected = { root ->
                        drawerOpen = false
                        selectRoot(root)
                    },
                    onRouteSelected = { route ->
                        drawerOpen = false
                        push(route)
                    },
                    onOpenSignIn = {
                        drawerOpen = false
                        onOpenSignIn()
                    },
                    onSignOut = {
                        drawerOpen = false
                        onSignOut()
                    },
                )
            }
        }
    }
}

/** Static drawer content: authentication header, primary roots and utility rows. */
@Composable
private fun NavigationDrawerSheet(
    activeRoot: AppRoot,
    isAuthenticated: Boolean,
    onRootSelected: (AppRoot) -> Unit,
    onRouteSelected: (AppNavKey) -> Unit,
    onOpenSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text =
            if (isAuthenticated) {
                stringResource(R.string.drawer_title_sign_out)
            } else {
                stringResource(R.string.drawer_title_sign_in)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier =
            Modifier
                .fillMaxWidth()
                .clickable { if (isAuthenticated) onSignOut() else onOpenSignIn() }
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        DrawerRow(
            label = stringResource(R.string.drawer_title_anime),
            selected = activeRoot == AppRoot.DISCOVER,
            onClick = { onRootSelected(AppRoot.DISCOVER) },
        )
        DrawerRow(
            label = stringResource(R.string.drawer_title_library),
            selected = activeRoot == AppRoot.LIBRARY,
            onClick = { onRootSelected(AppRoot.LIBRARY) },
        )
        DrawerRow(
            label = stringResource(R.string.drawer_title_home),
            selected = activeRoot == AppRoot.SOCIAL,
            onClick = { onRootSelected(AppRoot.SOCIAL) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerRow(
            label = stringResource(R.string.drawer_title_reviews),
            onClick = { onRouteSelected(AppNavKey.Reviews()) },
        )
        DrawerRow(
            label = stringResource(R.string.drawer_title_episodes),
            onClick = { onRouteSelected(AppNavKey.Episodes()) },
        )
        if (isAuthenticated) {
            DrawerRow(
                label = stringResource(R.string.drawer_title_profile),
                onClick = { onRouteSelected(AppNavKey.Profile(userId = 0L)) },
            )
            DrawerRow(
                label = stringResource(R.string.drawer_title_messages),
                onClick = { onRouteSelected(AppNavKey.Messages()) },
            )
            DrawerRow(
                label = stringResource(R.string.drawer_title_notifications),
                onClick = { onRouteSelected(AppNavKey.Notifications) },
            )
        }
        DrawerRow(
            label = stringResource(R.string.title_activity_settings),
            onClick = { onRouteSelected(AppNavKey.Settings) },
        )
        DrawerRow(
            label = stringResource(R.string.menu_title_about),
            onClick = { onRouteSelected(AppNavKey.About) },
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerRow(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}
