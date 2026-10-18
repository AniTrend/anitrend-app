@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.user.model.UserStatisticsRecord
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.profile.ProfileStatsRow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class ProfileStatsRowComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun profileStatsExposeAccessibleActionsAndFormattedValues() {
        val mediaTypeRequests = CopyOnWriteArrayList<String>()
        val snackbarValues = CopyOnWriteArrayList<String>()
        val stats = mutableStateOf<UserStatisticsRecord?>(null)

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ProfileStatsRow(
                                        stats = stats.value,
                                        onAnimeTotalClick = { mediaTypeRequests.add(KeyUtil.ANIME) },
                                        onAnimeTimeClick = { snackbarValues.add(it) },
                                        onMangaTotalClick = { mediaTypeRequests.add(KeyUtil.MANGA) },
                                        onMangaChaptersClick = { snackbarValues.add(it) },
                                    )
                                }
                            }
                        }
                    },
                )
            }
            waitForIdle()

            val animeLabel = hostActivity.getString(R.string.anime)
            val animeTimeLabel = hostActivity.getString(R.string.anime_time)
            val mangaLabel = hostActivity.getString(R.string.manga)
            val chaptersLabel = hostActivity.getString(R.string.chapters)
            onNodeWithContentDescription(animeLabel).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(animeTimeLabel).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(mangaLabel).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(chaptersLabel).assertIsDisplayed().assertHasClickAction()
            onAllNodesWithText("..").assertCountEquals(4)
            onNodeWithContentDescription(animeTimeLabel).performClick()
            runOnIdle { assertEquals(emptyList<String>(), snackbarValues.toList()) }
            onNodeWithText("1.2 K").assertDoesNotExist()

            runOnUiThread {
                stats.value =
                    UserStatisticsRecord(
                        anime = UserStatisticsRecord.Anime(count = 1_234, minutesWatched = 1_234),
                        manga = UserStatisticsRecord.Manga(count = 50, chaptersRead = 1_001),
                    )
            }
            waitForIdle()

            onNodeWithText("1.2 K").assertIsDisplayed()
            onNodeWithText("20.6 Hours").assertIsDisplayed()
            onNodeWithText("50").assertIsDisplayed()
            onNodeWithText("1.0 K").assertIsDisplayed()
            onNodeWithContentDescription(animeLabel).performClick()
            onNodeWithContentDescription(animeTimeLabel).performClick()
            onNodeWithContentDescription(mangaLabel).performClick()
            onNodeWithContentDescription(chaptersLabel).performClick()
            runOnIdle {
                assertEquals(listOf(KeyUtil.ANIME, KeyUtil.MANGA), mediaTypeRequests.toList())
                assertEquals(listOf("20.6 Hours", "1.0 K"), snackbarValues.toList())
            }

            runOnUiThread {
                stats.value =
                    UserStatisticsRecord(
                        anime = UserStatisticsRecord.Anime(count = 1_000, minutesWatched = 60),
                        manga = UserStatisticsRecord.Manga(count = 999, chaptersRead = 1_000),
                    )
            }
            waitForIdle()
            onNodeWithText("1.0 K").assertIsDisplayed()
            onNodeWithText("1.0 Hours").assertIsDisplayed()
            onNodeWithText("999").assertIsDisplayed()
            onNodeWithText("1000").assertIsDisplayed()
            runOnUiThread {
                stats.value =
                    UserStatisticsRecord(
                        anime = UserStatisticsRecord.Anime(minutesWatched = 3_660),
                        manga = UserStatisticsRecord.Manga(),
                    )
            }
            waitForIdle()
            onNodeWithText("2.5 Days").assertIsDisplayed()

            runOnUiThread {
                stats.value =
                    UserStatisticsRecord(
                        anime = UserStatisticsRecord.Anime(minutesWatched = 527_040),
                        manga = UserStatisticsRecord.Manga(),
                    )
            }
            waitForIdle()
            onNodeWithText("1.0 Years").assertIsDisplayed()
        }
    }
}
