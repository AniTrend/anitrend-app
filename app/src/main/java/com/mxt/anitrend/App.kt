package com.mxt.anitrend

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.analytics.FirebaseAnalytics
import com.mxt.anitrend.koin.AppModule.appModule
import com.mxt.anitrend.util.ApplicationPref
import com.mxt.anitrend.util.LocaleUtil
import io.fabric.sdk.android.Fabric
import io.wax911.emojify.EmojiManager
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by max on 2017/10/22.
 * Application class
 */

class App : MultiDexApplication() {

    val applicationPref by inject<ApplicationPref>()

    init {
        EventBus.builder().logNoSubscriberMessages(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(BuildConfig.DEBUG)
                .sendSubscriberExceptionEvent(BuildConfig.DEBUG)
                .throwSubscriberException(BuildConfig.DEBUG)
                .installDefaultEventBus()
    }

    /**
     * @return Application global registered firebase analytics
     *
     * @see com.mxt.anitrend.util.AnalyticsUtil
     */
    val analytics: FirebaseAnalytics? by lazy {
        if (applicationPref.isUsageAnalyticsEnabled == true) {
            FirebaseAnalytics.getInstance(this@App).apply {
                setAnalyticsCollectionEnabled(applicationPref.isUsageAnalyticsEnabled!!)
            }
        }
        null
    }

    /**
     * Get application global registered fabric instance, depending on
     * the current application preferences the application may have
     * disabled the current instance from sending any data
     *
     * @see com.mxt.anitrend.util.AnalyticsUtil
     */
    val fabric: Fabric? by lazy {
        if (!BuildConfig.DEBUG)
            if (applicationPref.isCrashReportsEnabled == true) {
                val crashlyticsCore = CrashlyticsCore.Builder()
                        .build()

                Fabric.with(Fabric.Builder(this)
                        .kits(crashlyticsCore)
                        .appIdentifier(BuildConfig.BUILD_TYPE)
                        .build())
            }
        null
    }

    /** [Koin](https://insert-koin.io/docs/2.0/getting-started/)
     * Initializes Koin dependency injection
     */
    private fun initializeDependencyInjection() {
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(listOf(appModule))
        }
    }

    private fun initializeApplication() {
        runCatching {
            EmojiManager.initEmojiData(this)
        }.exceptionOrNull()?.printStackTrace()
        runCatching {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                ProviderInstaller.installIfNeededAsync(
                        applicationContext,
                        object : ProviderInstaller.ProviderInstallListener {
                            override fun onProviderInstalled() {

                            }

                            override fun onProviderInstallFailed(i: Int, intent: Intent) {

                            }
                        }
                )
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun onCreate() {
        super.onCreate()
        initializeDependencyInjection()
        initializeApplication()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.onAttach(base))
        MultiDex.install(this)
    }
}
