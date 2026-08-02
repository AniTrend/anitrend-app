package com.mxt.anitrend.architecture

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.domain.model.toSaveMediaListEntryCommand
import com.mxt.anitrend.fixture.MediaListFixtures.aFuzzyDateRecord
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Guards the no-optimistic-mutation rule at the media-list domain boundary: command
 * construction must express the planned transition through the command and the derived
 * save request only, never through the committed record itself.
 */
class FailedIncrementLeavesProgressUnchangedTest {

    @Test
    fun `command construction for a planning entry never mutates the committed record`() {
        val committedModel = aMediaList(
            id = 7,
            mediaId = 303,
            progress = 0,
            status = KeyUtil.PLANNING,
        )
        val committedRecord = committedModel.toMediaListRecord(revision = 3L, ownerUserId = 42L, ownerUserName = "max")
        val currentDate = aFuzzyDateRecord(2026, 7, 29)

        val command = buildIncrementMediaProgressCommand(committedRecord, currentDate)
        val saveCommand = command.toSaveMediaListEntryCommand()

        // The transition is expressed only through the command and the derived request.
        assertEquals(MediaListStatus.CURRENT, command.status)
        assertEquals(1, command.requestedProgress)
        assertEquals(FuzzyDateRecord(2026, 7, 29), command.startedAt)
        assertEquals(FuzzyDateInput(day = 29, month = 7, year = 2026), saveCommand.startedAt)
        assertNull(saveCommand.completedAt)

        // The committed record is never mutated: failure/rollback semantics start from untouched state.
        assertEquals(0, committedRecord.progress)
        assertEquals(KeyUtil.PLANNING, committedRecord.status)
        assertNull(committedRecord.startedAt)
        assertNull(committedRecord.completedAt)
        assertEquals(3L, committedRecord.revision)
    }
}
