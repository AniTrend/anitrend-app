package com.mxt.anitrend

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.android.gms.security.ProviderInstaller
import com.mxt.anitrend.analytics.AnalyticsLogging
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.koin.AppModule.appModule
import com.mxt.anitrend.koin.AppModule.presenterModule
import com.mxt.anitrend.koin.AppModule.widgetModule
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.locale.LocaleUtil
import io.wax911.emojify.EmojiManager
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

/**
 * Created by max on 2017/10/22.
 * Application class
 */

class App : MultiDexApplication() {

    private val supportAnalytics by inject<ISupportAnalytics>()
    private val settings by inject<Settings>()

    /**
     * Timber logging tree depending on the build type we plant the appropriate tree
     */
    private fun plantLoggingTree() {
        when (BuildConfig.DEBUG) {
            true -> Timber.plant(Timber.DebugTree())
            else -> Timber.plant(supportAnalytics as AnalyticsLogging)
        }
    }

    private fun initializeEventBus() {
        EventBus.builder().logNoSubscriberMessages(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(BuildConfig.DEBUG)
                .sendSubscriberExceptionEvent(BuildConfig.DEBUG)
                .throwSubscriberException(BuildConfig.DEBUG)
                .installDefaultEventBus()
    }

    /** [Koin](https://insert-koin.io/docs/2.0/getting-started/)
     * Initializes Koin dependency injection
     */
    private fun initializeDependencyInjection() {
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(listOf(appModule, widgetModule, presenterModule))
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

    fun applyTheme() {
        if (CompatUtil.isLightTheme(settings.theme))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    override fun onCreate() {
        super.onCreate()
        initializeDependencyInjection()
        initializeApplication()
        initializeEventBus()
        plantLoggingTree()
        applyTheme()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.onAttach(base))
        MultiDex.install(this)
    }
}
