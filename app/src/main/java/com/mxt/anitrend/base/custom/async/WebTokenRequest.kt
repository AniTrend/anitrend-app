package com.mxt.anitrend.base.custom.async

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.WebToken
import com.mxt.anitrend.model.entity.base.AuthBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.ShortcutUtil
import timber.log.Timber
import java.util.concurrent.ExecutionException

/**
 * Created by max on 2017/10/14.
 * Web token requester
 */
object WebTokenRequest {
    private const val TAG = "WebTokenRequest"
    private val lock = Any()

    @Volatile
    private var token: WebToken? = null

    @JvmStatic
    fun getInstance(): WebToken? = token

    /**
     * Invalidate authentication state, defaulting to signed out state
     * and disable sync services
     */
    @JvmStatic
    fun invalidateInstance(context: Context) {
        val presenter = BasePresenter(context)
        presenter.settings.isAuthenticated = false
        presenter.settings.lastDismissedNotificationId = -1
        presenter.database.invalidateBoxStores()
        KoinExt.get(JobSchedulerUtil::class.java).cancelNotificationJob(context)
        WebFactory.invalidate()
        token = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutUtil.removeAllDynamicShortcuts(context)
        }
        KoinExt.get(ISupportAnalytics::class.java).clearUserSession()
    }

    /**
     * Double checks to assure that multiple threads attempting to access
     * the token don't invoke multiple refresh token requests all at once
     */
    private fun checkTokenState(
        context: Context,
        presenter: BasePresenter,
    ) {
        val now = System.currentTimeMillis() / 1000L
        if (token == null || (token?.expires ?: 0) < now) {
            val authCode = presenter.database.authCode?.code
            if (authCode == null) {
                Timber.tag(TAG).e("Token had an invalid instance from context: %s", context)
                return
            }
            val response = WebFactory.requestCodeTokenSync(authCode)
            if (response != null) {
                createNewTokenReference(response)
                presenter.database.webToken = response
                Timber.tag(TAG).d("Token refreshed & saved at time stamp: %s", System.currentTimeMillis() / 1000L)
            } else {
                Timber.tag(TAG).e("Token had an invalid instance from context: %s", context)
            }
        }
    }

    /**
     * Request a new token if the application has not been authenticated,
     * other wise request a new refresh token and replace the current token
     * retaining the refresh token key
     */
    @JvmStatic
    fun getToken(context: Context) {
        synchronized(lock) {
            if (KoinExt.get(Settings::class.java).isAuthenticated) {
                val presenter = BasePresenter(context)
                val now = System.currentTimeMillis() / 1000L
                if (token == null || (token?.expires ?: 0) < now) {
                    token = presenter.database.webToken
                    checkTokenState(context, presenter)
                }
            }
        }
    }

    /**
     * Request a new access token using access code for authenticated content,
     * and replace the current token with the new one from the server after authentication
     */
    @JvmStatic
    @Throws(ExecutionException::class, InterruptedException::class)
    fun getToken(code: String): Boolean {
        val authenticatedToken = AuthenticationCodeAsync().execute(code).get()
        if (authenticatedToken != null) {
            createNewTokenReference(authenticatedToken)
            val boxQuery = DatabaseHelper()
            boxQuery.webToken = authenticatedToken
            boxQuery.authCode = AuthBase(code, authenticatedToken.refresh_token)
            return true
        }
        return false
    }

    /**
     * Copies valid token data from a newly received token into the
     * existing token instance for persistence
     */
    private fun createNewTokenReference(webToken: WebToken) {
        try {
            webToken.calculateExpires()
            token = webToken.clone()
        } catch (e: CloneNotSupportedException) {
            Timber.tag(TAG).e(e, "createNewTokenReference failed")
        }
    }

    private class AuthenticationCodeAsync : AsyncTask<String, Void, WebToken>() {
        override fun doInBackground(vararg codes: String): WebToken? = WebFactory.requestCodeTokenSync(codes[0])
    }
}
