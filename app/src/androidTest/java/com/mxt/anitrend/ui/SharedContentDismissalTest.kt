package com.mxt.anitrend.ui

import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Dismissal policy for the ACTION_SEND share composer (NFR-005, NFR-011).
 *
 * SharedContentFragment is a destination in MainActivity's root nav graph, so
 * hide dismissal must pop through the NavController: the controller's back
 * stack stays consistent and the previous destination is restored without
 * finishing the task. These tests reuse the MainActivityExternalIngressTest
 * harness (TestSessionUtil, the warm onNewIntent seam, and the nav host
 * accessor) and assert the resulting back stack and task state.
 */
@RunWith(AndroidJUnit4::class)
class SharedContentDismissalTest {

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            ApplicationProvider.getApplicationContext(),
            authenticated = false,
        )
    }

    @Test
    fun warmShareIntentHideDismissalReturnsToPreviousDestination() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = Intent(context, MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity ->
                val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "https://example.com")
                // Seam note: same ActivityScenario limitation as
                // MainActivityExternalIngressTest; a warm ACTION_SEND cannot
                // be delivered through the platform dispatch, so the
                // production override is invoked directly.
                activity.javaClass.getDeclaredMethod("onNewIntent", Intent::class.java).apply {
                    isAccessible = true
                }.invoke(activity, intent)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                assertEquals(R.id.sharedContentFragment, navController(activity).currentDestination?.id)
                // The start destination is added without a back-stack entry
                // (the navigator back stack is empty on first navigation), so
                // the composer's push is the only FragmentManager entry.
                assertEquals(1, host(activity).childFragmentManager.backStackEntryCount)
                // Hide the sheet exactly like a drag-to-dismiss or the toolbar
                // close action: the behavior state transition is what the
                // fragment's dismissal policy reacts to. The Material settle
                // animation dispatches STATE_HIDDEN when the animation ends,
                // so the pop is polled instead of assumed after one idle sync.
                val sheet = activity.findViewById<View>(R.id.design_bottom_sheet)
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_HIDDEN
            }
            awaitDestination(scenario, R.id.animeFragment)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val controller = navController(activity)
                // The destination is popped, not the task.
                assertEquals(R.id.animeFragment, controller.currentDestination?.id)
                assertNull(controller.previousBackStackEntry)
                assertFalse("hide dismissal must not finish the task", activity.isFinishing)
                // No stale NavController entry survives: the composer's entry
                // is removed and the host returns to the start-destination
                // shape (no FragmentManager back stack).
                assertEquals(0, host(activity).childFragmentManager.backStackEntryCount)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun systemBackDismissesSharedContentToPreviousDestination() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = Intent(context, MainActivity::class.java)
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity ->
                val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "https://example.com")
                // Seam note: same ActivityScenario limitation as the hide
                // test; a warm ACTION_SEND cannot be delivered through the
                // platform dispatch, so the production override is invoked
                // directly on the task-root activity.
                activity.javaClass.getDeclaredMethod("onNewIntent", Intent::class.java).apply {
                    isAccessible = true
                }.invoke(activity, intent)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.sharedContentFragment, controller.currentDestination?.id)
                // The warm share route sits on the start destination, the
                // previous entry beneath the composer.
                assertEquals(R.id.animeFragment, controller.previousBackStackEntry?.destination?.id)
                assertEquals(1, host(activity).childFragmentManager.backStackEntryCount)
                activity.onBackPressed()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity { activity ->
                val controller = navController(activity)
                assertEquals(R.id.animeFragment, controller.currentDestination?.id)
                assertNull(controller.previousBackStackEntry)
                assertFalse("back from the composer must not finish the task", activity.isFinishing)
                assertEquals(0, host(activity).childFragmentManager.backStackEntryCount)
                activity.setIntent(launchIntent)
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    private fun navController(activity: MainActivity): androidx.navigation.NavController {
        val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        return host.navController
    }

    private fun host(activity: MainActivity): NavHostFragment = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment

    /**
     * Polls until the NavController settles on [destinationId] or the deadline
     * passes. The sheet hide dispatch is driven by the Material settle
     * animation, whose completion is not reliably captured by a single
     * waitForIdleSync on slow emulators; polling the destination is the
     * deterministic way to observe the dismissal outcome.
     */
    private fun awaitDestination(
        scenario: ActivityScenario<MainActivity>,
        destinationId: Int,
    ) {
        val deadline = SystemClock.uptimeMillis() + DISMISSAL_TIMEOUT_MS
        var currentDestinationId: Int? = null
        while (SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity { activity ->
                currentDestinationId = navController(activity).currentDestination?.id
            }
            if (currentDestinationId == destinationId) {
                return
            }
            Thread.sleep(250)
        }
        assertEquals(
            "destination $destinationId not reached within $DISMISSAL_TIMEOUT_MS ms, was $currentDestinationId",
            destinationId,
            currentDestinationId,
        )
    }

    private companion object {
        const val DISMISSAL_TIMEOUT_MS = 30_000L
    }
}
