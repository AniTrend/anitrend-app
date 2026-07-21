package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.binding.basicText
import com.mxt.anitrend.databinding.ActivityLoginBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.ShortcutUtil
import com.mxt.anitrend.viewmodel.LoginAuthState
import com.mxt.anitrend.viewmodel.LoginAuthViewModel
import com.mxt.anitrend.viewmodel.LoginUserViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by max on 2017/11/03.
 * Authentication activity
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: LoginAuthViewModel
    private lateinit var userViewModel: LoginUserViewModel
    private var model: User? = null

    private val settings by lazy { KoinExt.get(Settings::class.java) }
    private val scheduler by lazy { koinOf<JobSchedulerUtil>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Preserve translucent theme (was previously handled by ActivityBase.configureActivity).
        setTheme(
            if (CompatUtil.isLightTheme(settings)) {
                R.style.AppThemeLight_Translucent
            } else {
                R.style.AppThemeDark_Translucent
            },
        )
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[LoginAuthViewModel::class.java]
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

        userViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    LoginUserViewModel(
                        userService = WebFactory.createService(
                            UserModel::class.java,
                            applicationContext,
                        ),
                    ) as T
            },
        )[LoginUserViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.state.collect { state ->
                    when (state) {
                        is LoginUserViewModel.UiState.Loading -> Unit
                        is LoginUserViewModel.UiState.Success -> {
                            model = state.user
                            koinOf<BoxQuery>().currentUser = model
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
            val SHORTCUT_MY_ANIME_BUNDLE = Bundle()
            SHORTCUT_MY_ANIME_BUNDLE.putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
            SHORTCUT_MY_ANIME_BUNDLE.putString(KeyUtil.arg_userName, model?.name)

            val SHORTCUT_MY_MANGA_BUNDLE = Bundle()
            SHORTCUT_MY_MANGA_BUNDLE.putString(KeyUtil.arg_mediaType, KeyUtil.MANGA)
            SHORTCUT_MY_MANGA_BUNDLE.putString(KeyUtil.arg_userName, model?.name)

            val SHORTCUT_PROFILE_BUNDLE = Bundle()
            SHORTCUT_PROFILE_BUNDLE.putString(KeyUtil.arg_userName, model?.name)

            ShortcutUtil.createShortcuts(
                this,
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_NOTIFICATION)
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_MY_ANIME)
                    .setShortcutParams(SHORTCUT_MY_ANIME_BUNDLE)
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_MY_MANGA)
                    .setShortcutParams(SHORTCUT_MY_MANGA_BUNDLE)
                    .build(),
                ShortcutUtil.ShortcutBuilder()
                    .setShortcutType(KeyUtil.SHORTCUT_PROFILE)
                    .setShortcutParams(SHORTCUT_PROFILE_BUNDLE)
                    .build(),
            )
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.auth_sign_in -> if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                binding.widgetFlipper.showNext()
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(WebFactory.API_AUTH_LINK)))
                } catch (e: Exception) {
                    Timber.e(e)
                    NotifyUtil.makeText(this, R.string.text_unknown_error, Toast.LENGTH_SHORT).show()
                }
            } else {
                NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            }
            R.id.container -> if (binding.widgetFlipper.displayedChild != WidgetPresenter.LOADING_STATE) {
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
                if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                }
                authViewModel.authenticate(intent.data.toString())
            }
        }
    }
}
