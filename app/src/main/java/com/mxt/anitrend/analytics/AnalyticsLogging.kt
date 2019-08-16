package com.mxt.anitrend.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.extension.empty
import com.mxt.anitrend.util.Settings
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import timber.log.Timber

class AnalyticsLogging(
        context: Context,
        settings: Settings
): Timber.Tree(), ISupportAnalytics, KoinComponent {

    private var fabric: Fabric? = null

    init {
        if (settings.isCrashReportsEnabled)
            fabric = Fabric.with(Fabric.Builder(context)
                    .kits(Crashlytics())
                    .appIdentifier(BuildConfig.BUILD_TYPE)
                    .build())
    }

    private val analytics by lazy {
        FirebaseAnalytics.getInstance(context).apply {
            setAnalyticsCollectionEnabled(
                    settings.isUsageAnalyticsEnabled
            )
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

        Crashlytics.setInt(PRIORITY, priority)
        Crashlytics.setString(TAG, tag)
        Crashlytics.setString(MESSAGE, message)

        when (throwable) {
            null -> log(priority, tag, message)
            else -> logException(throwable)
        }
    }

    override fun logCurrentScreen(context: FragmentActivity, tag : String) {
        runCatching {
            fabric?.currentActivity = context
            analytics.setCurrentScreen(context, tag, null)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun logCurrentState(tag: String, bundle: Bundle?) {
        runCatching {
            bundle?.also { analytics.logEvent(tag, it) }
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun logException(throwable: Throwable) {
        runCatching {
            Crashlytics.logException(throwable)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun log(priority: Int, tag: String?, message: String) {
        runCatching {
            Crashlytics.log(priority, tag, message)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun clearUserSession() {
        runCatching {
            Crashlytics.setUserIdentifier(String.empty())
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun setCrashAnalyticUser(userIdentifier: String) {
        runCatching {
            Crashlytics.setUserIdentifier(userIdentifier)
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun resetAnalyticsData() {
        runCatching {
            analytics.resetAnalyticsData()
        }.exceptionOrNull()?.printStackTrace()
    }

    companion object {
        private const val PRIORITY = "priority"
        private const val TAG = "tag"
        private const val MESSAGE = "message"
    }
}