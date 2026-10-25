@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.StudioGrid
import com.mxt.anitrend.ui.media.StudioRowUiState
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

private const val STUDIO_FAVOURITE_TAG = "studio-favourite-marker"

@RunWith(AndroidJUnit4::class)
class StudioGridComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun studioRowsExposeNamesAndOnlyFavouriteRowsShowTheMarker() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val studios =
                listOf(
                    StudioRowUiState(id = 301L, name = "Sunrise", isFavourite = false),
                    StudioRowUiState(id = 302L, name = "Bones", isFavourite = true),
                    StudioRowUiState(id = 303L, name = "Madhouse", isFavourite = false),
                )
            runOnUiThread { installStudioGridHost(hostActivity, studios) }
            waitForIdle()

            onNodeWithText("Sunrise").assertIsDisplayed()
            onNodeWithText("Bones").assertIsDisplayed()
            onNodeWithText("Madhouse").assertIsDisplayed()
            assertEquals(
                1,
                onAllNodesWithTag(STUDIO_FAVOURITE_TAG, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .size,
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun clickingStudioRowInvokesCallbackWithItsId() {
        var clickedStudioId: Long? = null

        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val studioId = 302L
            runOnUiThread {
                installStudioGridHost(
                    activity = hostActivity,
                    items = listOf(StudioRowUiState(id = studioId, name = "Bones", isFavourite = false)),
                    onStudioClick = { clickedStudioId = it },
                )
            }
            waitForIdle()

            onNodeWithText("Bones").performClick()
            assertEquals(studioId, clickedStudioId)
        }
    }

    private fun installStudioGridHost(
        activity: ProgressLayoutTestActivity,
        items: List<StudioRowUiState>,
        onStudioClick: (Long) -> Unit = {},
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        StudioGrid(
                            items = items,
                            modifier = Modifier.fillMaxSize(),
                            onStudioClick = onStudioClick,
                            onLoadMore = {},
                        )
                    }
                }
            },
        )
    }
}
