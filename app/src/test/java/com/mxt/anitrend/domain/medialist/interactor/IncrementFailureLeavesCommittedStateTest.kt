package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.model.IncrementMediaProgressCommand
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class IncrementFailureLeavesCommittedStateTest {

    @Test
    fun `failed increment leaves committed store state unchanged`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        val committedEntry = aMediaList(id = 7, mediaId = 303, progress = 5)
        store.apply(
            MediaListStoreChange.EntryUpserted(
                committedEntry.toMediaListRecord(revision = 0L, ownerUserId = 77L, ownerUserName = "max"),
            ),
        )

        doReturn(
            User().apply {
                id = 77L
                name = "max"
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
        doReturn(Result.failure<MediaList>(IllegalStateException("increment failed")))
            .`when`(repository)
            .saveMediaListEntry(
                id = 7,
                mediaId = 303L,
                status = MediaListStatus.CURRENT,
                scoreRaw = 80,
                score = 8.0,
                progress = 6,
                progressVolumes = 0,
                repeat = 0,
                priority = 1,
                private = false,
                hiddenFromStatusLists = false,
                customLists = null,
                advancedScores = null,
                notes = null,
                scoreFormat = ScoreFormat.POINT_100,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val saveInteractor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(KeyedMutex(backgroundScope), DefaultMutationRegistry(), DefaultOperationIdGenerator()),
            mediaListStore = store,
            revisionProvider = RevisionProvider(),
            userRepository = userRepository,
        )
        val interactor = IncrementMediaProgressInteractor(saveMediaListEntryInteractor = saveInteractor)

        val result = interactor(command)

        assertTrue(result is MutationResult.Failure)
        assertEquals(5, store.state.value.entriesById.getValue(7L).progress)
        assertEquals(0L, store.state.value.entriesById.getValue(7L).revision)
    }

    private fun createCommand(): IncrementMediaProgressCommand = IncrementMediaProgressCommand(
        id = 7,
        mediaId = 303L,
        currentProgress = 5,
        requestedProgress = 6,
        status = MediaListStatus.CURRENT,
        score = 8.0,
        scoreRaw = 80,
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
