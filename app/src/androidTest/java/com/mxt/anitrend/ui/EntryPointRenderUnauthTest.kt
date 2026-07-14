package com.mxt.anitrend.ui

import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertTrue
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
            Log.i("EntryPointRenderUnauthTest", "Launching ${entry.name}")
            ActivityScenario.launch<android.app.Activity>(entry.intentProvider(context)).use { scenario ->
                if (entry.assertUi) {
                    scenario.onActivity { activity ->
                        assertTrue("Expected ${entry.name} decor view to be shown", activity.window.decorView.isShown)
                    }
                }
            }
        }
    }
}
