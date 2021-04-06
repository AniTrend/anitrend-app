package com.mxt.anitrend

import androidx.multidex.MultiDexApplication
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.crash.runtime.UncaughtExceptionHandler
import org.koin.android.ext.android.get
import timber.log.Timber

/**
 * Created by max on 2017/10/22.
 * Application class
 */

class App : MultiDexApplication() {

    /**
     * Timber logging tree depending on the build type we plant the appropriate tree
     */
    private fun plantAnalyticsTree() {
        Timber.plant(get<ISupportAnalytics>() as Timber.Tree)
    }

    private fun createUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(
            UncaughtExceptionHandler()
        )
    }

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     *
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     *
     *
     * If you override this method, be sure to call `super.onCreate()`.
     *
     *
     * Be aware that direct boot may also affect callback order on
     * Android [android.os.Build.VERSION_CODES.N] and later devices.
     * Until the user unlocks the device, only direct boot aware components are
     * allowed to run. You should consider that all direct boot unaware
     * components, including such [android.content.ContentProvider], are
     * disabled until user unlock happens, especially when component callback
     * order matters.
     */
    override fun onCreate() {
        super.onCreate()
        createUncaughtExceptionHandler()
        plantAnalyticsTree()
    }
}
