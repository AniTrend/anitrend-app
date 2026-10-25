@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CustomSwipeRefreshLayout
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.MediaLatestList
import com.mxt.anitrend.viewmodel.MediaLatestViewModel
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import io.mockk.coEvery
import io.mockk.mockk
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
@LargeTest
class MediaLatestAppendBoundaryTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun failedPageTwoBlocksLaterScrollUntilRetryRequestsPageTwo() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<BrowseRepository>()
        val requestedPages = CopyOnWriteArrayList<Int>()
        val pageTwoAttempts = AtomicInteger()
        val repository = mockk<BrowseRepository>(relaxed = true)
        coEvery {
            repository.getMediaBrowse(
                page = any(),
                perPage = any(),
                type = any(),
                sort = any(),
                isAdult = any(),
            )
        } coAnswers {
            val pageNumber = secondArg<Int?>() ?: 1
            requestedPages += pageNumber
            when (pageNumber) {
                1 -> Result.success(mediaPage(1, hasNextPage = true))
                2 -> if (pageTwoAttempts.incrementAndGet() == 1) {
                    Result.failure(IllegalStateException("page two failed"))
                } else {
                    Result.success(mediaPage(2, hasNextPage = false))
                }
                else -> Result.failure(IllegalStateException("unexpected page $pageNumber"))
            }
        }
        koin.declare(repository)

        try {
            ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
                var containerId = View.NO_ID
                scenario.onActivity { activity ->
                    val container = FrameLayout(activity).apply { id = View.generateViewId() }
                    containerId = container.id
                    activity.setContentView(container)
                    activity.supportFragmentManager.beginTransaction()
                        .replace(
                            container.id,
                            MediaLatestList.newInstance(
                                Bundle().apply {
                                    putString(KeyUtil.arg_mediaType, KeyUtil.ANIME)
                                },
                            ),
                        )
                        .commitNow()
                }
                composeRule.waitUntil("latest page one rows", 20_000L) {
                    requestedPages.contains(1) &&
                        latestState(scenario, containerId) is MediaLatestViewModel.UiState.Success &&
                        rowCount() > 0
                }

                Espresso.onView(withId(R.id.refreshLayout)).perform(swipeUp())
                composeRule.waitForIdle()
                val beforeDownwardDrag = scrollOffsets()
                assertTrue("latest grid must scroll before the boundary drag", beforeDownwardDrag.any { it > 0f })
                Espresso.onView(withId(R.id.refreshLayout)).perform(swipeDown())
                composeRule.waitForIdle()
                val afterDownwardDrag = scrollOffsets()
                assertTrue(
                    "mid-list downward touch scrolls the grid instead of refreshing the native parent",
                    beforeDownwardDrag.size == afterDownwardDrag.size &&
                        beforeDownwardDrag.zip(afterDownwardDrag).any { (before, after) -> after < before },
                )
                scenario.onActivity { activity ->
                    assertFalse(
                        activity.findViewById<CustomSwipeRefreshLayout>(R.id.refreshLayout).isRefreshing(),
                    )
                }

                for (attempt in 0 until 16) {
                    if (requestedPages.count { it == 2 } > 0) break
                    Espresso.onView(withId(R.id.refreshLayout)).perform(swipeUp())
                    composeRule.waitForIdle()
                }
                composeRule.waitUntil("page two failure", 20_000L) {
                    requestedPages.count { it == 2 } == 1 &&
                        pageTwoAttempts.get() == 1 &&
                        latestState(scenario, containerId).let { state ->
                            state is MediaLatestViewModel.UiState.Error && state.page == 2
                        }
                }
                Espresso.onView(withText(R.string.try_again)).check(matches(isDisplayed()))

                Espresso.onView(withId(R.id.refreshLayout)).perform(swipeDown())
                Espresso.onView(withId(R.id.refreshLayout)).perform(swipeUp())
                composeRule.waitForIdle()
                assertEquals("a later near-end movement must not skip the failed page", listOf(1, 2), requestedPages.toList())

                Espresso.onView(withText(R.string.try_again)).perform(click())
                composeRule.waitUntil("explicit retry of page two", 20_000L) {
                    requestedPages.size == 3 &&
                        requestedPages.lastOrNull() == 2 &&
                        latestState(scenario, containerId) is MediaLatestViewModel.UiState.Success
                }
                assertEquals("retry must repeat page two without requesting page three", listOf(1, 2, 2), requestedPages.toList())
                val latestItems = (latestState(scenario, containerId) as MediaLatestViewModel.UiState.Success).items
                assertEquals("latest rows retain one instance per media id", (1L..41L).toList(), latestItems.map { it.id })
            }
        } finally {
            koin.declare(originalRepository)
        }
    }

    private fun rowCount(): Int = composeRule.onAllNodesWithTag("latest-media-row", useUnmergedTree = true)
        .fetchSemanticsNodes().size

    private fun scrollOffsets(): List<Float> = composeRule.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange),
        useUnmergedTree = true,
    ).fetchSemanticsNodes().map { node ->
        node.config[SemanticsProperties.VerticalScrollAxisRange].value()
    }

    private fun latestState(
        scenario: ActivityScenario<ProgressLayoutTestActivity>,
        containerId: Int,
    ): MediaLatestViewModel.UiState? {
        var state: MediaLatestViewModel.UiState? = null
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(containerId)
            state = ViewModelProvider(requireNotNull(fragment))[MediaLatestViewModel::class.java].state.value
        }
        return state
    }

    private fun mediaPage(pageNumber: Int, hasNextPage: Boolean): PageContainer<MediaBase> {
        val ids = if (pageNumber == 1) 1L..40L else 40L..41L
        return PageContainer<MediaBase>().apply {
            pageData = ids.map { id ->
                MediaBase().apply {
                    this.id = id
                    type = KeyUtil.ANIME
                    title = MediaTitle(null, null, null, "Latest $id")
                    status = KeyUtil.FINISHED
                    format = KeyUtil.TV
                }
            }
            pageInfo = PageInfo().apply { setHasNextPage(hasNextPage) }
        }
    }
}
