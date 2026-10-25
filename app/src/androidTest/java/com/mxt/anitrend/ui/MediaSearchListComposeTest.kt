package com.mxt.anitrend.ui

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_CARD_TAG
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_COVER_TAG
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_RATING_TAG
import com.mxt.anitrend.ui.mediasearch.MediaSearchPagingList
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migrated row contracts for the retired MediaSearchAdapter binding of
 * adapter_series: title, the composed year/type caption rules, the rating
 * overlay host, and click/long-click routing.
 */
@RunWith(AndroidJUnit4::class)
class MediaSearchListComposeTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun cardsProjectTitleAndYearTypeCaption() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installCards(
                    activity = hostActivity,
                    items =
                    listOf(
                        item(id = 1L, title = "Series One", mediaType = KeyUtil.ANIME, mediaFormat = "TV", episodes = 12),
                        item(id = 2L, title = "Series Two", mediaType = KeyUtil.ANIME, mediaFormat = KeyUtil.MOVIE, episodes = 0),
                        item(id = 3L, title = "Series Three", mediaType = KeyUtil.MANGA, mediaFormat = "MANGA", chapters = 42),
                        item(id = 4L, title = "Series Four", mediaType = KeyUtil.ANIME, mediaFormat = "TV", episodes = 0, validDate = false),
                    ),
                )
            }
            waitForIdle()

            onNodeWithText("Series One").assertExists()
            onNodeWithText(caption("2024", context.getString(R.string.text_anime_episodes, 12))).assertExists()
            onNodeWithText(caption("2024", CompatUtil.capitalizeWords(KeyUtil.MOVIE))).assertExists()
            onNodeWithText(caption("2024", context.getString(R.string.text_manga_chapters, 42))).assertExists()
            onNodeWithText(
                caption(context.getString(R.string.tba_placeholder), CompatUtil.capitalizeWords("TV")),
            ).assertExists()
            onAllNodesWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true).assertCountEquals(4)
            onAllNodesWithTag(MEDIA_SERIES_RATING_TAG, useUnmergedTree = true).assertCountEquals(4)
            // Coverless rows must still reserve the legacy AspectImageView bounds.
            val cover = onAllNodesWithTag(MEDIA_SERIES_COVER_TAG, useUnmergedTree = true)[0]
                .fetchSemanticsNode()
            val coverBounds = cover.boundsInRoot
            assertTrue(
                "cover reserves the legacy aspect height",
                abs(coverBounds.height - coverBounds.width * KeyUtil.AspectRatio) <= 2f,
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun cardClickAndLongClickRouteToConsumers() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            var clicked = -1L
            var longClicked = -1L
            runOnUiThread {
                installCards(
                    activity = hostActivity,
                    items = listOf(item(id = 9L)),
                    onMediaClick = { clicked = it.id },
                    onMediaLongClick = { longClicked = it.id },
                )
            }
            waitForIdle()

            onNodeWithTag(MEDIA_SERIES_CARD_TAG).performClick()
            assertEquals(9L, clicked)
            assertEquals(-1L, longClicked)

            onNodeWithTag(MEDIA_SERIES_CARD_TAG).performTouchInput { longClick() }
            assertEquals(9L, longClicked)
        }
    }

    private fun caption(year: String, typeText: String): String = String.format(Locale.getDefault(), "%s - %s", year, typeText)

    private fun installCards(
        activity: ProgressLayoutTestActivity,
        items: List<MediaSearchItemUiModel>,
        onMediaClick: (MediaSearchItemUiModel) -> Unit = {},
        onMediaLongClick: (MediaSearchItemUiModel) -> Unit = {},
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        val lazyPagingItems = remember { flowOf(PagingData.from(items)) }
                            .collectAsLazyPagingItems()
                        MediaSearchPagingList(
                            lazyPagingItems = lazyPagingItems,
                            onMediaClick = onMediaClick,
                            onMediaLongClick = onMediaLongClick,
                        )
                    }
                }
            },
        )
    }

    private fun item(
        id: Long,
        title: String = "Series One",
        mediaType: String = KeyUtil.ANIME,
        mediaFormat: String = "TV",
        episodes: Int = 0,
        chapters: Int = 0,
        validDate: Boolean = true,
    ): MediaSearchItemUiModel = MediaSearchItemUiModel(
        id = id,
        title = title,
        titleEnglish = null,
        titleOriginal = null,
        coverImage = null,
        mediaType = mediaType,
        mediaFormat = mediaFormat,
        mediaStatus = KeyUtil.RELEASING,
        mediaEpisodes = episodes,
        mediaChapters = chapters,
        mediaVolumes = 0,
        mediaStartDate = if (validDate) FuzzyDateRecord(day = 1, month = 1, year = 2024) else null,
        averageScore = 85,
        isFavourite = true,
    )
}
