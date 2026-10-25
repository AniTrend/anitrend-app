package com.mxt.anitrend.ui

import android.Manifest
import android.content.Intent
import android.view.View
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.search.SearchFragment
import io.mockk.coEvery
import io.mockk.mockk
import java.util.concurrent.CopyOnWriteArrayList
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

/**
 * Integration coverage for SearchFragment's media Paging Compose host, per-tab
 * native target swaps, scroll-gated gestures, submitted-query appends, and cached
 * detail-back restoration of both results and viewport.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SearchMediaTabsComposeTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private data class MediaRequest(val search: String, val page: Int)

    @Before
    fun dismissFirstRunBlockers() {
        // A fresh install raises the POST_NOTIFICATIONS system dialog, which
        // pauses the activity before the search fragment composes. Grant it
        // deterministically and verify, so a failed grant fails fast instead of
        // surfacing as a mysterious content timeout.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation
            .executeShellCommand("pm grant com.mxt.anitrend android.permission.POST_NOTIFICATIONS")
            .use { descriptor ->
                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { it.readBytes() }
            }
        val granted =
            instrumentation.targetContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        assertTrue("POST_NOTIFICATIONS granted before launch", granted)
    }

    @Test
    fun submittedMediaAppendSurvivesDetailBackWithoutRefreshReplay() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<SearchRepository>()
        val requests = CopyOnWriteArrayList<MediaRequest>()
        val repository = mockk<SearchRepository>(relaxed = true)
        coEvery {
            repository.searchMedia(
                id = any(),
                page = any(),
                perPage = any(),
                search = any(),
                type = any<MediaType>(),
                format = any(),
                startDate = any(),
                endDate = any(),
                season = any(),
                genres = any(),
                genresExclude = any(),
                isAdult = any(),
                sort = any(),
            )
        } coAnswers {
            val search = invocation.args[3] as? String ?: ""
            val page = invocation.args[1] as? Int ?: 1
            requests += MediaRequest(search, page)
            Result.success(mediaPage(search, page))
        }
        coEvery { repository.searchCharacter(any(), any(), any(), any(), any()) } returns
            Result.success(
                PageContainer<CharacterBase>().apply {
                    pageData = listOf(
                        CharacterBase().apply {
                            id = 1L
                            name = TitleBase("Character", null, null, null)
                        },
                    )
                    pageInfo = PageInfo().apply { setHasNextPage(false) }
                },
            )
        koin.declare(repository)
        try {
            val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_SEARCH)
                .putExtra(KeyUtil.arg_search, "naruto")
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                runCatching {
                    onView(withText("What's New!")).check(matches(isDisplayed()))
                    androidx.test.espresso.Espresso.pressBackUnconditionally()
                }
                composeRule.waitUntil("initial deterministic media page", 20_000L) {
                    requests.any { it.search == "naruto" && it.page == 1 } && mediaTextCount("naruto 1") > 0
                }

                // Scroll into the list so the next downward drag is a mid-list drag.
                onView(withId(R.id.refreshLayout)).perform(swipeUp())
                composeRule.waitForIdle()
                val beforeDrag = gridScrollOffsets()
                assertTrue("grid scrolled past the start", beforeDrag.any { it > 0f })

                // A downward drag must scroll the grid instead of starting refresh.
                onView(withId(R.id.refreshLayout)).perform(swipeDown())
                composeRule.waitForIdle()
                val afterDrag = gridScrollOffsets()
                assertTrue(
                    "mid-list downward drag scrolls the grid instead of triggering refresh",
                    beforeDrag.size == afterDrag.size &&
                        beforeDrag.zip(afterDrag).any { (before, after) -> after < before },
                )
                scenario.onActivity { activity ->
                    val refreshLayout = activity.findViewById<CustomSwipeRefreshLayout>(R.id.refreshLayout)
                    assertFalse("refresh stayed inactive", refreshLayout.isRefreshing())
                }

                // Switching to a grid tab and back exercises both native-target swaps.
                clickSectionChip(scenario, "har")
                composeRule.waitUntil("character grid visible", 20_000L) { characterRowCount() > 0 }
                clickSectionChip(scenario, "anime")
                composeRule.waitUntil("media cards visible after tab return", 20_000L) {
                    mediaCardCount() > 0
                }
                for (attempt in 0 until 12) {
                    if (mediaTextCount("naruto 1") > 0) break
                    onView(withId(R.id.refreshLayout)).perform(swipeDown())
                    composeRule.waitForIdle()
                }
                composeRule.onAllNodesWithTag("media-series-card", useUnmergedTree = true)[0]
                    .assertIsDisplayed()
                onView(withId(R.id.progressStateLoading)).check(matches(not(isDisplayed())))

                onView(withId(R.id.search_query_input))
                    .perform(replaceText("submitted"), pressImeActionButton())
                composeRule.waitUntil("submitted media query renders", 20_000L) {
                    requests.any { it.search == "submitted" && it.page == 1 } &&
                        mediaTextCount("submitted 1") > 0
                }
                for (attempt in 0 until 12) {
                    if (mediaTextCount("submitted 42") > 0) break
                    onView(withId(R.id.refreshLayout)).perform(swipeUp())
                    composeRule.waitForIdle()
                }
                composeRule.waitUntil("submitted query appends the second page", 20_000L) {
                    requests.any { it.search == "submitted" && it.page == 2 } &&
                        mediaTextCount("submitted 42") > 0
                }
                val beforeDetailCounts = mediaRequestCounts(requests, "submitted")
                val beforeDetailViewport = gridScrollOffsets()
                assertTrue("appended results retain a scrolled viewport", beforeDetailViewport.any { it > 0f })

                composeRule.onAllNodesWithTag("media-series-card", useUnmergedTree = true)[0]
                    .performClick()
                composeRule.waitUntil("media detail destination", 20_000L) {
                    currentDestination(scenario) == R.id.mediaFragment
                }
                androidx.test.espresso.Espresso.pressBack()
                composeRule.waitUntil("submitted media list restored after detail", 20_000L) {
                    currentDestination(scenario) == R.id.searchFragment &&
                        mediaTextCount("submitted 42") > 0
                }
                scenario.onActivity { activity ->
                    val search = requireNotNull(findSearchFragment(activity.supportFragmentManager.fragments))
                    search.parentFragmentManager.beginTransaction().detach(search).commitNow()
                    search.parentFragmentManager.beginTransaction().attach(search).commitNow()
                }
                composeRule.waitForIdle()
                composeRule.waitUntil("submitted media viewport restored after view recreation", 20_000L) {
                    currentDestination(scenario) == R.id.searchFragment &&
                        mediaTextCount("submitted 42") > 0
                }
                assertEquals(
                    "detail return and view recreation must not replay media refresh",
                    beforeDetailCounts,
                    mediaRequestCounts(requests, "submitted"),
                )
                val afterDetailViewport = gridScrollOffsets()
                assertTrue(
                    "detail return and view recreation preserve the media viewport",
                    beforeDetailViewport.size == afterDetailViewport.size &&
                        beforeDetailViewport.zip(afterDetailViewport).all { (before, after) ->
                            kotlin.math.abs(before - after) < 0.01f
                        },
                )
            }
        } finally {
            koin.declare(originalRepository)
        }
    }

    private fun mediaPage(search: String, page: Int): PageContainer<MediaBase> {
        val firstId = ((page - 1) * KeyUtil.PAGING_LIMIT + 1).toLong()
        val lastId = (page * KeyUtil.PAGING_LIMIT).toLong()
        return PageContainer<MediaBase>().apply {
            pageData = (firstId..lastId).map { id ->
                MediaBase().apply {
                    this.id = id
                    type = KeyUtil.ANIME
                    format = "TV"
                    episodes = 12
                    title = MediaTitle(null, null, null, "$search $id")
                }
            }
            pageInfo = PageInfo().apply { setHasNextPage(page == 1) }
        }
    }

    private fun mediaRequestCounts(
        requests: List<MediaRequest>,
        search: String,
    ): Map<Int, Int> = requests.filter { it.search == search }.groupingBy { it.page }.eachCount()

    private fun currentDestination(scenario: ActivityScenario<MainActivity>): Int? {
        var destination: Int? = null
        scenario.onActivity { activity ->
            destination = (activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment)
                .navController.currentDestination?.id
        }
        return destination
    }

    private fun findSearchFragment(fragments: List<androidx.fragment.app.Fragment>): SearchFragment? {
        for (fragment in fragments) {
            if (fragment is SearchFragment) return fragment
            findSearchFragment(fragment.childFragmentManager.fragments)?.let { return it }
        }
        return null
    }

    private fun mediaTextCount(text: String): Int = runCatching {
        composeRule.onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().size
    }.getOrElse { error ->
        probeErrors.appendLine("mediaTextCount($text): $error")
        0
    }

    private val probeErrors = StringBuilder()

    private fun viewDump(activity: MainActivity): String {
        val builder = StringBuilder()
        fun walk(view: View, depth: Int) {
            if (view is android.widget.TextView && view.text.isNotBlank()) {
                builder.appendLine("${"  ".repeat(depth)}${view.javaClass.simpleName}[visible=${view.visibility == View.VISIBLE}]: ${view.text}")
            }
            if (view is android.view.ViewGroup) {
                for (index in 0 until view.childCount) walk(view.getChildAt(index), depth + 1)
            }
        }
        walk(activity.window.decorView, 0)
        return builder.toString()
    }

    private fun sharedStateVisible(): Boolean = runCatching {
        onView(withText(R.string.layout_empty_response)).check(matches(isDisplayed()))
        true
    }.getOrElse {
        runCatching {
            onView(withText(R.string.button_try_again)).check(matches(isDisplayed()))
            true
        }.getOrDefault(false)
    }

    private fun mediaCardCount(): Int = runCatching {
        composeRule.onAllNodesWithTag("media-series-card", useUnmergedTree = true)
            .fetchSemanticsNodes().size
    }.getOrElse { error ->
        probeErrors.appendLine("mediaCardCount: $error")
        0
    }

    private fun characterRowCount(): Int = runCatching {
        composeRule.onAllNodesWithTag("character-row-card", useUnmergedTree = true)
            .fetchSemanticsNodes().size
    }.getOrElse { error ->
        probeErrors.appendLine("characterRowCount: $error")
        0
    }

    private fun gridScrollOffsets(): List<Float> {
        val nodes =
            runCatching {
                composeRule.onAllNodes(
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange),
                    useUnmergedTree = true,
                ).fetchSemanticsNodes()
            }.getOrDefault(emptyList())
        return nodes.map { it.config[SemanticsProperties.VerticalScrollAxisRange].value() }
    }

    private fun clickSectionChip(
        scenario: ActivityScenario<MainActivity>,
        textFragment: String,
    ) {
        composeRule.waitForIdle()
        scenario.onActivity { activity ->
            val group = activity.findViewById<ChipGroup>(R.id.search_section_selector)
            for (index in 0 until group.childCount) {
                val chip = group.getChildAt(index) as? Chip ?: continue
                if (chip.text.contains(textFragment, ignoreCase = true)) {
                    chip.performClick()
                    return@onActivity
                }
            }
        }
    }
}
