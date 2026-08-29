@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.app.Activity
import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ToolbarHomeNavigationTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = true)
    }

    private fun launchEntryPoint(name: String): ActivityScenario<Activity> {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent =
            EntryPointFixtures.authenticated(context)
                .first { it.name == name }
                .intentProvider(context)
        @Suppress("UNCHECKED_CAST")
        return ActivityScenario.launch<Activity>(intent) as ActivityScenario<Activity>
    }

    /**
     * The toolbar navigation (up) button has no public view id, and AppCompat
     * detaches it from the toolbar when no navigation icon is set, which is
     * the pushed-destination state here. The destination listener registers
     * the production back policy directly on that button, so the harness
     * reaches it through the package-private [Toolbar.getNavButtonView] seam;
     * there is no public API for it.
     */
    private fun toolbarNavButton(activity: Activity): ImageButton? {
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        val method = Toolbar::class.java.getDeclaredMethod("getNavButtonView")
        method.isAccessible = true
        return method.invoke(toolbar) as? ImageButton
    }

    /**
     * The ROUTE_MEDIA_LIST entry (media-list shortcuts and the ingress route)
     * is pushed by default (NFR-002), so the up affordance returns to the
     * caller beneath the list instead of finishing the task. This test drives
     * the production toolbar navigation listener path: clicking the toolbar
     * navigation button invokes the listener the destination listener
     * registered on the toolbar, which forwards to
     * [com.mxt.anitrend.view.activity.index.MainActivity.navigateBackFromDestination].
     * The caller is captured from the back stack because the root beneath the
     * list depends on the configured startup page.
     */
    @Test
    fun pushedMediaList_toolbarUpReturnsToPreviousDestinationWithoutFinishingTask() {
        launchEntryPoint("MediaListFragment").use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            var callerDestination: Int? = null
            scenario.onActivity { activity ->
                val host = (activity as MainActivity).supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(R.id.mediaListFragment, host.navController.currentDestination?.id)
                callerDestination = host.navController.previousBackStackEntry?.destination?.id
                assertFalse("pushed media list must have a caller beneath it", callerDestination == null)
                val navButton = toolbarNavButton(activity)
                assertNotNull("pushed destinations must expose the toolbar up affordance", navButton)
                navButton!!.performClick()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val host = (activity as MainActivity).supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(callerDestination, host.navController.currentDestination?.id)
                assertFalse(
                    "toolbar up from the pushed media list must return to the caller, not finish the task",
                    activity.isFinishing,
                )
                activity.setIntent(Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java))
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    /**
     * System-back coverage for the same pushed-media-list contract. Kept
     * separate from the toolbar test: the deprecated back override routes
     * through the same production policy as the toolbar click
     * ([com.mxt.anitrend.view.activity.index.MainActivity.navigateBackFromDestination]),
     * and the two affordances must both keep caller-back semantics.
     */
    @Suppress("DEPRECATION")
    @Test
    fun pushedMediaList_systemBackReturnsToPreviousDestinationWithoutFinishingTask() {
        launchEntryPoint("MediaListFragment").use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            var callerDestination: Int? = null
            scenario.onActivity { activity ->
                val host = (activity as MainActivity).supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(R.id.mediaListFragment, host.navController.currentDestination?.id)
                callerDestination = host.navController.previousBackStackEntry?.destination?.id
                assertFalse("pushed media list must have a caller beneath it", callerDestination == null)
                activity.onBackPressed()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val host = (activity as MainActivity).supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(callerDestination, host.navController.currentDestination?.id)
                assertFalse(
                    "pushed media list back must return to the caller, not finish the task",
                    activity.isFinishing,
                )
                activity.setIntent(Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java))
                activity.finish()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }
}
