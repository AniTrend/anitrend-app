package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaListStoreTest {
    private val currentAnimeQuery = MediaListQueryKey(
        userId = 1L,
        userName = null,
        mediaType = MediaType.ANIME,
        statuses = setOf(MediaListStatus.CURRENT),
        sort = MediaListSort.PROGRESS_DESC,
    )

    @Test
    fun `entry upsert updates entriesById and entryIdByMediaId`() = runTest {
        val store = InMemoryMediaListStore()
        val entry = createEntry(id = 10L, mediaId = 100L, status = "CURRENT", revision = 1L)

        store.apply(MediaListStoreChange.EntryUpserted(entry))

        val state = store.state.value
        assertEquals(entry.copy(id = 10L), state.entriesById.getValue(10L))
        assertEquals(10L, state.entryIdByMediaId.getValue(100L))
    }

    @Test
    fun `entry delete removes from entriesById entryIdByMediaId and all queries`() = runTest {
        val store = InMemoryMediaListStore()
        val entry = createEntry(id = 10L, mediaId = 100L, status = "CURRENT", revision = 1L)
        store.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = currentAnimeQuery,
                token = 1L,
                entries = listOf(entry),
                pageInfo = createPageInfo(1),
            ),
        )

        store.apply(MediaListStoreChange.EntryDeleted(entryId = 10L, mediaId = 100L, revision = 2L))

        val state = store.state.value
        assertFalse(state.entriesById.containsKey(10L))
        assertFalse(state.entryIdByMediaId.containsKey(100L))
        assertFalse(state.queries.getValue(currentAnimeQuery).orderedEntryIds.contains(10L))
    }

    @Test
    fun `collection loaded merges pages correctly`() = runTest {
        val store = InMemoryMediaListStore()
        store.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = currentAnimeQuery,
                token = 1L,
                entries = listOf(createEntry(10L, 100L, "CURRENT", 1L), createEntry(11L, 101L, "CURRENT", 1L)),
                pageInfo = createPageInfo(1),
            ),
        )

        store.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = currentAnimeQuery,
                token = 1L,
                entries = listOf(createEntry(12L, 102L, "CURRENT", 1L), createEntry(11L, 101L, "CURRENT", 2L)),
                pageInfo = createPageInfo(2),
            ),
        )

        val snapshot = store.state.value.queries.getValue(currentAnimeQuery)
        assertEquals(listOf(10L, 11L, 12L), snapshot.orderedEntryIds)
        assertEquals(setOf(1, 2), snapshot.loadedPages)
    }

    @Test
    fun `stale revisions are rejected`() = runTest {
        val store = InMemoryMediaListStore()
        store.apply(MediaListStoreChange.EntryUpserted(createEntry(10L, 100L, "CURRENT", 5L, progress = 9)))

        store.apply(MediaListStoreChange.EntryUpserted(createEntry(10L, 100L, "CURRENT", 4L, progress = 1)))

        assertEquals(9, store.state.value.entriesById.getValue(10L).progress)
    }

    @Test
    fun `query membership entry changing status removes from non matching queries`() = runTest {
        val store = InMemoryMediaListStore()
        val entry = createEntry(10L, 100L, "CURRENT", 1L)
        store.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = currentAnimeQuery,
                token = 1L,
                entries = listOf(entry),
                pageInfo = createPageInfo(1),
            ),
        )

        store.apply(MediaListStoreChange.EntryUpserted(entry.copy(status = "COMPLETED", revision = 2L)))

        val snapshot = store.state.value.queries.getValue(currentAnimeQuery)
        assertTrue(snapshot.orderedEntryIds.isEmpty())

        store.apply(MediaListStoreChange.EntryUpserted(entry.copy(status = "CURRENT", revision = 3L)))

        val restoredSnapshot = store.state.value.queries.getValue(currentAnimeQuery)
        assertEquals(listOf(10L), restoredSnapshot.orderedEntryIds)
    }

    private fun createEntry(
        id: Long,
        mediaId: Long,
        status: String,
        revision: Long,
        progress: Int = 5,
    ): MediaListRecord = MediaListRecord(
        id = id,
        mediaId = mediaId,
        status = status,
        score = 8.0,
        scoreRaw = 80,
        progress = progress,
        progressVolumes = 0,
        repeat = 0,
        priority = 1,
        `private` = false,
        hiddenFromStatusLists = false,
        customLists = emptyList(),
        advancedScores = emptyMap(),
        notes = null,
        startedAt = null,
        completedAt = null,
        media = MediaSummaryRecord(
            id = mediaId,
            titleUserPreferred = "Title $mediaId",
            titleRomaji = "Title $mediaId",
            titleEnglish = null,
            titleOriginal = null,
            coverImage = null,
            type = "ANIME",
            episodes = 12,
            chapters = 0,
            volumes = 0,
            status = "RELEASING",
            siteUrl = null,
        ),
        revision = revision,
        ownerUserId = 1L,
    )

    private fun createPageInfo(currentPage: Int): PageInfoRecord = PageInfoRecord(
        currentPage = currentPage,
        lastPage = 2,
        perPage = 10,
        total = 20,
        hasNextPage = currentPage < 2,
        hasPreviousPage = currentPage > 1,
    )
}
