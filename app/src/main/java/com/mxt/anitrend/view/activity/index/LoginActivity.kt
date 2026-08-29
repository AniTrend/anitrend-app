package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.binding.basicText
import com.mxt.anitrend.databinding.ActivityLoginBinding
import com.mxt.anitrend.model.api.retro.ServiceFactory
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.ShortcutUtil
import com.mxt.anitrend.util.WidgetState
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.viewmodel.LoginAuthState
import com.mxt.anitrend.viewmodel.LoginAuthViewModel
import com.mxt.anitrend.viewmodel.LoginUserViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/** Hosts OAuth authentication and completes post-login setup. */
class LoginActivity :
    CommonActivity(),
    View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: LoginAuthViewModel by viewModel()
    private val userViewModel: LoginUserViewModel by viewModel()
    private var model: User? = null
    private val scheduler: JobSchedulerUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.container.setOnClickListener(this)
        binding.authSignIn.setOnClickListener(this)
        binding.createAccountText.basicText(getString(R.string.create_new_account))

        if (!settings.isAuthenticated) {
            checkNewIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Returning from the external browser (Chrome back button, or the flow was
        // abandoned) does not deliver a redirect intent, so the flipper would stay
        // on the loading state indefinitely. Restore the content state instead,
        // while an intent carrying redirect data is still handled by checkNewIntent.
        if (!settings.isAuthenticated && intent?.data == null) {
            restoreContentState()
        }
    }

    private fun restoreContentState() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) &&
            binding.widgetFlipper.displayedChild == WidgetState.LOADING_STATE
        ) {
            binding.widgetFlipper.showPrevious()
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
        binding.widgetFlipper.showPrevious()
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
        binding.widgetFlipper.showPrevious()
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

    companion object {
        /**
         * Post-login shortcut params. The media-list and profile routes need
         * typed [UserScreenParam] identity, but ShortcutInfo intent extras must
         * be PersistableBundle-safe (platform restriction since API 26, a
         * Parcelable value throws at build time), so the identity is carried as
         * wire primitives and the host reconstructs the typed parameter at
         * ingress. Kept pure for the instrumentation tests that assert the
         * produced [ShortcutInfo] extras.
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.auth_sign_in -> if (binding.widgetFlipper.displayedChild == WidgetState.CONTENT_STATE) {
                binding.widgetFlipper.showNext()
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ServiceFactory.API_AUTH_LINK)))
                } catch (e: Exception) {
                    Timber.e(e)
                    restoreContentState()
                    NotifyUtil.makeText(this, R.string.text_unknown_error, Toast.LENGTH_SHORT).show()
                }
            } else {
                NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            }
            R.id.container -> if (binding.widgetFlipper.displayedChild != WidgetState.LOADING_STATE) {
                finish()
            } else {
                NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
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
                if (binding.widgetFlipper.displayedChild == WidgetState.CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                }
                authViewModel.authenticate(intent.data.toString())
            }
        }
    }
}
