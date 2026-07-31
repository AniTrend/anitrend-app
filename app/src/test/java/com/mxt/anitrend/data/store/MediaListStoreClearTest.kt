package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.medialist.MediaListStoreState
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaListStoreClearTest {
    private val queryKey = MediaListQueryKey(
        userId = 1L,
        userName = null,
        mediaType = MediaType.ANIME,
        statuses = setOf(MediaListStatus.CURRENT),
        sort = MediaListSort.PROGRESS_DESC,
    )

    @Test
    fun `given populated store when clear called then state and deletion revisions reset`() = runTest {
        val store = InMemoryMediaListStore()
        val entry = createEntry(id = 10L, mediaId = 100L, status = "CURRENT", revision = 1L)

        store.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                token = 1L,
                entries = listOf(entry),
                pageInfo = createPageInfo(1),
            ),
        )
        store.apply(MediaListStoreChange.EntryDeleted(entryId = 10L, mediaId = 100L, revision = 5L))

        store.clear()

        assertEquals(MediaListStoreState(), store.state.value)

        store.apply(MediaListStoreChange.EntryUpserted(entry))

        assertTrue(store.state.value.entriesById.containsKey(10L))
    }

    private fun createEntry(
        id: Long,
        mediaId: Long,
        status: String,
        revision: Long,
    ): MediaListRecord = MediaListRecord(
        id = id,
        mediaId = mediaId,
        status = status,
        score = 8.0,
        scoreRaw = 80,
        progress = 5,
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
        lastPage = 1,
        perPage = 10,
        total = 10,
        hasNextPage = false,
        hasPreviousPage = false,
    )
}
