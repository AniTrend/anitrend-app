package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.model.api.retro.ServiceFactory
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.currentAniTrendTheme
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.ShortcutUtil
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.viewmodel.LoginAuthState
import com.mxt.anitrend.viewmodel.LoginAuthViewModel
import com.mxt.anitrend.viewmodel.LoginUserViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/** Hosts OAuth authentication and completes post-login setup. */
class LoginActivity : CommonActivity() {

    private val authViewModel: LoginAuthViewModel by viewModel()
    private val userViewModel: LoginUserViewModel by viewModel()
    private var model: User? = null
    private val scheduler: JobSchedulerUtil by inject()

    /** Loading flipper state, ported from the retired ViewFlipper widget. */
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel.authState.observe(this) { authState ->
            when (authState) {
                LoginAuthState.Loading -> Unit
                LoginAuthState.Success -> {
                    settings.isAuthenticated = true
                    userViewModel.loadCurrentUser()
                }
                is LoginAuthState.Failure -> {
                    settings.isAuthenticated = false
                    showAuthFailure(authState)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.state.collect { state ->
                    when (state) {
                        is LoginUserViewModel.UiState.Loading -> Unit
                        is LoginUserViewModel.UiState.Success -> {
                            model = state.user
                            setResult(RESULT_OK)
                            scheduleJobAndShortcuts()
                            finish()
                        }
                        is LoginUserViewModel.UiState.Error -> {
                            showCurrentUserError(state.message)
                        }
                    }
                }
            }
        }

        setContent {
            AniTrendTheme(currentAniTrendTheme()) {
                LoginScreen(
                    isLoading = isLoading,
                    onSignInClick = { signIn() },
                    onDismissClick = { dismiss() },
                )
            }
        }

        if (!settings.isAuthenticated) {
            checkNewIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Returning from the external browser (Chrome back button, or the flow was
        // abandoned) does not deliver a redirect intent, so the screen would stay
        // on the loading state indefinitely. Restore the content state instead,
        // while an intent carrying redirect data is still handled by checkNewIntent.
        if (!settings.isAuthenticated && intent?.data == null) {
            restoreContentState()
        }
    }

    private fun restoreContentState() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && isLoading) {
            isLoading = false
        }
    }

    private fun signIn() {
        if (!isLoading) {
            isLoading = true
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ServiceFactory.API_AUTH_LINK)))
            } catch (e: Exception) {
                Timber.e(e)
                isLoading = false
                NotifyUtil.makeText(this, R.string.text_unknown_error, Toast.LENGTH_SHORT).show()
            }
        } else {
            NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
        }
    }

    private fun dismiss() {
        if (!isLoading) {
            finish()
        } else {
            NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAuthFailure(authState: LoginAuthState.Failure) {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        if (!TextUtils.isEmpty(authState.error) && !TextUtils.isEmpty(authState.errorDescription)) {
            NotifyUtil.createAlerter(
                this,
                authState.error.orEmpty(),
                authState.errorDescription.orEmpty(),
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateOrange,
                KeyUtil.DURATION_LONG,
            )
        } else {
            NotifyUtil.createAlerter(
                this,
                getString(R.string.login_error_title),
                authState.errorDescription ?: getString(R.string.text_error_auth_login),
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateRed,
                KeyUtil.DURATION_LONG,
            )
        }
        isLoading = false
    }

    private fun showCurrentUserError(message: String) {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        WebTokenRequest.invalidateInstance(applicationContext)
        NotifyUtil.createAlerter(
            this,
            getString(R.string.text_error_auth_login),
            message,
            R.drawable.ic_warning_white_18dp,
            R.color.colorStateRed,
            KeyUtil.DURATION_LONG,
        )
        isLoading = false
        Timber.e(message)
    }

    private fun scheduleJobAndShortcuts() {
        scheduler.scheduleNotificationJob(applicationContext)
        createApplicationShortcuts()
    }

    private fun createApplicationShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutUtil.createShortcuts(
                this,
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_NOTIFICATION)
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_MY_ANIME)
                    .setShortcutParams(userShortcutBundle(model, KeyUtil.ANIME))
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_MY_MANGA)
                    .setShortcutParams(userShortcutBundle(model, KeyUtil.MANGA))
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_PROFILE)
                    .setShortcutParams(userShortcutBundle(model, null))
                    .build(),
                // NFR-004: Airing, Feed, and Trending are routable shortcut
                // destinations; register them when the launcher budget
                // permits. ShortcutUtil selects the set deterministically by
                // priority, so an over-budget launcher keeps the four legacy
                // producers above and never throws.
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_AIRING)
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_FEEDS)
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_TRENDING)
                    .build(),
            )
        }
    }

    /** Helpers for constructing post-login shortcut arguments. */
    companion object {
        /**
         * Post-login shortcut params. The media-list and profile routes carry
         * identity as wire primitives because ShortcutInfo intent extras must
         * be PersistableBundle-safe (platform restriction since API 26, a
         * Parcelable value throws at build time). Kept pure for the
         * instrumentation tests that assert the produced [ShortcutInfo] extras.
         */
        @VisibleForTesting
        internal fun userShortcutBundle(
            user: User?,
            mediaType: String?,
        ): Bundle = Bundle().apply {
            mediaType?.let { putString(KeyUtil.arg_mediaType, it) }
            user?.let {
                putLong(KeyUtil.arg_id, it.id)
                it.name?.let { name -> putString(KeyUtil.arg_userName, name) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (!settings.isAuthenticated) {
            checkNewIntent(intent)
        }
    }

    private fun checkNewIntent(intent: Intent?) {
        if (intent != null && intent.data != null) {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                if (!isLoading) {
                    isLoading = true
                }
                authViewModel.authenticate(intent.data.toString())
            }
        }
    }
}

/** Centered sign-in card: browser OAuth handoff with a loading/content flipper state. */
@Composable
private fun LoginScreen(
    isLoading: Boolean,
    onSignInClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    val markwon = createMarkwon(LocalContext.current) { link ->
        // A login flow has no in-app route destinations: every link opens externally.
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onDismissClick),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier =
            Modifier
                .padding(dimensionResource(R.dimen.spacing_xs))
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.anitrend_sign_in),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.anitrend_sign_in_browser),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier =
                    Modifier
                        .padding(top = dimensionResource(R.dimen.md_margin))
                        .fillMaxWidth(),
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier =
                        Modifier
                            .padding(top = dimensionResource(R.dimen.spacing_xs))
                            .size(48.dp),
                    )
                } else {
                    Button(
                        onClick = onSignInClick,
                        modifier =
                        Modifier
                            .padding(top = dimensionResource(R.dimen.spacing_xs))
                            .fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.sign_into_anilist))
                    }
                }
                MarkdownText(
                    markdown = stringResource(R.string.create_new_account),
                    markwon = markwon,
                    onLinkClick = {},
                    onLinkLongClick = {},
                    modifier =
                    Modifier
                        .padding(top = dimensionResource(R.dimen.spacing_xs))
                        .fillMaxWidth(),
                )
            }
        }
    }
}
