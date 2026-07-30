package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListQueryKey
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RevisionProvider
import com.mxt.anitrend.domain.medialist.interactor.SaveMediaListEntryInteractor
import com.mxt.anitrend.domain.model.SaveMediaListEntryCommand
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class CrossScreenConvergenceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var store: InMemoryMediaListStore
    private lateinit var repository: BrowseRepository
    private lateinit var userRepository: UserRepository

    private val queryKey = MediaListQueryKey(
        userId = 42L,
        userName = null,
        mediaType = MediaType.ANIME,
        statuses = setOf(MediaListStatus.CURRENT),
        sort = MediaListSort.PROGRESS_DESC,
    )

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        store = InMemoryMediaListStore()
        repository = mock(BrowseRepository::class.java)
        userRepository = mock(UserRepository::class.java)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `save interactor updates store query for multiple observers`() = runTest(testDispatcher) {
        val savedEntry = aMediaList(id = 5, mediaId = 101, progress = 9)

        store.apply(
            MediaListStoreChange.CollectionLoaded(
                queryKey = queryKey,
                entries = emptyList(),
                pageInfo = null,
            ),
        )
        doReturn(
            User().apply {
                id = 42L
                name = "max"
                mediaListOptions.scoreFormat = ScoreFormat.POINT_100.name
            },
        ).`when`(userRepository).cachedCurrentUser
        doReturn(Result.success(savedEntry))
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
            mutationExecutor = DefaultMutationExecutor(KeyedMutex(backgroundScope), DefaultMutationRegistry(), DefaultOperationIdGenerator()),
            mediaListStore = store,
            revisionProvider = RevisionProvider(),
            userRepository = userRepository,
        )
        val observerOne = async { store.observeQuery(queryKey).drop(1).first() }
        val observerTwo = async { store.observeQuery(queryKey).drop(1).first() }
        advanceUntilIdle()

        val result = interactor(
            SaveMediaListEntryCommand(
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
            ),
        )
        advanceUntilIdle()

        assertEquals(MutationResult.Success, result)
        assertEquals(1, store.observeQuery(queryKey).first().entries.size)
        assertTrue(observerOne.await().entries.size == 1)
        assertEquals(listOf(101L), observerTwo.await().entries.map { it.mediaId })
    }
}
