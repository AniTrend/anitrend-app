package com.mxt.anitrend.ui.detail

import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.model.entity.group.RecyclerItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DetailListGridRowsTest {
    @Test
    fun duplicateEntityInDifferentGroupsKeepsScopedKeysAndPageReplayDoesNotAppend() {
        val groupedPage =
            projectGroupedDetailRows(
                listOf(
                    RecyclerHeaderItem("Voice Actor", 1),
                    RecyclerItem(),
                    RecyclerHeaderItem("Music Performer", 1),
                    RecyclerItem(),
                ),
            ) { _, groupKey -> staffRoleRow("$groupKey:character:23:media:42") }

        val roleRows = groupedPage.filterIsInstance<DetailGridRow.StaffCharacterRole>()
        assertEquals(2, roleRows.size)
        assertNotEquals(roleRows[0].key, roleRows[1].key)

        val firstAppend = appendDetailRows(emptyList(), groupedPage)
        assertEquals(2, firstAppend.addedContentCount)
        assertEquals(4, firstAppend.rows.size)

        val replayedPage = appendDetailRows(firstAppend.rows, groupedPage)
        assertEquals(0, replayedPage.addedContentCount)
        assertEquals(firstAppend.rows.size, replayedPage.rows.size)
        assertEquals(firstAppend.rows.map { it.key }, replayedPage.rows.map { it.key })
    }

    @Test
    fun appendingAnotherPageKeepsRepeatedGroupContentUnderItsOriginalHeader() {
        val firstPage =
            listOf(
                DetailGridRow.Header("group-header:voice", "Voice Actor", 1),
                staffRoleRow("voice/role:1"),
                DetailGridRow.Header("group-header:music", "Music Performer", 1),
                staffRoleRow("music/role:2"),
            )
        val secondPage =
            listOf(
                DetailGridRow.Header("group-header:voice", "Voice Actor", 1),
                staffRoleRow("voice/role:3"),
                DetailGridRow.Header("group-header:music", "Music Performer", 1),
                staffRoleRow("music/role:4"),
            )

        val appended = appendDetailRows(firstPage, secondPage)

        assertEquals(
            listOf(
                "group-header:voice",
                "voice/role:1",
                "voice/role:3",
                "group-header:music",
                "music/role:2",
                "music/role:4",
            ),
            appended.rows.map { it.key },
        )
        assertEquals(2, (appended.rows[0] as DetailGridRow.Header).count)
        assertEquals(2, (appended.rows[3] as DetailGridRow.Header).count)
        assertEquals(2, appended.addedContentCount)

        val thirdPage = appendDetailRows(
            appended.rows,
            listOf(DetailGridRow.Header("group-header:voice", "Voice Actor", 1), staffRoleRow("voice/role:5")),
        )
        assertEquals(3, (thirdPage.rows.first() as DetailGridRow.Header).count)
    }

    @Test
    fun actorMediaAndVoiceActorKeysIncludeTheRoleGroup() {
        val media = MediaBase().apply { id = 501L }
        val staff = StaffBase().apply { id = 23L }
        val edges =
            listOf("Main Role", "Supporting Role").map { role ->
                MediaEdge().apply {
                    node = media
                    characterRole = role
                    voiceActors = listOf(staff)
                }
            }

        val rows =
            projectCharacterActorRows(
                edges = edges,
                onMediaClick = { _, _ -> },
                onMediaLongClick = { _, _ -> },
                onStaffClick = { _, _ -> },
            )

        assertEquals(2, rows.filterIsInstance<DetailGridRow.CharacterMediaHeader>().size)
        assertEquals(2, rows.filterIsInstance<DetailGridRow.Staff>().size)
        assertEquals(4, rows.map { it.key }.toSet().size)
    }

    private fun staffRoleRow(key: String) = DetailGridRow.StaffCharacterRole(
        key = key,
        state =
        StaffCharacterRoleUiState(
            characterId = 23L,
            characterName = "Character",
            mediaId = 42L,
            mediaTitle = "Media",
            coverImageUrl = null,
            isFavourite = false,
        ),
        onClick = {},
    )
}
