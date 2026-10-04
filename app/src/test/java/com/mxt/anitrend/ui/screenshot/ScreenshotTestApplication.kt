package com.mxt.anitrend.ui.screenshot

import android.app.Application
import com.mxt.anitrend.koin.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

/**
 * Test-only application bootstrap for screenshot captures.
 *
 * Starts the production Koin graph so custom views and production renderers
 * resolve their real injected collaborators, while never starting production
 * background work: no WorkManager factory, no scheduled jobs, and no capture
 * path performs network access. Themes, resources, custom views, and
 * ViewBinding all come from the production app.
 */
class ScreenshotTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@ScreenshotTestApplication)
                modules(appModules)
            }
        }
    }
}