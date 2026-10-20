package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.currentAniTrendTheme
import com.mxt.anitrend.ui.splash.SplashContent
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.base.WelcomeActivity
import com.mxt.anitrend.viewmodel.StartupViewModel
import com.mxt.anitrend.viewmodel.StartupViewModel.StartupUiState
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Splash screen hosting the startup sequence owned by [StartupViewModel].
 *
 * The retained behavior is unchanged: silent update scheduling, auth
 * invalidation, data migrations (skipped on fresh installs), then routing
 * to the onboarding tour or the main activity. The migration-failure
 * dialog keeps its acknowledge-and-exit semantics.
 */
class SplashActivity : CommonActivity() {

    private val startupViewModel by viewModel<StartupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AniTrendTheme(currentAniTrendTheme()) {
                val state by startupViewModel.state.collectAsStateWithLifecycle()

                when (val current = state) {
                    is StartupUiState.Ready -> LaunchedEffectRoute(
                        showOnboarding = current.showOnboarding,
                        onRoute = ::routeTo,
                    )
                    else -> SplashContent(
                        state = state,
                        onMigrationFailureAcknowledged = { finish() },
                    )
                }
            }
        }
    }

    private fun routeTo(showOnboarding: Boolean) {
        startActivity(
            Intent(
                this,
                if (showOnboarding) {
                    WelcomeActivity::class.java
                } else {
                    MainActivity::class.java
                },
            ),
        )
        finish()
    }
}

/** Runs the routing side effect exactly once per ready state. */
@androidx.compose.runtime.Composable
private fun LaunchedEffectRoute(
    showOnboarding: Boolean,
    onRoute: (Boolean) -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(showOnboarding) {
        onRoute(showOnboarding)
    }
}
