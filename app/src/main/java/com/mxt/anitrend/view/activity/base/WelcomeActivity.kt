package com.mxt.anitrend.view.activity.base

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.currentAniTrendTheme
import com.mxt.anitrend.ui.onboarding.OnboardingPrivacySheet
import com.mxt.anitrend.ui.onboarding.OnboardingScreen
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.index.LoginActivity
import com.mxt.anitrend.view.activity.index.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Hosts the first-run onboarding tour and routes users into the main
 * activity.
 *
 * The Connect action launches [LoginActivity] for a result; only a
 * successful authentication and current-user load (RESULT_OK) completes the
 * tour. Cancelling or failing the flow leaves the tour on its last page
 * with the fresh-install flag intact. The guest action completes exactly
 * once. No runtime permissions are requested here.
 */
class WelcomeActivity : CommonActivity() {

    private val onboardingViewModel by viewModel<OnboardingViewModel>()

    private var completionInvoked: Boolean = false

    private val connectLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                completeOnboarding()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AniTrendTheme(currentAniTrendTheme()) {
                var privacyChoicesOpen by remember { mutableStateOf(false) }
                OnboardingScreen(
                    pages = onboardingViewModel.pages,
                    isAuthenticated = settings.isAuthenticated,
                    showPrivacyChoices = FirebaseApp.getApps(applicationContext).isNotEmpty(),
                    onOpenPrivacyChoices = { privacyChoicesOpen = true },
                    onConnectAniList = {
                        connectLauncher.launch(Intent(this, LoginActivity::class.java))
                    },
                    onContinueAsGuest = { completeOnboarding() },
                )
                if (privacyChoicesOpen) {
                    OnboardingPrivacySheet(
                        onDismiss = { privacyChoicesOpen = false },
                    )
                }
            }
        }
    }

    /** Marks the tour completed and hands off to the main activity exactly once. */
    private fun completeOnboarding() {
        if (completionInvoked) return
        completionInvoked = true
        onboardingViewModel.onPostFreshInstall()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
