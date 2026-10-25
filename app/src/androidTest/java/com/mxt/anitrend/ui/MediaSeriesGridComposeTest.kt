@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.ui

import android.view.View
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.favourite.FavouriteGridHost
import com.mxt.anitrend.ui.favourite.FavouriteGridSection
import com.mxt.anitrend.ui.media.MediaSeriesGrid
import com.mxt.anitrend.ui.media.StudioRowUiState
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_CARD_TAG
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_COVER_TAG
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_RATING_TAG
import com.mxt.anitrend.ui.mediasearch.MEDIA_SERIES_STATUS_TAG
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState
import com.mxt.anitrend.ui.staff.StaffRowUiState
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaSeriesGridComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun mediaSeriesCardProjectsLegacyFieldsAndRoutesRowClicks() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val media = mediaFixture().apply {
                coverImage = ImageBase(
                    extraLarge = "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}",
                    large = "file:///covers/large.png",
                    medium = null,
                )
            }
            var clicked: MediaSeriesCardUiState? = null

            runOnUiThread {
                installMediaGrid(hostActivity, media) { clicked = it }
            }
            waitForIdle()

            val projection = MediaSeriesCardUiState.from(hostActivity, media)
            assertEquals(
                "android.resource://${hostActivity.packageName}/${R.drawable.reg_bg}",
                projection.coverImage,
            )
            assertEquals(R.color.colorStateBlue, projection.statusColorResource)
            assertEquals("Series Title", projection.title)
            assertEquals(
                String.format(
                    Locale.getDefault(),
                    "%s - %s",
                    "2024",
                    hostActivity.getString(R.string.text_anime_episodes, 12),
                ),
                projection.yearTypeText,
            )
            assertEquals(85, projection.renderModel.averageScore)
            assertEquals(true, projection.hasMediaListEntry)
            assertEquals(KeyUtil.CURRENT, projection.mediaListStatus)

            onNodeWithTag(MEDIA_SERIES_COVER_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_SERIES_STATUS_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_SERIES_RATING_TAG, useUnmergedTree = true).assertIsDisplayed()
            var listStatusVisibility = View.GONE
            runOnUiThread {
                listStatusVisibility = hostActivity.findViewById<View>(R.id.rating_list_status).visibility
            }
            assertEquals(View.VISIBLE, listStatusVisibility)
            onNodeWithText("Series Title").assertIsDisplayed()
            onNodeWithText(projection.yearTypeText).assertIsDisplayed()
            onNodeWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true).performClick()
            assertEquals(501L, clicked?.id)
            assertEquals(KeyUtil.ANIME, clicked?.renderModel?.mediaType)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun favouriteSectionSwitchReplacesMediaAndComposeGridSurfaces() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val selectedSection = mutableStateOf(FavouriteGridSection.ANIME)
            val media = MediaSeriesCardUiState.from(hostActivity, mediaFixture())
            var clicked: MediaSeriesCardUiState? = null
            runOnUiThread {
                installFavouriteGrid(hostActivity, selectedSection, media) { clicked = it }
            }
            waitForIdle()

            onNodeWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true).performClick()
            assertEquals(501L, clicked?.id)
            assertEquals(KeyUtil.ANIME, clicked?.renderModel?.mediaType)
            onNodeWithText("Series Title").assertIsDisplayed()

            runOnUiThread { selectedSection.value = FavouriteGridSection.CHARACTERS }
            waitForIdle()
            onAllNodesWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true).assertCountEquals(0)
            onNodeWithText("Favourite Character").assertIsDisplayed()

            runOnUiThread { selectedSection.value = FavouriteGridSection.STAFF }
            waitForIdle()
            onNodeWithText("Favourite Staff").assertIsDisplayed()

            runOnUiThread { selectedSection.value = FavouriteGridSection.STUDIOS }
            waitForIdle()
            onNodeWithText("Favourite Studio").assertIsDisplayed()

            runOnUiThread { selectedSection.value = FavouriteGridSection.MANGA }
            waitForIdle()
            onNodeWithTag(MEDIA_SERIES_CARD_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Series Title").assertIsDisplayed()
        }
    }

    private fun installMediaGrid(
        activity: ProgressLayoutTestActivity,
        media: MediaBase,
        onMediaClick: (MediaSeriesCardUiState) -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        MediaSeriesGrid(
                            items = listOf(MediaSeriesCardUiState.from(activity, media)),
                            listState = rememberLazyGridState(),
                            onMediaClick = onMediaClick,
                            onMediaLongClick = {},
                            onLoadMore = {},
                        )
                    }
                }
            },
        )
    }

    private fun installFavouriteGrid(
        activity: ProgressLayoutTestActivity,
        selectedSection: MutableState<FavouriteGridSection>,
        media: MediaSeriesCardUiState,
        onMediaClick: (MediaSeriesCardUiState) -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        FavouriteGridHost(
                            section = selectedSection.value,
                            mediaItems = listOf(media),
                            characterItems = listOf(
                                CharacterRowUiState(
                                    id = 2L,
                                    name = "Favourite Character",
                                    imageUrl = null,
                                    isFavourite = false,
                                ),
                            ),
                            staffItems = listOf(
                                StaffRowUiState(
                                    id = 3L,
                                    name = "Favourite Staff",
                                    image = null,
                                    language = null,
                                    isFavourite = false,
                                ),
                            ),
                            studioItems = listOf(
                                StudioRowUiState(id = 4L, name = "Favourite Studio", isFavourite = false),
                            ),
                            gridState = rememberLazyGridState(),
                            mediaColumnsResource = R.integer.grid_giphy_x3,
                            onMediaClick = onMediaClick,
                            onMediaLongClick = {},
                            onCharacterClick = {},
                            onStaffClick = {},
                            onStudioClick = {},
                            onLoadMore = {},
                        )
                    }
                }
            },
        )
    }

    private fun mediaFixture(): MediaBase = MediaBase().apply {
        id = 501L
        type = KeyUtil.ANIME
        title = MediaTitle(null, null, null, "Series Title")
        status = KeyUtil.RELEASING
        format = KeyUtil.TV
        startDate = FuzzyDate(day = 1, month = 4, year = 2024)
        episodes = 12
        mediaListEntry = MediaList().apply { status = KeyUtil.CURRENT }
        averageScore = 85
    }
}
