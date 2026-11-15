@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.hamcrest.Matchers.allOf
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.navigation.extension.navigateToChangelog
import com.mxt.anitrend.view.activity.index.MainActivity
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangelogComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun changelogShowsContentAndDismissesToPreviousDestination() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val navHost =
                mainActivity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
            val navController = navHost.navController
            val previousDestination = navController.currentDestination?.id
            assertNotNull(previousDestination)

            runOnUiThread {
                navController.navigateToChangelog()
            }
            waitForIdle()

            val currentFragment = navHost.childFragmentManager.primaryNavigationFragment
            assertTrue("Changelog should host Compose content", currentFragment?.view is ComposeView)
            onNodeWithText(mainActivity.getString(R.string.text_what_is_new)).assertIsDisplayed()
            onNodeWithText("v${BuildConfig.versionName}").assertIsDisplayed()
            onView(isAssignableFrom(RichMarkdownTextView::class.java))
                .check(matches(allOf(isDisplayed(), withText(containsString("What's Changed")))))

            val dismissNode = onNodeWithText(mainActivity.getString(R.string.Dismiss))
            dismissNode.performScrollTo()
            dismissNode.assertIsDisplayed().assertIsEnabled().assertHasClickAction()
            enableAccessibilityChecks()
            onRoot().tryPerformAccessibilityChecks()
            dismissNode.performClick()
            waitForIdle()

            runOnIdle {
                assertEquals(previousDestination, navController.currentDestination?.id)
            }
        }
    }
}
