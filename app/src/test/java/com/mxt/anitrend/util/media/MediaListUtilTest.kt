package com.mxt.anitrend.util.media

import android.os.Bundle
import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.util.KeyUtil
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [MediaListUtil].
 *
 * Note: [Bundle] is a stub in pure JVM tests. With
 * `isReturnDefaultValues = true`, methods like `putString` and `containsKey`
 * return default values without storing data. Therefore, tests for
 * [getMediaListParams] verify that the method does not throw and returns
 * a non-null Bundle. The actual Bundle content is exercised by Android
 * instrumentation tests.
 */
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class MediaListUtilTest {

    // -------------------------------------------------------------------------
    // getMediaListParams - smoke tests
    // -------------------------------------------------------------------------

    @Test
    fun getMediaListParams_withValidModel_shouldReturnNonNullBundle() {
        val model = MediaListFixtures.aMediaList()
        val params = MediaListUtil.getMediaListParams(model, KeyUtil.POINT_100)
        assertThat(params, notNullValue())
    }

    @Test
    fun getMediaListParams_withNullScoreRaw_shouldNotThrow() {
        val model = MediaListFixtures.aMediaList(scoreRaw = null)
        MediaListUtil.getMediaListParams(model, KeyUtil.POINT_100)
        // If we reach here without exception, the null-safety guard works
    }

    @Test
    fun getMediaListParams_withNullAdvancedScores_shouldNotThrow() {
        val model = MediaListFixtures.aMediaList(advancedScores = null)
        MediaListUtil.getMediaListParams(model, KeyUtil.POINT_100)
    }

    @Test
    fun getMediaListParams_withCustomLists_shouldNotThrow() {
        val model = MediaListFixtures.aMediaList()
        model.customLists = emptyList()
        MediaListUtil.getMediaListParams(model, KeyUtil.POINT_100)
    }

    @Test
    fun getMediaListParams_withNullNotes_shouldNotThrow() {
        val model = MediaListFixtures.aMediaList(notes = null)
        MediaListUtil.getMediaListParams(model, KeyUtil.POINT_100)
    }

    @Test
    fun getMediaListParams_withScoreFormatPoint10Decimal_shouldNotThrow() {
        val model = MediaListFixtures.aMediaList()
        MediaListUtil.getMediaListParams(model, KeyUtil.POINT_10_DECIMAL)
    }

    // -------------------------------------------------------------------------
    // isTitleSort
    // -------------------------------------------------------------------------

    @Test
    fun isTitleSort_withTitle_shouldReturnTrue() {
        assertThat(MediaListUtil.isTitleSort(KeyUtil.TITLE), `is`(true))
    }

    @Test
    fun isTitleSort_withScore_shouldReturnFalse() {
        assertThat(MediaListUtil.isTitleSort(KeyUtil.SCORE), `is`(false))
    }

    // -------------------------------------------------------------------------
    // isProgressUpdatable
    // -------------------------------------------------------------------------

    @Test
    fun isProgressUpdatable_nullNextAiringEpisode_shouldReturnFalse() {
        val media = MediaListFixtures.aMediaList().apply {
            this.media = MediaListFixtures.anAnimeMediaBase().apply {
                nextAiringEpisode = null
            }
        }
        assertThat(MediaListUtil.isProgressUpdatable(media), `is`(false))
    }

    @Test
    fun isProgressUpdatable_episodeMinusProgressGteOne_shouldReturnTrue() {
        val media = MediaListFixtures.aMediaList(progress = 5).apply {
            this.media = MediaListFixtures.anAnimeMediaBase().apply {
                nextAiringEpisode = AiringSchedule(episode = 7)
            }
        }
        assertThat(MediaListUtil.isProgressUpdatable(media), `is`(true))
    }

    @Test
    fun isProgressUpdatable_episodeMinusProgressLtOne_shouldReturnFalse() {
        val media = MediaListFixtures.aMediaList(progress = 6).apply {
            this.media = MediaListFixtures.anAnimeMediaBase().apply {
                nextAiringEpisode = AiringSchedule(episode = 6)
            }
        }
        assertThat(MediaListUtil.isProgressUpdatable(media), `is`(false))
    }

    // -------------------------------------------------------------------------
    // isFilterMatch
    // -------------------------------------------------------------------------

    @Test
    fun isFilterMatch_englishTitleContainsFilter_shouldReturnTrue() {
        val model = aMediaListWithTitle(english = "Fullmetal Alchemist")
        assertThat(MediaListUtil.isFilterMatch(model, "metal"), `is`(true))
    }

    @Test
    fun isFilterMatch_romajiTitleContainsFilter_shouldReturnTrue() {
        val model = aMediaListWithTitle(romaji = "Hagane no Renkinjutsushi")
        assertThat(MediaListUtil.isFilterMatch(model, "hagane"), `is`(true))
    }

    @Test
    fun isFilterMatch_noTitleMatches_shouldReturnFalse() {
        val model = aMediaListWithTitle(english = "Naruto", romaji = "Naruto")
        assertThat(MediaListUtil.isFilterMatch(model, "one piece"), `is`(false))
    }

    @Test
    fun isFilterMatch_lowercaseFilterMatchesEnglishTitle_shouldReturnTrue() {
        // isFilterMatch lowercases the title but NOT the filter.
        // The caller is expected to pass an already-lowercased filter.
        val model = aMediaListWithTitle(english = "Attack on Titan")
        assertThat(MediaListUtil.isFilterMatch(model, "attack"), `is`(true))
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun aMediaListWithTitle(
        english: String? = null,
        romaji: String? = null,
        original: String? = null,
    ): MediaList {
        val title = MediaTitle(
            romajiRaw = romaji,
            englishRaw = english,
            originalRaw = original,
            userPreferredRaw = english ?: romaji,
        )
        val media = MediaListFixtures.anAnimeMediaBase().apply { this.title = title }
        return MediaListFixtures.aMediaList(media = media)
    }
}
