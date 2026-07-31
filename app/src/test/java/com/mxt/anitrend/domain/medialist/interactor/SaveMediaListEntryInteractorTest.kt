package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListQueryResult
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.medialist.MediaListStoreState
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.mutation.SessionInvalidatedException
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.SaveMediaListEntryCommand
import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SaveMediaListEntryInteractorTest {

    @Test
    fun `successful save commits EntryUpserted to store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        val currentUser = User().apply {
            id = 77L
            name = "max"
            mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
        }
        doReturn(currentUser).`when`(userRepository).cachedCurrentUser
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
                scoreFormat = ScoreFormat.POINT_100,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            mediaListStore = store,
            requestSequence = RequestSequence(),
            userRepository = userRepository,
        )

        val result = interactor(command)

        assertEquals(MutationResult.Success, result)
        assertEquals(9, store.state.value.entriesById.getValue(5L).progress)
        assertEquals(1L, store.state.value.entriesById.getValue(5L).revision)
        assertEquals(77L, store.state.value.entriesById.getValue(5L).ownerUserId)
    }

    @Test
    fun `failed save returns MutationResult Failure and does not commit`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        val failure = IllegalStateException("save failed")
        doReturn(
            User().apply {
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
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
                scoreFormat = ScoreFormat.POINT_100,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            mediaListStore = store,
            requestSequence = RequestSequence(),
            userRepository = userRepository,
        )

        val result = interactor(command)

        assertTrue(result is MutationResult.Failure)
        assertTrue(store.state.value.entriesById.isEmpty())
    }

    @Test
    fun `stale response is rejected by store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = InMemoryMediaListStore()
        val command = createCommand()
        doReturn(
            User().apply {
                id = 77L
                name = "max"
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
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
                scoreFormat = ScoreFormat.POINT_100,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            mediaListStore = store,
            requestSequence = RequestSequence(),
            userRepository = userRepository,
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

    @Test
    fun `session invalidation before commit skips store apply and clears registry`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = RecordingMediaListStore()
        val registry = DefaultMutationRegistry()
        val sessionEpoch = SessionEpoch()
        val command = createCommand()

        doReturn(
            User().apply {
                id = 77L
                name = "max"
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
        doAnswer {
            sessionEpoch.bump()
            MediaListFixtures.aMediaList(id = 5, mediaId = 101, progress = 9)
        }.`when`(repository)
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
                scoreFormat = ScoreFormat.POINT_100,
                startedAt = null,
                completedAt = null,
                commitToStore = false,
                revision = 1L,
            )

        val interactor = SaveMediaListEntryInteractor(
            browseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = registry, operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = sessionEpoch),
            mediaListStore = store,
            requestSequence = RequestSequence(),
            userRepository = userRepository,
        )

        val thrown = try {
            interactor(command)
            null
        } catch (expected: SessionInvalidatedException) {
            expected
        }

        assertNotNull(thrown)
        assertTrue(store.appliedChanges.isEmpty())
        assertTrue(registry.state.value.isEmpty())
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

    private class RecordingMediaListStore : MediaListStore {
        private val mutableState = MutableStateFlow(MediaListStoreState())

        val appliedChanges = mutableListOf<MediaListStoreChange>()

        override val state: StateFlow<MediaListStoreState> = mutableState

        override suspend fun apply(change: MediaListStoreChange) {
            appliedChanges += change
        }

        override suspend fun clear() {
            mutableState.value = MediaListStoreState()
        }

        override fun observeEntryByMediaId(mediaId: Long): Flow<MediaListRecord?> = emptyFlow()

        override fun observeQuery(key: MediaListQueryKey): Flow<MediaListQueryResult> = emptyFlow()
    }
}
