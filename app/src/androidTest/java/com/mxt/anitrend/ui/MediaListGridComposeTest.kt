@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.ui

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.MEDIA_LIST_PROGRESS_CONTROL_TAG
import com.mxt.anitrend.ui.medialist.MEDIA_LIST_CARD_TAG
import com.mxt.anitrend.ui.medialist.MEDIA_LIST_STATUS_STRIP_TAG
import com.mxt.anitrend.ui.medialist.MediaListGrid
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class MediaListGridComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun expandedAndCompactStylesProjectMetadataAndKeepAVisibleStatusStripe() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)

            runOnUiThread {
                installMediaListGrid(hostActivity, KeyUtil.LIST_VIEW_STYLE_DEFAULT)
            }
            waitForIdle()
            onNodeWithText("Media List Title").assertIsDisplayed()
            // Year/type and airing captions are native interop views
            // (SeriesYearTypeTextView / AiringTextView) inside AndroidView, so
            // Compose semantics cannot see their text: assert through the native
            // tree, tolerating styled spans from htmlText.
            Espresso.onView(ViewMatchers.withText(CoreMatchers.containsString("12 Episodes")))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(ViewMatchers.withText(CoreMatchers.containsString("Releasing")))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

            runOnUiThread {
                installMediaListGrid(hostActivity, KeyUtil.LIST_VIEW_STYLE_COMPACT_X1)
            }
            waitForIdle()
            onNodeWithText("Media List Title").assertIsDisplayed()
            onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_LIST_STATUS_STRIP_TAG, useUnmergedTree = true).assertIsDisplayed()
            val compactX1StatusHeight =
                onNodeWithTag(MEDIA_LIST_STATUS_STRIP_TAG, useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
                    .height
            assertTrue("compact x1 status stripe should have a visible row height", compactX1StatusHeight > 0f)

            runOnUiThread {
                installMediaListGrid(hostActivity, KeyUtil.LIST_VIEW_STYLE_COMPACT_X2)
            }
            waitForIdle()
            onNodeWithText("Media List Title").assertIsDisplayed()
            onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true).assertDoesNotExist()
            onNodeWithTag(MEDIA_LIST_STATUS_STRIP_TAG, useUnmergedTree = true).assertIsDisplayed()
            val compactX2StatusHeight =
                onNodeWithTag(MEDIA_LIST_STATUS_STRIP_TAG, useUnmergedTree = true)
                    .fetchSemanticsNode()
                    .boundsInRoot
                    .height
            assertTrue("compact x2 status stripe should have a visible row height", compactX2StatusHeight > 0f)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun rowClickLongClickAndProgressControlRouteToTheirCallbacks() {
        TestSessionUtil.setAuthenticated(InstrumentationRegistry.getInstrumentation().targetContext, authenticated = false)
        var openedMediaId: Long? = null
        var openedManageId: Long? = null
        var incrementedMediaId: Long? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installMediaListGrid(
                    activity = hostActivity,
                    style = KeyUtil.LIST_VIEW_STYLE_DEFAULT,
                    onIncrement = { incrementedMediaId = it.mediaId },
                    onOpenMedia = { openedMediaId = it.mediaId },
                    onOpenManage = { openedManageId = it.mediaId },
                )
            }
            waitForIdle()

            onNodeWithTag(MEDIA_LIST_CARD_TAG, useUnmergedTree = true).performClick()
            assertEquals(501L, openedMediaId)

            onNodeWithTag(MEDIA_LIST_CARD_TAG, useUnmergedTree = true)
                .performTouchInput { longClick() }
            assertEquals(501L, openedManageId)

            onNodeWithTag(MEDIA_LIST_PROGRESS_CONTROL_TAG, useUnmergedTree = true).performClick()
            assertEquals(501L, incrementedMediaId)
        }
    }

    private fun installMediaListGrid(
        activity: ProgressLayoutTestActivity,
        style: Int,
        onIncrement: (MediaListItemUiModel) -> Unit = {},
        onOpenMedia: (MediaListItemUiModel) -> Unit = {},
        onOpenManage: (MediaListItemUiModel) -> Unit = {},
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        MediaListGrid(
                            items = listOf(mediaListItem()),
                            style = style,
                            modifier = Modifier.fillMaxSize(),
                            listState = LazyGridState(),
                            onIncrement = onIncrement,
                            onIncrementBusy = {},
                            onIncrementLimit = {},
                            onOpenMedia = onOpenMedia,
                            onOpenManage = onOpenManage,
                        )
                    }
                }
            },
        )
    }

    private fun mediaListItem(): MediaListItemUiModel = MediaListItemUiModel(
        id = 1L,
        mediaId = 501L,
        status = KeyUtil.CURRENT,
        progress = 3,
        progressVolumes = 0,
        score = 8.7,
        repeat = 0,
        mediaTitle = "Media List Title",
        mediaTitleEnglish = "English Media Title",
        mediaTitleOriginal = "Original Media Title",
        mediaCoverImage = null,
        mediaType = KeyUtil.ANIME,
        mediaFormat = KeyUtil.TV,
        mediaStatus = KeyUtil.RELEASING,
        mediaEpisodes = 12,
        mediaChapters = 0,
        mediaVolumes = 0,
        mediaStartDate = FuzzyDateRecord(year = 2024, month = 4, day = 1),
        nextAiringEpisode = null,
        mediaIsFavourite = false,
        isIncrementPending = false,
        isDeletePending = false,
        canIncrement = true,
    )
}
