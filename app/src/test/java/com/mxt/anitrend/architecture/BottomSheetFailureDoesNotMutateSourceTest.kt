package com.mxt.anitrend.architecture

import com.mxt.anitrend.domain.model.toDraft
import com.mxt.anitrend.domain.model.toSaveMediaListEntryCommand
import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.buildMediaListFromForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BottomSheetFailureDoesNotMutateSourceTest {

    private val statuses = arrayOf(
        KeyUtil.CURRENT,
        KeyUtil.PLANNING,
        KeyUtil.COMPLETED,
        KeyUtil.DROPPED,
        KeyUtil.PAUSED,
        KeyUtil.REPEATING,
    )

    @Test
    fun `failed save leaves source media list unchanged`() {
        val source = MediaListFixtures.aMediaList(
            progress = 5,
            notes = "original",
            status = KeyUtil.CURRENT,
        )

        val draft = buildMediaListFromForm(
            draft = source.toDraft(),
            statusIndex = 4,
            statuses = statuses,
            progress = 9,
            repeat = 2,
            score = 8f,
            progressVolumes = 0,
            isAnime = true,
            startedAt = MediaListFixtures.aFuzzyDate(2026, 2, 1),
            completedAt = null,
            isHidden = false,
            isHiddenFromStatusLists = true,
            priority = 4,
            notes = "edited",
            advancedScores = mapOf("Story" to 8.0f),
        )
        val command = draft.toSaveMediaListEntryCommand(source, customLists = listOf("Favorites"))
        val result = Result.failure<MediaList>(IllegalStateException("save failed"))

        result.exceptionOrNull()

        assertEquals(KeyUtil.PAUSED, draft.status)
        assertEquals(9, command.progress)
        assertEquals(KeyUtil.CURRENT, source.status)
        assertEquals(5, source.progress)
        assertEquals("original", source.notes)
        assertNull(source.completedAt)
    }
}
