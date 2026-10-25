package com.mxt.anitrend.sheet

import android.os.Bundle
import android.view.View
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mxt.anitrend.R
import com.mxt.anitrend.model.api.retro.base.GiphyService
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.model.entity.giphy.Pagination
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.view.sheet.BottomSheetListUsers
import com.mxt.anitrend.view.sheet.BottomSheetUsers
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class CollectionSheetTouchScrollTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun downwardDragWithinUserResultsScrollsWithoutDismissingTheSheet() {
        ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
            val sheet = BottomSheetUsers.Builder()
                .setModel((1L..60L).map { id -> UserBase(name = "User $id").apply { this.id = id } })
                .setTitle(R.string.title_bottom_sheet_likes)
                .build()
            scenario.onActivity { sheet.showNow(it.supportFragmentManager, "users-touch") }
            assertMidListDrag(scenario, sheet, R.id.userListComposeView)
        }
    }

    @Test
    fun downwardDragWithinGifResultsScrollsWithoutDismissingTheSheet() {
        val koin = GlobalContext.get()
        val originalService = koin.get<GiphyService>()
        val service = mockk<GiphyService>()
        val gifs = (1..90).map { Giphy(id = "$it", title = "GIF $it") }
        coEvery { service.getTrending(any(), any(), any(), any()) } returns
            Response.success(GiphyContainer(gifs, Pagination(total_count = gifs.size, count = gifs.size)))
        koin.declare(service)
        try {
            ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
                val sheet = BottomSheetGiphy().apply {
                    arguments = Bundle().apply { putInt(KeyUtil.arg_title, R.string.title_giphy_preview) }
                }
                scenario.onActivity { sheet.showNow(it.supportFragmentManager, "giphy-touch") }
                assertMidListDrag(scenario, sheet, R.id.giphy_grid_compose_view)
            }
        } finally {
            koin.declare(originalService)
        }
    }

    @Test
    fun downwardDragWithinFollowerResultsScrollsWithoutDismissingTheSheet() {
        val koin = GlobalContext.get()
        val originalRepository = koin.get<UserRepository>()
        val repository = mockk<UserRepository>()
        val users = (1L..60L).map { id ->
            UserBase(name = "User $id").apply { this.id = id }
        }
        val page = PageContainer<UserBase>().apply {
            pageData = users
            pageInfo = PageInfo().apply { setHasNextPage(false) }
        }
        coEvery { repository.getFollowers(1L, any(), any(), any()) } returns Result.success(page)
        coEvery { repository.getFollowing(any(), any(), any(), any()) } returns Result.success(PageContainer())
        koin.declare(repository)
        try {
            ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
                val sheet = BottomSheetListUsers.Builder()
                    .setUserId(1L)
                    .setRequestType(KeyUtil.USER_FOLLOWERS_REQ)
                    .setModelCount(users.size)
                    .setTitle(R.string.title_bottom_sheet_likes)
                    .build() as BottomSheetListUsers
                scenario.onActivity { sheet.showNow(it.supportFragmentManager, "followers-touch") }
                compose.waitUntil(timeoutMillis = 10_000L) { sheet.userItems.value.size == users.size }
                assertMidListDrag(scenario, sheet, R.id.userListComposeView)
            }
        } finally {
            koin.declare(originalRepository)
        }
    }

    private fun assertMidListDrag(
        scenario: ActivityScenario<ProgressLayoutTestActivity>,
        sheet: DialogFragment,
        hostId: Int,
    ) {
        compose.waitUntil(timeoutMillis = 10_000L) { scrollOffsets().isNotEmpty() }
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(30)
        compose.waitForIdle()
        val before = scrollOffsets()
        assertTrue("results must be scrolled before the downward drag", before.any { it > 0f })
        var topBefore = 0
        scenario.onActivity {
            topBefore = requireNotNull(sheet.dialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet).top
        }
        onView(withId(hostId)).perform(
            GeneralSwipeAction(
                Swipe.SLOW,
                GeneralLocation.translate(GeneralLocation.TOP_CENTER, 0f, 0.25f),
                GeneralLocation.translate(GeneralLocation.BOTTOM_CENTER, 0f, -0.25f),
                Press.FINGER,
            ),
        )
        compose.waitForIdle()
        val after = scrollOffsets()
        assertTrue("downward touch must return toward earlier rows", before.zip(after).any { (a, b) -> b < a })
        scenario.onActivity {
            assertTrue("mid-list dragging must not dismiss the dialog", sheet.dialog?.isShowing == true)
            val surface = requireNotNull(sheet.dialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            assertEquals(topBefore, surface.top)
            assertEquals(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.from(surface).state)
        }
    }

    private fun scrollOffsets(): List<Float> = compose.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange),
        useUnmergedTree = true,
    ).fetchSemanticsNodes().map { it.config[SemanticsProperties.VerticalScrollAxisRange].value() }
}
