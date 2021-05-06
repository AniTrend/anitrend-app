package com.mxt.anitrend.analytics

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.extension.empty
import com.mxt.anitrend.util.Settings
import timber.log.Timber

@SuppressLint("MissingPermission")
class AnalyticsLogging(context: Context, settings: Settings) : Timber.Tree(), ISupportAnalytics {

    private val analytics by lazy(LazyThreadSafetyMode.NONE) {
        FirebaseApp.getApps(context).let {
            if (it.isNotEmpty())
                FirebaseAnalytics.getInstance(context).apply {
                    setAnalyticsCollectionEnabled(
                        settings.isUsageAnalyticsEnabled
                    )
                }
            else
                null
        }
    }

    private val crashlytics by lazy(LazyThreadSafetyMode.NONE) {
        FirebaseApp.getApps(context).let {
            if (it.isNotEmpty())
                FirebaseCrashlytics.getInstance().apply {
                    setCrashlyticsCollectionEnabled(
                        settings.isCrashReportsEnabled
                    )
                }
            else
                null
        }
    }

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.
     *
     * @param priority Log level. See [Log] for constants.
     * @param tag Explicit or inferred tag. May be `null`.
     * @param message Formatted log message. May be `null`, but then `t` will not be.
     * @param throwable Accompanying exceptions. May be `null`, but then `message` will not be.
     */
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority < Log.INFO)
            return

        runCatching {
            crashlytics?.setCustomKey(PRIORITY, priority)
            crashlytics?.setCustomKey(TAG, tag ?: "Unknown")
            crashlytics?.setCustomKey(MESSAGE, message)
        }.exceptionOrNull()?.printStackTrace()

        when (throwable) {
            null -> log(priority, tag, message)
            else -> logException(throwable)
        }
    }

    override fun logCurrentScreen(context: FragmentActivity, tag: String) {
        runCatching {
            analytics?.setCurrentScreen(context, tag, null)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun logCurrentState(tag: String, bundle: Bundle?) {
        runCatching {
            bundle?.also { analytics?.logEvent(tag, it) }
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun logException(throwable: Throwable) {
        runCatching {
            crashlytics?.recordException(throwable)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun log(priority: Int, tag: String?, message: String) {
        runCatching {
            crashlytics?.log(message)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun clearUserSession() {
        runCatching {
            crashlytics?.setUserId(String.empty())
            crashlytics?.deleteUnsentReports()
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun setCrashAnalyticUser(userIdentifier: String) {
        runCatching {
            crashlytics?.setUserId(userIdentifier)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun resetAnalyticsData() {
        runCatching {
            analytics?.resetAnalyticsData()
        }.exceptionOrNull()?.printStackTrace()
    }

    companion object {
        private const val PRIORITY = "priority"
        private const val TAG = "tag"
        private const val MESSAGE = "message"
    }
}