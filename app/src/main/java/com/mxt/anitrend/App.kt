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
    var boxStore: BoxStore? = null
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

    private fun setCrashAnalytics(pref: ApplicationPref) {
        if (!BuildConfig.DEBUG)
            if (pref.isCrashReportsEnabled!!) {
                val crashlyticsCore = CrashlyticsCore.Builder()
                        .build()

                fabric = Fabric.with(Fabric.Builder(this)
                        .kits(crashlyticsCore)
                        .appIdentifier(BuildConfig.BUILD_TYPE)
                        .build())
            }
    }

    private fun initApp(pref: ApplicationPref) {
        EventBus.builder().logNoSubscriberMessages(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(BuildConfig.DEBUG)
                .sendSubscriberExceptionEvent(BuildConfig.DEBUG)
                .throwSubscriberException(BuildConfig.DEBUG)
                .installDefaultEventBus()
        if (pref.isUsageAnalyticsEnabled!!) {
            analytics = FirebaseAnalytics.getInstance(this).apply {
                setAnalyticsCollectionEnabled(pref.isUsageAnalyticsEnabled!!)
            }
        }
        JobSchedulerUtil.scheduleJob(applicationContext)
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
        val pref = ApplicationPref(this)
        setCrashAnalytics(pref)
        setupBoxStore()
        initApp(pref)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtil.onAttach(base))
        MultiDex.install(this)
    }
}
