package com.mxt.anitrend

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.crashlytics.android.core.CrashlyticsCore
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.analytics.FirebaseAnalytics
import com.mxt.anitrend.model.entity.MyObjectBox
import com.mxt.anitrend.util.ApplicationPref
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.LocaleUtil
import io.fabric.sdk.android.Fabric
import io.objectbox.BoxStore
import io.wax911.emojify.EmojiManager
import org.greenrobot.eventbus.EventBus

/**
 * Created by max on 2017/10/22.
 * Application class
 */

class App : MultiDexApplication() {

    val applicationPref = ApplicationPref(this)

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
    var analytics: FirebaseAnalytics? = null
        private set
    /**
     * @return Default application object box database instance
     *
     * @see com.mxt.anitrend.data.DatabaseHelper
     */
    lateinit var boxStore: BoxStore
        private set

    /**
     * Get application global registered fabric instance, depending on
     * the current application preferences the application may have
     * disabled the current instance from sending any data
     *
     * @see com.mxt.anitrend.util.AnalyticsUtil
     */
    var fabric: Fabric? = null
        private set

    private fun setupBoxStore() {
        boxStore = MyObjectBox.builder()
                .androidContext(this@App)
                .build()
    }

    private fun setCrashAnalytics() {
        if (!BuildConfig.DEBUG)
            if (applicationPref.isCrashReportsEnabled == true) {
                val crashlyticsCore = CrashlyticsCore.Builder()
                        .build()

                fabric = Fabric.with(Fabric.Builder(this)
                        .kits(crashlyticsCore)
                        .appIdentifier(BuildConfig.BUILD_TYPE)
                        .build())
            }
    }

    private fun initApp() {
        if (applicationPref.isUsageAnalyticsEnabled == true) {
            analytics = FirebaseAnalytics.getInstance(this).apply {
                setAnalyticsCollectionEnabled(applicationPref.isUsageAnalyticsEnabled!!)
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            ProviderInstaller.installIfNeededAsync(applicationContext,
                    object : ProviderInstaller.ProviderInstallListener {
                        override fun onProviderInstalled() {

                        }

                        override fun onProviderInstallFailed(i: Int, intent: Intent) {

                        }
                    })
        try {
            EmojiManager.initEmojiData(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
        setCrashAnalytics()
        setupBoxStore()
        initApp()
    }

    override fun attachBaseContext(base: Context) {
        val appPrefs = ApplicationPref(base)
        super.attachBaseContext(LocaleUtil.onAttach(base, appPrefs))
        MultiDex.install(this)
    }
}
