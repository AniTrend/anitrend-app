@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.ui

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.mediabrowse.MEDIA_BROWSE_AIRING_TAG
import com.mxt.anitrend.ui.mediabrowse.MEDIA_BROWSE_CARD_TAG
import com.mxt.anitrend.ui.mediabrowse.MEDIA_BROWSE_RATING_TAG
import com.mxt.anitrend.ui.mediabrowse.MEDIA_BROWSE_STATUS_TAG
import com.mxt.anitrend.ui.mediabrowse.MediaBrowseGrid
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_RATING_TAG
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaBrowseGridComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun browseRowsPreserveEveryLegacyLayoutAndCardActions() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val media = mutableStateOf(mediaFixture(KeyUtil.ANIME), referentialEqualityPolicy())
            val style = mutableIntStateOf(KeyUtil.LIST_VIEW_STYLE_DEFAULT)
            val isCompatType = mutableStateOf(false)
            var clickedId: Long? = null
            var longClickedId: Long? = null

            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                MediaBrowseGrid(
                                    items = listOf(media.value),
                                    isCompatType = isCompatType.value,
                                    style = style.intValue,
                                    columnsResource = R.integer.single_list_x1,
                                    onMediaClick = { clickedId = it.id },
                                    onMediaLongClick = { longClickedId = it.id },
                                    onLoadMore = {},
                                )
                            }
                        }
                    },
                )
            }
            waitForIdle()

            onNodeWithTag(MEDIA_BROWSE_CARD_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Browse Series").assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_AIRING_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_STATUS_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_RATING_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_CARD_TAG, useUnmergedTree = true).performClick()
            assertEquals(501L, clickedId)
            onNodeWithTag(MEDIA_BROWSE_CARD_TAG, useUnmergedTree = true).performTouchInput { longClick() }
            assertEquals(501L, longClickedId)

            runOnUiThread { media.value = mediaFixture(KeyUtil.MANGA) }
            waitForIdle()
            onNodeWithTag(MEDIA_BROWSE_AIRING_TAG, useUnmergedTree = true).assertDoesNotExist()
            onNodeWithText("Browse Manga").assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_RATING_TAG, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread { media.value = mediaFixture(KeyUtil.ANIME) }
            waitForIdle()

            runOnUiThread { style.intValue = KeyUtil.LIST_VIEW_STYLE_COMPACT_X1 }
            waitForIdle()
            onNodeWithTag(MEDIA_BROWSE_AIRING_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_RATING_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_STATUS_TAG, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread {
                style.intValue = KeyUtil.LIST_VIEW_STYLE_COMPACT_X2
                media.value = mediaFixture(KeyUtil.MANGA)
            }
            waitForIdle()
            onNodeWithTag(MEDIA_BROWSE_AIRING_TAG, useUnmergedTree = true).assertDoesNotExist()
            onNodeWithTag(MEDIA_BROWSE_RATING_TAG, useUnmergedTree = true).assertDoesNotExist()
            onNodeWithTag(MEDIA_BROWSE_STATUS_TAG, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread {
                isCompatType.value = true
                style.intValue = KeyUtil.LIST_VIEW_STYLE_DEFAULT
                media.value = mediaFixture(KeyUtil.ANIME)
            }
            waitForIdle()
            onNodeWithText("Browse Series").assertIsDisplayed()
            onNodeWithTag(MEDIA_SERIES_RATING_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_BROWSE_AIRING_TAG, useUnmergedTree = true).assertDoesNotExist()
            onNodeWithTag(MEDIA_BROWSE_RATING_TAG, useUnmergedTree = true).assertDoesNotExist()
        }
    }

    private fun mediaFixture(type: String): MediaBase = MediaBase().apply {
        id = 501L
        this.type = type
        title = MediaTitle(null, null, null, if (type == KeyUtil.MANGA) "Browse Manga" else "Browse Series")
        status = KeyUtil.RELEASING
        format = if (type == KeyUtil.ANIME) KeyUtil.TV else KeyUtil.MANGA
        startDate = FuzzyDate(day = 1, month = 4, year = 2024)
        episodes = 12
        mediaListEntry = MediaList().apply { status = KeyUtil.CURRENT }
        chapters = 40
        averageScore = 85
    }
}
