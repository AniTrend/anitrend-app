package com.mxt.anitrend.koin

import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.check.checkModules

/**
 * Instrumentation test that verifies the full Koin dependency graph
 * resolves correctly at runtime with a real Android context.
 *
 * Must run on a device or emulator.
 */
class KoinModuleResolutionTest {

    @After
    fun tearDown() {
        stopKoin()
    }

    @Suppress("DEPRECATION")
    @Test
    fun `verify all Koin modules resolve correctly`() {
        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(appModules)
        }.checkModules()
    }
}
