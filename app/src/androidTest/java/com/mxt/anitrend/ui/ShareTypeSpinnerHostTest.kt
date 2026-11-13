@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.endsWith
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareTypeSpinnerHostTest {

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            ApplicationProvider.getApplicationContext(),
            authenticated = false,
        )
    }

    @Test
    fun shareSheetDropdownKeepsSpinnerSelection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val options = context.resources.getStringArray(R.array.post_share_types)
        val launchIntent = Intent(context, MainActivity::class.java)

        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity ->
                val shareIntent =
                    Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "https://example.com")
                activity.javaClass.getDeclaredMethod("onNewIntent", Intent::class.java).apply {
                    isAccessible = true
                }.invoke(activity, shareIntent)
            }
            instrumentation.waitForIdleSync()

            scenario.onActivity { activity ->
                val spinner = activity.findViewById<Spinner>(R.id.sheet_share_post_type)
                assertEquals(KeyUtil.PLAIN_TYPE, spinner.selectedItemPosition)
            }
            onView(withId(R.id.sheet_share_post_type)).perform(click())
            instrumentation.waitForIdleSync()
            onData(equalTo(options[KeyUtil.IMAGE_TYPE]))
                .inAdapterView(withClassName(endsWith("DropDownListView")))
                .inRoot(isPlatformPopup())
                .check(matches(isDisplayed()))

            onData(equalTo(options[KeyUtil.IMAGE_TYPE]))
                .inAdapterView(withClassName(endsWith("DropDownListView")))
                .inRoot(isPlatformPopup())
                .perform(click())

            scenario.onActivity { activity ->
                assertEquals(
                    KeyUtil.IMAGE_TYPE,
                    activity.findViewById<Spinner>(R.id.sheet_share_post_type).selectedItemPosition,
                )
            }
            scenario.onActivity { activity ->
                activity.setIntent(launchIntent)
                activity.finish()
            }
            instrumentation.waitForIdleSync()
        }
    }
}
