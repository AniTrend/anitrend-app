package com.mxt.anitrend.ui

import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.model.api.retro.crunchy.EpisodeService
import com.mxt.anitrend.model.entity.crunchy.Channel
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.model.entity.crunchy.Rss
import com.mxt.anitrend.repository.CrunchyrollRepository
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.list.WatchListFragment
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class ToolbarEpisodeSearchNavigationTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Before
    fun grantNotificationPermission() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation.executeShellCommand(
                "pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS",
            ).use { descriptor ->
                ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
            }
        }
    }

    @Test
    fun toolbarSearchFiltersEpisodesAndClosingRestoresCachedRows() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<CrunchyrollRepository>()
        val settings = koin.get<Settings>()
        val originalAuthentication = settings.isAuthenticated
        val feed = Rss(
            channel = Channel(
                episode = listOf(
                    Episode(title = "First show", link = "https://example.test/first"),
                    Episode(title = "Target show", link = "https://example.test/target"),
                ),
            ),
        )
        val service = object : EpisodeService {
            override suspend fun getPopularFeed(): Response<Rss> = Response.success(feed)
            override suspend fun getLatestFeed(): Response<Rss> = Response.success(feed)
            override suspend fun getRssByUrl(link: String?): Response<Rss> = Response.success(feed)
        }
        koin.declare(CrunchyrollRepository(feedService = service, crunchyrollService = service))
        settings.isAuthenticated = false
        try {
            val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_AIRING)
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                runCatching {
                    onView(androidx.test.espresso.matcher.ViewMatchers.withText("What's New!"))
                        .check(
                            androidx.test.espresso.assertion.ViewAssertions.matches(
                                androidx.test.espresso.matcher.ViewMatchers.isDisplayed(),
                            ),
                        )
                    androidx.test.espresso.Espresso.pressBackUnconditionally()
                }
                scenario.onActivity { activity ->
                    val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                    host.navController.navigate(
                        R.id.watchListFragment,
                        Bundle().apply {
                            putBoolean(WatchListFragment.ARG_FEED_ROUTE, true)
                        },
                    )
                }
                compose.waitUntil("episode rows render", 20_000L) {
                    textCount("First show") == 1 && textCount("Target show") == 1
                }
                onView(withId(R.id.action_search)).perform(click())
                onView(withId(R.id.searchTextView)).perform(typeText("target"))
                compose.waitUntil("native toolbar filters the active episode destination", 10_000L) {
                    textCount("First show") == 0 && textCount("Target show") == 1
                }
                compose.onNodeWithText("Target show").assertIsDisplayed()
                onView(withId(R.id.action_up_btn)).perform(click())
                compose.waitUntil("closing search restores cached episode rows", 10_000L) {
                    textCount("First show") == 1 && textCount("Target show") == 1
                }
                onView(withId(R.id.action_filter)).check(doesNotExist())
            }
        } finally {
            settings.isAuthenticated = originalAuthentication
            koin.declare(originalRepository)
        }
    }

    private fun textCount(text: String): Int = compose.onAllNodesWithText(text, useUnmergedTree = true)
        .fetchSemanticsNodes().size
}
