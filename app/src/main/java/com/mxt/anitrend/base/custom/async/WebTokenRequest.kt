package com.mxt.anitrend.base.custom.async

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.data.store.AccountStoreClearer
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.model.api.retro.ServiceFactory
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
        val presenter = koinOf<BasePresenter>()
        presenter.settings.isAuthenticated = false
        presenter.settings.lastDismissedNotificationId = -1
        presenter.database.invalidateBoxStores()
        KoinExt.get(AccountStoreClearer::class.java).clearAll()
        KoinExt.get(JobSchedulerUtil::class.java).cancelNotificationJob(context)
        ServiceFactory.invalidate()
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
        boxQuery: BoxQuery,
    ) {
        val now = System.currentTimeMillis() / 1000L
        if (token == null || (token?.expires ?: 0) < now) {
            val authCode = boxQuery.authCode?.code
            if (authCode == null) {
                Timber.e("Token had an invalid instance from context: %s", context)
                return
            }
            val response = ServiceFactory.requestCodeTokenSync(authCode)
            if (response != null) {
                createNewTokenReference(response)
                boxQuery.webToken = response
                Timber.d("Token refreshed & saved at time stamp: %s", System.currentTimeMillis() / 1000L)
            } else {
                Timber.e("Token had an invalid instance from context: %s", context)
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
            if (koinOf<Settings>().isAuthenticated) {
                val boxQuery = koinOf<BoxQuery>()
                val now = System.currentTimeMillis() / 1000L
                if (token == null || (token?.expires ?: 0) < now) {
                    token = boxQuery.webToken
                    checkTokenState(context, boxQuery)
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
    @Suppress("DEPRECATION")
    fun getToken(code: String): Boolean {
        val authenticatedToken = AuthenticationCodeAsync().execute(code).get()
        if (authenticatedToken != null) {
            createNewTokenReference(authenticatedToken)
            val boxQuery = koinOf<DatabaseHelper>()
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
            Timber.e(e, "createNewTokenReference failed")
        }
    }

    @Suppress("DEPRECATION")
    private class AuthenticationCodeAsync : AsyncTask<String, Void, WebToken>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg codes: String): WebToken? = ServiceFactory.requestCodeTokenSync(codes[0])
    }
}
