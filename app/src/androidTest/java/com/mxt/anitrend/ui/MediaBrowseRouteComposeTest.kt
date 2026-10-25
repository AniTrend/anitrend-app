@file:Suppress("UnstableApiUsage", "UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.ui.mediabrowse.MEDIA_BROWSE_CARD_TAG
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
class MediaBrowseRouteComposeTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var originalRepository: BrowseRepository

    @Before
    fun setUp() {
        TestSessionUtil.setAuthenticated(
            ApplicationProvider.getApplicationContext(),
            authenticated = false,
        )
        originalRepository = GlobalContext.get().get()
        val repository = mockk<BrowseRepository>()
        coEvery {
            repository.getMediaBrowse(
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(),
            )
        } returns Result.success(pageContainer())
        GlobalContext.get().declare(repository)
    }

    @After
    fun tearDown() {
        GlobalContext.get().declare(originalRepository)
    }

    @Test
    fun routedBrowseShowsComposeMediaAndOpensTheSelectedMediaDestination() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_ROUTE, MainActivity.ROUTE_MEDIA_BROWSE)
            .putExtra(KeyUtil.arg_mediaType, KeyUtil.ANIME)

        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            composeRule.waitUntil(60_000L) {
                composeRule.onAllNodesWithTag(MEDIA_BROWSE_CARD_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag(MEDIA_BROWSE_CARD_TAG, useUnmergedTree = true).assertIsDisplayed()
            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(R.id.mediaBrowseFragment, host.navController.currentDestination?.id)
            }

            composeRule.onNodeWithTag(MEDIA_BROWSE_CARD_TAG, useUnmergedTree = true).performClick()
            composeRule.waitForIdle()
            scenario.onActivity { activity ->
                val host = activity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
                assertEquals(R.id.mediaFragment, host.navController.currentDestination?.id)
                assertEquals(501L, host.navController.currentBackStackEntry?.arguments?.getLong(KeyUtil.arg_id))
                assertEquals(KeyUtil.ANIME, host.navController.currentBackStackEntry?.arguments?.getString(KeyUtil.arg_mediaType))
            }
        }
    }

    private fun pageContainer(): PageContainer<MediaBase> = PageContainer<MediaBase>().apply {
        pageData = listOf(
            MediaBase().apply {
                id = 501L
                type = KeyUtil.ANIME
                title = MediaTitle(null, null, null, "Routed Browse Series")
                status = KeyUtil.RELEASING
                format = KeyUtil.TV
                episodes = 12
                averageScore = 85
            },
        )
        pageInfo = PageInfo(total = 1, perPage = KeyUtil.PAGING_LIMIT, currentPage = 1).apply {
            setHasNextPage(false)
        }
    }
}
