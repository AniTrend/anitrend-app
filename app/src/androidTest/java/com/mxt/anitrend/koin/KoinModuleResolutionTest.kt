package com.mxt.anitrend.koin

import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkerParameters
import io.mockk.mockk
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.test.check.checkModules

/**
 * Instrumentation test that verifies the full Koin dependency graph
 * resolves correctly at runtime with a real Android context.
 *
 * Uses an isolated [KoinApplication] created via [KoinApplication.init]
 * instead of the global Koin context, so the app's already-started Koin
 * instance is neither replaced nor stopped, and subsequent tests are not
 * poisoned. [checkModules] closes only the isolated instance.
 *
 * Must run on a device or emulator.
 */
class KoinModuleResolutionTest {

    @Suppress("DEPRECATION")
    @Test
    fun verifyAllKoinModulesResolveCorrectly() {
        val workerParameters: WorkerParameters = mockk(relaxed = true)

        KoinApplication.init().apply {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(appModules)
        }.checkModules {
            withInstance(workerParameters)
        }
    }
}
