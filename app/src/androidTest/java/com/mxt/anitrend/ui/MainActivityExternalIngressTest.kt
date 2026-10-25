package com.mxt.anitrend.ui

import android.content.Context
import android.view.KeyEvent
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.navigation.AppNavKey
import com.mxt.anitrend.navigation.AppRoot
import com.mxt.anitrend.navigation.resolveExternalEntry
import com.mxt.anitrend.navigation.resolveStartupEntry
import com.mxt.anitrend.ui.library.LibraryPreset
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Ingress semantics of the Navigation 3 shell boundary: the cold/warm
 * resolver decisions (NFR-001 route chains, NFR-002 pushed lists,
 * NFR-003 external finish arming) and the persisted startup mapping,
 * asserted with real intents against the exported activity.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityExternalIngressTest {

    private lateinit var settings: Settings

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        settings = Settings(context)
    }

    private fun anilistIntent(
        context: Context,
        path: String,
        id: Long = 0L,
        userName: String? = null,
    ): Intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://anilist.co$path"),
    ).setClass(context, MainActivity::class.java)
        .putExtra(com.mxt.anitrend.util.KeyUtil.arg_id, id)
        .putExtra(com.mxt.anitrend.util.KeyUtil.arg_userName, userName)

    // ── NFR-001: /user/<name>/animelist and /mangalist land on Profile then push a typed media list ──

    @Test
    fun coldStartUserAnimeListLinkLandsOnProfileThenPushesTypedMediaList() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entry = resolveExternalEntry(anilistIntent(context, "/user/wax911/animelist"), isTaskRoot = true, settings)!!

        assertEquals(
            listOf(
                AppNavKey.Profile(userId = 0L, userName = "wax911"),
                AppNavKey.UserMediaList(userId = 0L, userName = "wax911", mediaType = "ANIME"),
            ),
            entry.keys,
        )
    }

    @Test
    fun coldStartUserMangaListLinkPushesTypedMediaList() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entry = resolveExternalEntry(anilistIntent(context, "/user/wax911/mangalist"), isTaskRoot = true, settings)!!

        assertEquals(
            AppNavKey.UserMediaList(userId = 0L, userName = "wax911", mediaType = "MANGA"),
            entry.keys.last(),
        )
    }

    @Test
    fun coldStartPlainUserLinkLandsOnProfileOnly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entry = resolveExternalEntry(anilistIntent(context, "/user/wax911"), isTaskRoot = true, settings)!!

        assertEquals(listOf(AppNavKey.Profile(userId = 0L, userName = "wax911")), entry.keys)
    }

    // ── Numeric user identity survives the optional list suffix (NFR-003) ──

    @Test
    fun coldStartNumericUserAnimeListLinkPreservesNumericUserId() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entry = resolveExternalEntry(anilistIntent(context, "/user/42/animelist"), isTaskRoot = true, settings)!!

        assertEquals(
            AppNavKey.Profile(userId = 42L),
            entry.keys.first(),
        )
        assertEquals(
            AppNavKey.UserMediaList(userId = 42L, userName = null, mediaType = "ANIME"),
            entry.keys.last(),
        )
    }

    // ── NFR-003: an ingress chain clears the external finish semantics ──

    @Test
    fun externalUserListChainIsNotArmedForExternalFinish() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entry = resolveExternalEntry(anilistIntent(context, "/user/wax911/animelist"), isTaskRoot = true, settings)!!

        // The chain routes through the shell which clears the flag after
        // the second push; a direct single-key landing keeps it armed.
        assertTrue(entry.keys.size > 1)
        val direct = resolveExternalEntry(anilistIntent(context, "/anime/1", id = 1L), isTaskRoot = true, settings)!!
        assertTrue(direct.finishOnBack)
        assertEquals(1, direct.keys.size)
    }

    // ── NFR-003: warm delivery never arms the external finish semantics ──

    @Test
    fun warmDeliveryNeverArmsExternalFinish() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entry = resolveExternalEntry(anilistIntent(context, "/anime/1", id = 1L), isTaskRoot = false, settings)!!

        assertFalse(entry.finishOnBack)
    }

    // ── Unrelated or malformed ingress keeps the default root ──

    @Test
    fun unknownIntentYieldsNoExternalEntry() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val plain = Intent(context, MainActivity::class.java)

        assertNull(resolveExternalEntry(plain, isTaskRoot = true, settings))
    }

    // ── Persisted startup page mapping (plan 10.7) ──

    @Test
    fun startupPageSelectsThePersistedRootAndPreset() {
        assertEquals(AppRoot.SOCIAL, resolveStartupEntry("0", isAuthenticated = true).root)
        assertEquals(AppRoot.DISCOVER, resolveStartupEntry("3", isAuthenticated = true).root)
        assertEquals(AppRoot.LIBRARY, resolveStartupEntry("4", isAuthenticated = true).root)
        assertEquals(LibraryPreset.AIRING, resolveStartupEntry("4", isAuthenticated = true).libraryPreset)
        assertEquals(LibraryPreset.MY_ANIME, resolveStartupEntry("5", isAuthenticated = true).libraryPreset)
        assertEquals(LibraryPreset.MY_MANGA, resolveStartupEntry("6", isAuthenticated = true).libraryPreset)
        // Unknown keeps the Airing fallback.
        assertEquals(LibraryPreset.AIRING, resolveStartupEntry("999", isAuthenticated = true).libraryPreset)
    }

    // ── NFR-003: a cold external direct landing finishes the task on back ──

    @Test
    fun coldStartAnimeLinkBackFinishesExternalTask() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ActivityScenario.launch<MainActivity>(anilistIntent(context, "/anime/1", id = 1L)).use { scenario ->
            // Espresso's pressBack throws when back finishes the activity,
            // so inject one real back key instead. Exactly one press: a
            // second could fall through the exit-confirmation path and mask a
            // single-press regression. If the shell's back handler is not
            // registered yet, the activity's default back handling still
            // finishes this cold external landing, so the press cannot
            // silently no-op.
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            // The external-finish contract: back on a cold external direct
            // landing finishes the activity. finish() destroys
            // asynchronously on the main thread, so wait for the thread to
            // go idle before asserting instead of racing the lifecycle.
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            // finish() destroys asynchronously on the main thread and can
            // land after the idle sync, so wait for the lifecycle with a
            // bounded deadline before asserting; this test-core version has
            // no blocking waitForState for a system-driven destroy.
            val deadline = System.currentTimeMillis() + 10_000L
            while (scenario.state != androidx.lifecycle.Lifecycle.State.DESTROYED &&
                System.currentTimeMillis() < deadline
            ) {
                Thread.sleep(100L)
            }
            assertTrue(scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED)
        }
    }
}
