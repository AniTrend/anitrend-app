package com.mxt.anitrend.ui

import android.content.Intent
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class EntryPointRenderAuthTest {
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = true)
    }

    @Test
    fun renderEntryPoints() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        EntryPointFixtures.authenticated(context)
            .filterNot { it.name == "SharedContentFragment" }
            .forEach { entry ->
                Log.i("EntryPointRenderAuthTest", "Launching ${entry.name}")
                val launchIntent = entry.intentProvider(context).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK,
                )
                ActivityScenario.launch<android.app.Activity>(launchIntent).use { scenario ->
                    scenario.onActivity { activity ->
                        if (entry.assertUi) {
                            assertTrue("Expected ${entry.name} decor view to be shown", activity.window.decorView.isShown)
                        }
                        activity.setIntent(launchIntent)
                        activity.finish()
                    }
                    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                }
            }
    }
}
