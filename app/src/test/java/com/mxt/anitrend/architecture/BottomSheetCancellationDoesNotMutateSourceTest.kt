package com.mxt.anitrend.architecture

import com.mxt.anitrend.domain.model.toDraft
import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.buildMediaListFromForm
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomSheetCancellationDoesNotMutateSourceTest {

    private val statuses = arrayOf(
        KeyUtil.CURRENT,
        KeyUtil.PLANNING,
        KeyUtil.COMPLETED,
        KeyUtil.DROPPED,
        KeyUtil.PAUSED,
        KeyUtil.REPEATING,
    )

    @Test
    fun `cancelling draft edits leaves source media list unchanged`() {
        val source = MediaListFixtures.aMediaList(
            progress = 5,
            notes = "original",
            status = KeyUtil.CURRENT,
        )

        val draft = buildMediaListFromForm(
            draft = source.toDraft(),
            statusIndex = 2,
            statuses = statuses,
            progress = 12,
            repeat = 1,
            score = 9f,
            progressVolumes = 0,
            isAnime = true,
            startedAt = MediaListFixtures.aFuzzyDate(2026, 1, 1),
            completedAt = MediaListFixtures.aFuzzyDate(2026, 7, 29),
            isHidden = true,
            isHiddenFromStatusLists = true,
            priority = 3,
            notes = "edited",
            advancedScores = mapOf("Story" to 9.0f),
            customLists = null,
        )

        assertEquals(KeyUtil.COMPLETED, draft.status)
        assertEquals(12, draft.progress)
        assertEquals(5, source.progress)
        assertEquals(KeyUtil.CURRENT, source.status)
        assertEquals("original", source.notes)
    }
}
