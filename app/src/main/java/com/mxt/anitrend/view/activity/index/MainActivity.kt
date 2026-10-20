package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.launchUpdateWorker
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.navigation.AppNavKey
import com.mxt.anitrend.navigation.ExternalEntry
import com.mxt.anitrend.navigation.resolveExternalEntry
import com.mxt.anitrend.navigation.resolveStartupEntry
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.currentAniTrendTheme
import com.mxt.anitrend.ui.navigation.AniTrendApp
import com.mxt.anitrend.ui.sheet.ConfirmSheet
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * The Navigation 3 application shell host.
 *
 * The activity retains only the external ingress and current-user
 * orchestration: cold entry resolution (external intent or persisted
 * startup page), warm delivery through [onNewIntent], the stale-guarded
 * current-user refresh, the once-per-version What's-new notice and the
 * sign-in/sign-out session lifecycle. Everything else — stacks, drawer,
 * back handling — belongs to [AniTrendApp].
 */
class MainActivity : com.mxt.anitrend.view.activity.CommonActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    /** The applied ingress; re-applied on the shell's live stacks per nonce. */
    private var entry by mutableStateOf(
        ExternalEntry(keys = emptyList()),
    )
    private var entryNonce by mutableStateOf(0L)

    private var authenticated by mutableStateOf(false)

    private var showSignOut by mutableStateOf(false)

    private var showLoginTip by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticated = settings.isAuthenticated

        // Cold entry: an external ingress wins, otherwise the persisted
        // startup page selects the root/preset.
        entry =
            resolveExternalEntry(intent, isTaskRoot, settings)
                ?: resolveStartupEntry(settings.startupPage, authenticated)

        observeCurrentUser()

        val activity = this
        setContent {
            AniTrendTheme(currentAniTrendTheme()) {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }

                Box(modifier = Modifier.fillMaxSize()) {
                    AniTrendApp(
                        isAuthenticated = authenticated,
                        startEntry = entry,
                        onOpenSignIn = { activity.startActivity(Intent(activity, LoginActivity::class.java)) },
                        onSignOut = { showSignOut = true },
                        onFinishExternalTask = { activity.finish() },
                        modifier = Modifier.fillMaxSize(),
                        entryNonce = entryNonce,
                    )
                    SnackbarHost(hostState = snackbarHostState)

                    if (showSignOut) {
                        ConfirmSheet(
                            titleRes = R.string.drawer_signout_title,
                            messageRes = R.string.drawer_signout_text,
                            confirmLabelRes = R.string.Yes,
                            onConfirm = {
                                showSignOut = false
                                WebTokenRequest.invalidateInstance(activity.applicationContext)
                                activity.finish()
                                activity.startActivity(Intent(activity, SplashActivity::class.java))
                            },
                            onDismiss = { showSignOut = false },
                        )
                    }
                    if (showLoginTip) {
                        ConfirmSheet(
                            titleRes = R.string.login_title,
                            messageRes = R.string.login_message,
                            confirmLabelRes = R.string.Ok,
                            onConfirm = { showLoginTip = false },
                            onDismiss = { showLoginTip = false },
                        )
                    }
                }

                // Once-per-version, nonblocking What's-new notice: marked
                // noticed when presented; opening the changelog is always
                // the user's choice and routes through the shell entry.
                LaunchedEffect(Unit) {
                    if (!settings.isUpdated) return@LaunchedEffect
                    settings.setUpdated()
                    val result =
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.changelog_whats_new),
                            actionLabel = context.getString(R.string.menu_title_changelog),
                            duration = SnackbarDuration.Long,
                        )
                    if (result == SnackbarResult.ActionPerformed) {
                        entry = ExternalEntry(keys = listOf(AppNavKey.Changelog))
                        entryNonce += 1L
                    }
                }
            }
        }

        // Flavor-owned update check: observes the update worker and
        // surfaces the result without any drawer menu dependency.
        launchUpdateWorker()
        requestCurrentUser()
    }

    override fun onResume() {
        super.onResume()
        // Session state may have changed through the login activity.
        authenticated = settings.isAuthenticated
        requestCurrentUser()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Warm delivery: singleTop routes the intent to the existing task,
        // so this is never a cold task-root ingress and must never arm
        // the external finish semantics (NFR-003). The resolver receives
        // isTaskRoot = false for exactly that reason.
        val warm = resolveExternalEntry(intent, isTaskRoot = false, settings) ?: return
        entry = warm
        entryNonce += 1L
        requestCurrentUser()
    }

    private fun observeCurrentUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.state.collect { state ->
                    when (state) {
                        is MainViewModel.UiState.Loading -> Unit
                        is MainViewModel.UiState.Success -> onCurrentUser(state.user)
                        is MainViewModel.UiState.Error -> {
                            Timber.e(state.message, "MainViewModel current user fetch failed")
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                NotifyUtil.createAlerter(
                                    this@MainActivity,
                                    getString(R.string.text_error_request),
                                    state.message,
                                    R.drawable.ic_warning_white_18dp,
                                    R.color.colorStateOrange,
                                    KeyUtil.DURATION_MEDIUM,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onCurrentUser(user: User) {
        if (settings.shouldShowTipFor(KeyUtil.KEY_LOGIN_TIP)) {
            settings.disableTipFor(KeyUtil.KEY_LOGIN_TIP)
            showLoginTip = true
        }
    }

    /**
     * Refreshes the cached current user at most once every five minutes,
     * matching the legacy throttled fetch contract.
     */
    private fun requestCurrentUser() {
        if (!settings.isAuthenticated) return
        val lastSyncedAt = settings.lastUserSyncTime
        if (DateUtil.timeDifferenceSatisfied(KeyUtil.TIME_UNIT_MINUTES, lastSyncedAt, 5)) {
            mainViewModel.loadCurrentUser()
            settings.lastUserSyncTime = System.currentTimeMillis()
        }
    }
}
