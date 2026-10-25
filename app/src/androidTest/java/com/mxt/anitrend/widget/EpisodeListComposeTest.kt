@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.crunchy.Episode
import com.mxt.anitrend.model.entity.crunchy.MediaContent
import com.mxt.anitrend.model.entity.crunchy.Thumbnail
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.episode.EpisodeList
import com.mxt.anitrend.ui.episode.EpisodeRowUiState
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EpisodeListComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun episodeRowsExposeTitleAndDurationAndInteractiveSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val episodes =
                listOf(
                    episodeFixture(
                        activity = hostActivity,
                        title = "Compose migration baseline episode",
                        duration = "1421",
                    ),
                    episodeFixture(
                        activity = hostActivity,
                        title = "Episode without duration",
                        duration = "invalid",
                    ),
                )
            var clickedIndex = -1
            var longClickedIndex = -1
            runOnUiThread {
                installEpisodeList(
                    activity = hostActivity,
                    episodes = episodes,
                    onEpisodeClick = { clickedIndex = it },
                    onEpisodeLongClick = { longClickedIndex = it },
                )
            }
            waitForIdle()

            val firstTitle = "Compose migration baseline episode"
            val titleRow = onNodeWithText(firstTitle)
            onAllNodesWithText(firstTitle).assertCountEquals(1)
            onAllNodesWithText("23:41").assertCountEquals(1)
            onAllNodesWithText("00:00").assertCountEquals(1)
            titleRow.assertIsDisplayed().assertHasClickAction()
            titleRow.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            onNodeWithText("23:41").assertIsDisplayed()
            onNodeWithText("00:00").assertIsDisplayed()

            titleRow.performClick()
            assertEquals(0, clickedIndex)
            titleRow.performTouchInput { longClick() }
            assertEquals(0, longClickedIndex)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun episodeNullThumbnailUsesResourceFallbackAndLegacyHeight() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val episode =
                episodeFixture(
                    activity = hostActivity,
                    title = "Episode with missing thumbnail",
                    duration = "1421",
                    thumbnailUrl = null,
                )
            val expectedCenterColor = resourceCenterPixel(hostActivity)
            runOnUiThread {
                installEpisodeList(activity = hostActivity, episodes = listOf(episode))
            }
            waitForIdle()

            val image = onNodeWithTag(EPISODE_IMAGE_TAG, useUnmergedTree = true)
            image.assertIsDisplayed()
            waitUntil("null thumbnail resource fallback is visible", 5_000L) {
                isColorNear(imageCenterPixel(image), expectedCenterColor)
            }

            val expectedHeight = wideImageHeightPixels(hostActivity)
            assertEquals(expectedHeight.toFloat(), image.fetchSemanticsNode().boundsInRoot.height, 1f)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun episodeWhitespaceThumbnailUsesSharedErrorDrawableAndLegacyHeight() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val episode =
                episodeFixture(
                    activity = hostActivity,
                    title = "Episode with blank thumbnail",
                    duration = "1421",
                    thumbnailUrl = " \t ",
                )
            val expectedErrorColor = Color.rgb(255, 224, 74)
            runOnUiThread {
                installEpisodeList(activity = hostActivity, episodes = listOf(episode))
            }
            waitForIdle()

            val image = onNodeWithTag(EPISODE_IMAGE_TAG, useUnmergedTree = true)
            image.assertIsDisplayed()
            waitUntil("blank thumbnail uses the shared error drawable", 5_000L) {
                isColorNear(imageCenterPixel(image), expectedErrorColor)
            }

            val expectedHeight = wideImageHeightPixels(hostActivity)
            assertEquals(expectedHeight.toFloat(), image.fetchSemanticsNode().boundsInRoot.height, 1f)
        }
    }

    private fun episodeFixture(
        activity: ProgressLayoutTestActivity,
        title: String,
        duration: String,
        thumbnailUrl: String? = "android.resource://${activity.packageName}/${R.drawable.reg_bg}",
    ): Episode = Episode(
        title = title,
        link = "https://example.invalid/watch/episode",
        description = "Episode row contract fixture.",
        content = MediaContent(duration = duration),
        thumbnail =
        thumbnailUrl?.let {
            listOf(Thumbnail(url = it, width = 640, height = 360))
        } ?: emptyList(),
    )

    private fun installEpisodeList(
        activity: ProgressLayoutTestActivity,
        episodes: List<Episode>,
        onEpisodeClick: (Int) -> Unit = {},
        onEpisodeLongClick: (Int) -> Unit = {},
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        EpisodeList(
                            items = episodes.mapIndexed { index, episode ->
                                EpisodeRowUiState.from(index, episode)
                            },
                            onEpisodeClick = onEpisodeClick,
                            onEpisodeLongClick = onEpisodeLongClick,
                        )
                    }
                }
            },
        )
    }

    private fun resourceCenterPixel(context: Context): Int {
        val bitmap = requireNotNull(BitmapFactory.decodeResource(context.resources, R.drawable.reg_bg))
        return try {
            bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        } finally {
            bitmap.recycle()
        }
    }

    private fun imageCenterPixel(node: SemanticsNodeInteraction): Int {
        val bitmap = node.captureToImage().asAndroidBitmap()
        return try {
            bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        } finally {
            bitmap.recycle()
        }
    }

    private fun wideImageHeightPixels(context: Context): Int {
        val maximumHeight = context.resources.getDimensionPixelSize(R.dimen.app_bar_height)
        val defaultMargin = context.resources.getDimensionPixelSize(R.dimen.sm_margin)
        return ((maximumHeight - defaultMargin) * 0.95f).toInt()
    }

    private fun isColorNear(
        actualColor: Int,
        expectedColor: Int,
        tolerance: Int = 16,
    ): Boolean = Color.alpha(actualColor) > 240 &&
        abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance

    private companion object {
        const val EPISODE_IMAGE_TAG = "episode-row-image"
    }
}
