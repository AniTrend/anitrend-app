package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.model.SaveMediaListEntryCommand
import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.repository.BrowseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SaveMediaListEntryInteractorTest {

    @Test
    fun `successful save commits EntryUpserted to store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        doReturn(Result.success(MediaListFixtures.aMediaList(id = 5, mediaId = 101, progress = 9)))
            .`when`(repository)
            .saveMediaListEntry(
                id = 5,
                mediaId = 101L,
                status = MediaListStatus.CURRENT,
                scoreRaw = 80,
                score = 8.0,
                progress = 9,
                progressVolumes = 0,
                repeat = 0,
                priority = 1,
                private = false,
                hiddenFromStatusLists = false,
                customLists = null,
                advancedScores = null,
                notes = null,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(KeyedMutex(backgroundScope), DefaultMutationRegistry(), DefaultOperationIdGenerator()),
            mediaListStore = store,
            revisionProvider = RevisionProvider(),
        )

        val result = interactor(command)

        assertEquals(MutationResult.Success, result)
        assertEquals(9, store.state.value.entriesById.getValue(5L).progress)
        assertEquals(1L, store.state.value.entriesById.getValue(5L).revision)
    }

    @Test
    fun `failed save returns MutationResult Failure and does not commit`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        val failure = IllegalStateException("save failed")
        doReturn(Result.failure<MediaList>(failure))
            .`when`(repository)
            .saveMediaListEntry(
                id = 5,
                mediaId = 101L,
                status = MediaListStatus.CURRENT,
                scoreRaw = 80,
                score = 8.0,
                progress = 9,
                progressVolumes = 0,
                repeat = 0,
                priority = 1,
                private = false,
                hiddenFromStatusLists = false,
                customLists = null,
                advancedScores = null,
                notes = null,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(KeyedMutex(backgroundScope), DefaultMutationRegistry(), DefaultOperationIdGenerator()),
            mediaListStore = store,
            revisionProvider = RevisionProvider(),
        )

        val result = interactor(command)

        assertTrue(result is MutationResult.Failure)
        assertTrue(store.state.value.entriesById.isEmpty())
    }

    @Test
    fun `stale response is rejected by store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        doReturn(Result.success(MediaListFixtures.aMediaList(id = 5, mediaId = 101, progress = 9)))
            .`when`(repository)
            .saveMediaListEntry(
                id = 5,
                mediaId = 101L,
                status = MediaListStatus.CURRENT,
                scoreRaw = 80,
                score = 8.0,
                progress = 9,
                progressVolumes = 0,
                repeat = 0,
                priority = 1,
                private = false,
                hiddenFromStatusLists = false,
                customLists = null,
                advancedScores = null,
                notes = null,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(KeyedMutex(backgroundScope), DefaultMutationRegistry(), DefaultOperationIdGenerator()),
            mediaListStore = store,
            revisionProvider = RevisionProvider(),
        )

        interactor(command)
        store.apply(
            MediaListStoreChange.EntryUpserted(
                entry = store.state.value.entriesById.getValue(5L).copy(progress = 1, revision = 0L),
            ),
        )

        assertEquals(9, store.state.value.entriesById.getValue(5L).progress)
        assertEquals(1L, store.state.value.entriesById.getValue(5L).revision)
    }

    private fun createCommand(): SaveMediaListEntryCommand = SaveMediaListEntryCommand(
        id = 5,
        mediaId = 101L,
        status = MediaListStatus.CURRENT,
        score = 8.0,
        scoreRaw = 80,
        progress = 9,
        progressVolumes = 0,
        repeat = 0,
        priority = 1,
        isPrivate = false,
        hiddenFromStatusLists = false,
        customLists = null,
        advancedScores = null,
        notes = null,
        startedAt = null,
        completedAt = null,
    )
}
