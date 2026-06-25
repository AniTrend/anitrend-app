package com.mxt.anitrend.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class EntryPointRenderUnauthTest {
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
    }

    @Test
    fun renderEntryPoints() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        EntryPointFixtures.unauthenticated(context).forEach { entry ->
            ActivityScenario.launch<android.app.Activity>(entry.intentProvider(context)).use {
                if (entry.assertUi) {
                    onView(isRoot()).check(matches(isDisplayed()))
                }
            }
        }
    }
}
