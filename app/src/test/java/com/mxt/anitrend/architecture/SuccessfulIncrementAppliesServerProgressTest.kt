package com.mxt.anitrend.architecture

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.medialist.interactor.IncrementMediaProgressInteractor
import com.mxt.anitrend.domain.medialist.interactor.SaveMediaListEntryInteractor
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.fixture.MediaListFixtures.aFuzzyDateRecord
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.fixture.MediaListFixtures.anAnimeMediaBase
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SuccessfulIncrementAppliesServerProgressTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `increment command is built from pure domain record without mutating committed state`() {
        val committedModel = aMediaList(
            id = 7,
            mediaId = 303,
            progress = 11,
            status = KeyUtil.CURRENT,
            media = anAnimeMediaBase(id = 303, episodes = 12),
        )
        val committedRecord = committedModel.toMediaListRecord(revision = 1L, ownerUserId = 42L, ownerUserName = "max")

        val command = buildIncrementMediaProgressCommand(committedRecord, aFuzzyDateRecord(2026, 7, 29))

        assertEquals(11, command.currentProgress)
        assertEquals(12, command.requestedProgress)
        assertEquals(MediaListStatus.COMPLETED, command.status)
        assertEquals(FuzzyDateRecord(2026, 7, 29), command.completedAt)
        assertEquals(11, committedRecord.progress)
        assertEquals(KeyUtil.CURRENT, committedRecord.status)
        assertNull(committedRecord.completedAt)
    }

    @Test
    fun `successful increment commits authoritative server progress to the canonical store`() = runTest {
        val repository = mock(BrowseRepository::class.java)
        val userRepository = mock(UserRepository::class.java)
        val store = InMemoryMediaListStore()
        val committedRecord = aMediaList(id = 7, mediaId = 303, progress = 5).toMediaListRecord(
            revision = 0L,
            ownerUserId = 42L,
            ownerUserName = "max",
        )
        store.apply(MediaListStoreChange.EntryUpserted(committedRecord))
        val command = buildIncrementMediaProgressCommand(committedRecord, aFuzzyDateRecord(2026, 7, 29))

        doReturn(
            User().apply {
                id = 42L
                name = "max"
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
        doReturn(Result.success(aMediaList(id = 7, mediaId = 303, progress = 6)))
            .`when`(repository)
            .saveMediaListEntry(
                id = 7,
                mediaId = 303L,
                status = MediaListStatus.CURRENT,
                scoreRaw = null,
                score = 8.0,
                progress = 6,
                progressVolumes = 0,
                repeat = 0,
                priority = 0,
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
            mutationExecutor = DefaultMutationExecutor(
                applicationScope = backgroundScope,
                keyedMutex = KeyedMutex(backgroundScope),
                mutationRegistry = DefaultMutationRegistry(),
                operationIdGenerator = DefaultOperationIdGenerator(),
                sessionEpoch = SessionEpoch(),
            ),
            mediaListStore = store,
            requestSequence = RequestSequence(),
            userRepository = userRepository,
        )
        val interactor = IncrementMediaProgressInteractor(saveMediaListEntryInteractor = saveInteractor)

        val result = interactor(command)

        assertEquals(MutationResult.Success, result)
        assertEquals(6, store.state.value.entriesById.getValue(7L).progress)
        assertEquals(5, committedRecord.progress)
    }
}
