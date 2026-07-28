package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.util.KeyUtil
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class ManageSheetLogicTest {

    private val statuses = arrayOf(
        KeyUtil.CURRENT,
        KeyUtil.PLANNING,
        KeyUtil.COMPLETED,
        KeyUtil.DROPPED,
        KeyUtil.PAUSED,
        KeyUtil.REPEATING,
    )

    // -------------------------------------------------------------------------
    // computeStatusSelectionEffects
    // -------------------------------------------------------------------------

    @Test
    fun computeStatusSelectionEffects_animeCurrentNotYetReleased_shouldReturnWarningNotAired() {
        val media = MediaListFixtures.aNotYetReleasedMediaBase()
        val result = computeStatusSelectionEffects(0, statuses, media, isAnime = true)
        assertThat(result!!.newStatus, `is`(KeyUtil.CURRENT))
        assertThat(result.warningResId, `is`(com.mxt.anitrend.R.string.status_warning_not_aired))
        assertThat(result.autoFillProgress, nullValue())
        assertThat(result.autoFillVolumes, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_animeCurrentFinished_shouldReturnNoWarning() {
        val media = MediaListFixtures.anAnimeMediaBase(status = KeyUtil.FINISHED)
        val result = computeStatusSelectionEffects(0, statuses, media, isAnime = true)
        assertThat(result!!.newStatus, `is`(KeyUtil.CURRENT))
        assertThat(result.warningResId, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_animeCompletedFinished_shouldAutoFillEpisodes() {
        val media = MediaListFixtures.anAnimeMediaBase(episodes = 24, status = KeyUtil.FINISHED)
        val result = computeStatusSelectionEffects(2, statuses, media, isAnime = true)
        assertThat(result!!.newStatus, `is`(KeyUtil.COMPLETED))
        assertThat(result.autoFillProgress, `is`(24))
        assertThat(result.autoFillVolumes, nullValue())
        assertThat(result.warningResId, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_animeCompletedReleasing_shouldReturnWarningStillAiring() {
        val media = MediaListFixtures.anAiringMediaBase()
        val result = computeStatusSelectionEffects(2, statuses, media, isAnime = true)
        assertThat(result!!.newStatus, `is`(KeyUtil.COMPLETED))
        assertThat(result.warningResId, `is`(com.mxt.anitrend.R.string.status_warning_still_airing))
        assertThat(result.autoFillProgress, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_animePlanning_shouldReturnNoEffects() {
        val media = MediaListFixtures.anAnimeMediaBase()
        val result = computeStatusSelectionEffects(1, statuses, media, isAnime = true)
        assertThat(result!!.newStatus, `is`(KeyUtil.PLANNING))
        assertThat(result.warningResId, nullValue())
        assertThat(result.autoFillProgress, nullValue())
        assertThat(result.autoFillVolumes, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_animeDroppedNotYetReleased_shouldReturnWarningNotAired() {
        val media = MediaListFixtures.aNotYetReleasedMediaBase()
        val result = computeStatusSelectionEffects(3, statuses, media, isAnime = true)
        assertThat(result!!.newStatus, `is`(KeyUtil.DROPPED))
        assertThat(result.warningResId, `is`(com.mxt.anitrend.R.string.status_warning_not_aired))
    }

    @Test
    fun computeStatusSelectionEffects_animeDroppedFinished_shouldReturnNoWarning() {
        val media = MediaListFixtures.anAnimeMediaBase(status = KeyUtil.FINISHED)
        val result = computeStatusSelectionEffects(3, statuses, media, isAnime = true)
        assertThat(result!!.newStatus, `is`(KeyUtil.DROPPED))
        assertThat(result.warningResId, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_mangaCurrentNotYetReleased_shouldReturnWarningNotPublished() {
        val media = MediaListFixtures.aNotYetReleasedMediaBase(type = KeyUtil.MANGA)
        val result = computeStatusSelectionEffects(0, statuses, media, isAnime = false)
        assertThat(result!!.newStatus, `is`(KeyUtil.CURRENT))
        assertThat(result.warningResId, `is`(com.mxt.anitrend.R.string.status_warning_not_published))
        assertThat(result.autoFillProgress, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_mangaCurrentFinished_shouldReturnNoWarning() {
        val media = MediaListFixtures.aMangaMediaBase(status = KeyUtil.FINISHED)
        val result = computeStatusSelectionEffects(0, statuses, media, isAnime = false)
        assertThat(result!!.newStatus, `is`(KeyUtil.CURRENT))
        assertThat(result.warningResId, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_mangaCompletedFinished_shouldAutoFillChaptersAndVolumes() {
        val media = MediaListFixtures.aMangaMediaBase(chapters = 100, volumes = 10, status = KeyUtil.FINISHED)
        val result = computeStatusSelectionEffects(2, statuses, media, isAnime = false)
        assertThat(result!!.newStatus, `is`(KeyUtil.COMPLETED))
        assertThat(result.autoFillProgress, `is`(100))
        assertThat(result.autoFillVolumes, `is`(10))
        assertThat(result.warningResId, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_mangaCompletedReleasing_shouldReturnWarningStillPublishing() {
        val media = MediaListFixtures.aMangaMediaBase(status = KeyUtil.RELEASING)
        val result = computeStatusSelectionEffects(2, statuses, media, isAnime = false)
        assertThat(result!!.newStatus, `is`(KeyUtil.COMPLETED))
        assertThat(result.warningResId, `is`(com.mxt.anitrend.R.string.status_warning_still_publishing))
        assertThat(result.autoFillProgress, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_mangaPlanning_shouldReturnNoEffects() {
        val media = MediaListFixtures.aMangaMediaBase()
        val result = computeStatusSelectionEffects(1, statuses, media, isAnime = false)
        assertThat(result!!.newStatus, `is`(KeyUtil.PLANNING))
        assertThat(result.warningResId, nullValue())
        assertThat(result.autoFillProgress, nullValue())
        assertThat(result.autoFillVolumes, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_invalidIndexNegative_shouldReturnNull() {
        val media = MediaListFixtures.anAnimeMediaBase()
        val result = computeStatusSelectionEffects(-1, statuses, media, isAnime = true)
        assertThat(result, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_invalidIndexOutOfBounds_shouldReturnNull() {
        val media = MediaListFixtures.anAnimeMediaBase()
        val result = computeStatusSelectionEffects(statuses.size, statuses, media, isAnime = true)
        assertThat(result, nullValue())
    }

    @Test
    fun computeStatusSelectionEffects_mangaDroppedNotYetReleased_shouldReturnWarningNotPublished() {
        val media = MediaListFixtures.aNotYetReleasedMediaBase(type = KeyUtil.MANGA)
        val result = computeStatusSelectionEffects(3, statuses, media, isAnime = false)
        assertThat(result!!.newStatus, `is`(KeyUtil.DROPPED))
        assertThat(result.warningResId, `is`(com.mxt.anitrend.R.string.status_warning_not_published))
    }

    // -------------------------------------------------------------------------
    // validateManageForm
    // -------------------------------------------------------------------------

    @Test
    fun validateManageForm_valid_shouldReturnValid() {
        val result = validateManageForm(progress = 5, repeat = 2)
        assertThat(result.isValid, `is`(true))
        assertThat(result.errorResId, nullValue())
    }

    @Test
    fun validateManageForm_zeroValues_shouldReturnValid() {
        val result = validateManageForm(progress = 0, repeat = 0)
        assertThat(result.isValid, `is`(true))
        assertThat(result.errorResId, nullValue())
    }

    @Test
    fun validateManageForm_negativeProgress_shouldReturnError() {
        val result = validateManageForm(progress = -1, repeat = 0)
        assertThat(result.isValid, `is`(false))
        assertThat(result.errorResId, `is`(com.mxt.anitrend.R.string.validation_progress_negative))
    }

    @Test
    fun validateManageForm_negativeRepeat_shouldReturnError() {
        val result = validateManageForm(progress = 0, repeat = -1)
        assertThat(result.isValid, `is`(false))
        assertThat(result.errorResId, `is`(com.mxt.anitrend.R.string.validation_repeat_negative))
    }

    @Test
    fun validateManageForm_bothNegative_shouldReturnProgressErrorFirst() {
        val result = validateManageForm(progress = -1, repeat = -1)
        assertThat(result.isValid, `is`(false))
        assertThat(result.errorResId, `is`(com.mxt.anitrend.R.string.validation_progress_negative))
    }

    // -------------------------------------------------------------------------
    // shouldDecodeHtml
    // -------------------------------------------------------------------------

    @Test
    fun shouldDecodeHtml_plainText_shouldReturnFalse() {
        assertThat(shouldDecodeHtml("Just plain text"), `is`(false))
    }

    @Test
    fun shouldDecodeHtml_withAmp_shouldReturnTrue() {
        assertThat(shouldDecodeHtml("foo &amp; bar"), `is`(true))
    }

    @Test
    fun shouldDecodeHtml_withNumericEntity_shouldReturnTrue() {
        assertThat(shouldDecodeHtml("foo &#39; bar"), `is`(true))
    }

    @Test
    fun shouldDecodeHtml_ampersandOnly_shouldReturnFalse() {
        assertThat(shouldDecodeHtml("A & B"), `is`(false))
    }

    @Test
    fun shouldDecodeHtml_emptyString_shouldReturnFalse() {
        assertThat(shouldDecodeHtml(""), `is`(false))
    }

    // -------------------------------------------------------------------------
    // buildMediaListFromForm
    // -------------------------------------------------------------------------

    @Test
    fun buildMediaListFromForm_allFieldsSet_shouldPopulateModel() {
        val model = MediaListFixtures.aMediaList()
        val startedAt = MediaListFixtures.aFuzzyDate(2024, 1, 10)
        val completedAt = MediaListFixtures.aFuzzyDate(2024, 6, 20)
        val advancedScores = mapOf("Story" to 9.0f, "Art" to 8.0f)

        val result = buildMediaListFromForm(
            model = model,
            statusIndex = 2, // COMPLETED
            statuses = statuses,
            progress = 24,
            repeat = 1,
            score = 8.5f,
            progressVolumes = 3,
            isAnime = true,
            startedAt = startedAt,
            completedAt = completedAt,
            isHidden = true,
            isHiddenFromStatusLists = true,
            priority = 2,
            notes = "Great series!",
            advancedScores = advancedScores,
        )

        assertThat(result.status, `is`(KeyUtil.COMPLETED))
        assertThat(result.progress, `is`(24))
        assertThat(result.repeat, `is`(1))
        assertThat(result.score, `is`(8.5f))
        assertThat(result.startedAt, `is`(startedAt))
        assertThat(result.completedAt, `is`(completedAt))
        assertThat(result.isHidden, `is`(true))
        assertThat(result.isHiddenFromStatusLists, `is`(true))
        assertThat(result.priority, `is`(2))
        assertThat(result.notes, `is`("Great series!"))
        assertThat(result.advancedScores, `is`(advancedScores))
    }

    @Test
    fun buildMediaListFromForm_isAnime_shouldNotSetProgressVolumes() {
        val model = MediaListFixtures.aMediaList(progressVolumes = 5)
        val result = buildMediaListFromForm(
            model = model,
            statusIndex = 0,
            statuses = statuses,
            progress = 10,
            repeat = 0,
            score = 7f,
            progressVolumes = 3,
            isAnime = true,
            startedAt = null,
            completedAt = null,
            isHidden = false,
            isHiddenFromStatusLists = false,
            priority = 0,
            notes = null,
            advancedScores = null,
        )
        assertThat(result.progressVolumes, `is`(5)) // unchanged from initial
    }

    @Test
    fun buildMediaListFromForm_isAnimeFalse_shouldProgressVolumes() {
        val model = MediaListFixtures.aMediaList(progressVolumes = 0)
        val result = buildMediaListFromForm(
            model = model,
            statusIndex = 0,
            statuses = statuses,
            progress = 10,
            repeat = 0,
            score = 7f,
            progressVolumes = 3,
            isAnime = false,
            startedAt = null,
            completedAt = null,
            isHidden = false,
            isHiddenFromStatusLists = false,
            priority = 0,
            notes = null,
            advancedScores = null,
        )
        assertThat(result.progressVolumes, `is`(3))
    }

    @Test
    fun buildMediaListFromForm_statusIndex_shouldMapCorrectly() {
        val model = MediaListFixtures.aMediaList()
        val result = buildMediaListFromForm(
            model = model,
            statusIndex = 4, // PAUSED
            statuses = statuses,
            progress = 10,
            repeat = 0,
            score = 7f,
            progressVolumes = 0,
            isAnime = true,
            startedAt = null,
            completedAt = null,
            isHidden = false,
            isHiddenFromStatusLists = false,
            priority = 0,
            notes = null,
            advancedScores = null,
        )
        assertThat(result.status, `is`(KeyUtil.PAUSED))
    }

    @Test
    fun buildMediaListFromForm_notesPassedThrough() {
        val model = MediaListFixtures.aMediaList(notes = null)
        val result = buildMediaListFromForm(
            model = model,
            statusIndex = 0,
            statuses = statuses,
            progress = 0,
            repeat = 0,
            score = 0f,
            progressVolumes = 0,
            isAnime = true,
            startedAt = null,
            completedAt = null,
            isHidden = false,
            isHiddenFromStatusLists = false,
            priority = 0,
            notes = "updated notes",
            advancedScores = null,
        )
        assertThat(result.notes, `is`("updated notes"))
    }

    @Test
    fun buildMediaListFromForm_advancedScoresSet() {
        val model = MediaListFixtures.aMediaList(advancedScores = null)
        val advancedScores = mapOf("Story" to 7.5f, "Characters" to 9.0f)
        val result = buildMediaListFromForm(
            model = model,
            statusIndex = 0,
            statuses = statuses,
            progress = 0,
            repeat = 0,
            score = 0f,
            progressVolumes = 0,
            isAnime = true,
            startedAt = null,
            completedAt = null,
            isHidden = false,
            isHiddenFromStatusLists = false,
            priority = 0,
            notes = null,
            advancedScores = advancedScores,
        )
        assertThat(result.advancedScores, `is`(advancedScores))
    }

    @Test
    fun buildMediaListFromForm_advancedScoresNull_shouldNotOverwriteExisting() {
        val existingScores = mapOf("Story" to 5.0f)
        val model = MediaListFixtures.aMediaList(advancedScores = existingScores)
        val result = buildMediaListFromForm(
            model = model,
            statusIndex = 0,
            statuses = statuses,
            progress = 0,
            repeat = 0,
            score = 0f,
            progressVolumes = 0,
            isAnime = true,
            startedAt = null,
            completedAt = null,
            isHidden = false,
            isHiddenFromStatusLists = false,
            priority = 0,
            notes = null,
            advancedScores = null,
        )
        assertThat(result.advancedScores, `is`(existingScores))
    }
}
