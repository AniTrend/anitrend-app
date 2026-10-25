@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.navigation.AppNavKey
import com.mxt.anitrend.navigation.resolveExternalEntry
import com.mxt.anitrend.util.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedContentDismissalTest {

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            ApplicationProvider.getApplicationContext(),
            authenticated = false,
        )
    }

    private fun resolveShareEntry(isTaskRoot: Boolean) = requireNotNull(
        resolveExternalEntry(
            intent =
            Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "https://example.com"),
            isTaskRoot = isTaskRoot,
            settings = Settings(ApplicationProvider.getApplicationContext<Context>()),
        ),
    )

    @Test
    fun warmShareIntentResolvesToPushedComposerWithoutExternalFinish() {
        val entry = resolveShareEntry(isTaskRoot = false)

        assertEquals(
            listOf(AppNavKey.SharedContent(text = "https://example.com", subject = null)),
            entry.keys,
        )
        assertNull(entry.root)
        assertFalse(entry.finishOnBack)
    }

    @Test
    fun coldShareIntentArmsExternalFinishOnBack() {
        val entry = resolveShareEntry(isTaskRoot = true)

        assertEquals(
            listOf(AppNavKey.SharedContent(text = "https://example.com", subject = null)),
            entry.keys,
        )
        assertNull(entry.root)
        assertTrue(entry.finishOnBack)
    }
}
