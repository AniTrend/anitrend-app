package com.mxt.anitrend.ui.screenshot

import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.robolectric.Shadows.shadowOf

/**
 * Shared teardown for screenshot captures.
 *
 * Captures can queue main-looper work (window and popup rendering) and touch
 * the coroutine main dispatcher; both leak across tests in a shared JVM and
 * break later plain tests that initialize `Dispatchers.Main`. Every screenshot
 * test drains the looper and restores the main dispatcher state.
 */
abstract class ScreenshotTestBase {

    @After
    fun releaseSharedJvmState() {
        shadowOf(Looper.getMainLooper()).idle()
        Dispatchers.resetMain()
    }
}
