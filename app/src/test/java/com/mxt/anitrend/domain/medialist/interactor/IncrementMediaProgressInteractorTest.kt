package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.model.IncrementMediaProgressCommand
import com.mxt.anitrend.fixture.MediaListFixtures
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
class IncrementMediaProgressInteractorTest {

    @Test
    fun `successful increment commits updated entry to store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        doReturn(
            User().apply {
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
        doReturn(Result.success(MediaListFixtures.aMediaList(id = 7, mediaId = 303, progress = 6)))
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
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            mediaListStore = store,
            requestSequence = RequestSequence(),
            userRepository = userRepository,
        )
        val interactor = IncrementMediaProgressInteractor(saveMediaListEntryInteractor = saveInteractor)

        val result = interactor(command)

        assertEquals(MutationResult.Success, result)
        assertEquals(6, store.state.value.entriesById.getValue(7L).progress)
    }

    @Test
    fun `failed increment returns Failure and does not commit`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        val failure = IllegalStateException("increment failed")
        doReturn(
            User().apply {
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
        doReturn(Result.failure<MediaList>(failure))
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
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            mediaListStore = store,
            requestSequence = RequestSequence(),
            userRepository = userRepository,
        )
        val interactor = IncrementMediaProgressInteractor(saveMediaListEntryInteractor = saveInteractor)

        val result = interactor(command)

        assertTrue(result is MutationResult.Failure)
        assertTrue(store.state.value.entriesById.isEmpty())
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
