package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.binding.basicText
import com.mxt.anitrend.databinding.ActivityLoginBinding
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.*
import com.mxt.anitrend.viewmodel.LoginAuthState
import com.mxt.anitrend.viewmodel.LoginAuthViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Created by max on 2017/11/03.
 * Authentication activity
 */

class LoginActivity :
    ActivityBase<User, BasePresenter>(),
    View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: LoginAuthViewModel
    private var model: User? = null

    private val authStateObserver = Observer<LoginAuthState> { authState ->
        when (authState) {
            LoginAuthState.Loading -> Unit
            LoginAuthState.Success -> {
                presenter.settings.isAuthenticated = true
                viewModel?.params?.apply {
                    putBoolean(KeyUtil.arg_asHtml, false)
                }
                viewModel?.requestData(KeyUtil.USER_CURRENT_REQ, applicationContext)
            }
            is LoginAuthState.Failure -> {
                presenter.settings.isAuthenticated = false
                if (!TextUtils.isEmpty(authState.error) && !TextUtils.isEmpty(authState.errorDescription)) {
                    NotifyUtil.createAlerter(
                        this@LoginActivity,
                        authState.error.orEmpty(),
                        authState.errorDescription.orEmpty(),
                        R.drawable.ic_warning_white_18dp,
                        R.color.colorStateOrange,
                        KeyUtil.DURATION_LONG,
                    )
                } else {
                    NotifyUtil.createAlerter(
                        this@LoginActivity,
                        getString(R.string.login_error_title),
                        authState.errorDescription ?: getString(R.string.text_error_auth_login),
                        R.drawable.ic_warning_white_18dp,
                        R.color.colorStateRed,
                        KeyUtil.DURATION_LONG,
                    )
                }
                binding.widgetFlipper.showPrevious()
            }
        }
    }

    private val scheduler by inject<JobSchedulerUtil>()
    private val settings by inject<Settings>()

    /**
     * Some activities may have custom themes and if that's the case
     * override this method and set your own theme style, also if you wish
     * to apply the default navigation bar style for light themes
     * @see ActivityBase.configureActivity
     */
    override fun configureActivity() {
        setTheme(
            if (CompatUtil.isLightTheme(settings)) {
                R.style.AppThemeLight_Translucent
            } else {
                R.style.AppThemeDark_Translucent
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPresenter(BasePresenter(applicationContext))
        setViewModel(true)
        authViewModel = ViewModelProvider(this)[LoginAuthViewModel::class.java]
        authViewModel.authState.observe(this, authStateObserver)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.container.setOnClickListener(this)
        binding.authSignIn.setOnClickListener(this)
        binding.createAccountText.basicText(getString(R.string.create_new_account))
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        if (presenter.settings.isAuthenticated) {
            finish()
        } else {
            checkNewIntent(intent)
        }
    }

    override fun updateUI() {
        scheduler.scheduleNotificationJob(applicationContext)
        createApplicationShortcuts()
        finish()
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
                this@LoginActivity,
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

    override fun makeRequest() {
    }

    override fun onChanged(model: User?) {
        this.model = model
        if (model != null && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            presenter.database.currentUser = model
            updateUI()
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

    override fun showError(error: String) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            WebTokenRequest.invalidateInstance(applicationContext)
            NotifyUtil.createAlerter(
                this,
                getString(R.string.text_error_auth_login),
                error,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateRed,
                KeyUtil.DURATION_LONG,
            )
            binding.widgetFlipper.showPrevious()
            Timber.e(error)
        }
    }

    override fun showEmpty(message: String) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            WebTokenRequest.invalidateInstance(applicationContext)
            NotifyUtil.createAlerter(
                this,
                getString(R.string.text_error_auth_login),
                message,
                R.drawable.ic_warning_white_18dp,
                R.color.colorStateOrange,
                KeyUtil.DURATION_LONG,
            )
            binding.widgetFlipper.showPrevious()
            Timber.w(message)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (!presenter.settings.isAuthenticated) {
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
