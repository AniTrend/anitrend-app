package com.mxt.anitrend.architecture

import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.repository.UserRepository
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class RapidProgressIncrementsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var baseRepository: BaseRepository
    private lateinit var browseRepository: BrowseRepository
    private lateinit var userRepository: UserRepository
    private lateinit var feedRepository: FeedRepository
    private lateinit var databaseHelper: DatabaseHelper

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        baseRepository = mock(BaseRepository::class.java)
        browseRepository = mock(BrowseRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        feedRepository = mock(FeedRepository::class.java)
        databaseHelper = mock(DatabaseHelper::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Suppress("UNCHECKED_CAST")
    @Ignore("Architectural regression: Phase 4 introduces per-resource serialisation and revision rejection")
    @Test
    fun `given rapid increments when responses return out of order then stale response wins`() = runTest {
        // Defect baseline from docs/architecture/state-synchronization-and-mutation-refactor.md,
        // Phase 4: the coordinator has no per-resource serialisation, so an older response can
        // arrive later and overwrite the newer widget state.
        val entry = MediaListFixtures.aMediaList(id = 1, mediaId = 100, progress = 5)
        val continuations = linkedMapOf<Int, Continuation<Result<MediaList>>>()

        doAnswer { invocation ->
            val continuation = invocation.arguments.last() as Continuation<Result<MediaList>>
            continuations[6] = continuation
            COROUTINE_SUSPENDED
        }.
            `when`(browseRepository)
            .saveMediaListEntry(
                id = 1,
                mediaId = 100L,
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
                scoreFormat = com.mxt.anitrend.graphql.generated.ScoreFormat.POINT_100,
                startedAt = null,
                completedAt = null,
            )
        doAnswer { invocation ->
            val continuation = invocation.arguments.last() as Continuation<Result<MediaList>>
            continuations[7] = continuation
            COROUTINE_SUSPENDED
        }.
            `when`(browseRepository)
            .saveMediaListEntry(
                id = 1,
                mediaId = 100L,
                status = MediaListStatus.CURRENT,
                scoreRaw = null,
                score = 8.0,
                progress = 7,
                progressVolumes = 0,
                repeat = 0,
                priority = 0,
                private = false,
                hiddenFromStatusLists = false,
                customLists = null,
                advancedScores = null,
                notes = null,
                scoreFormat = com.mxt.anitrend.graphql.generated.ScoreFormat.POINT_100,
                startedAt = null,
                completedAt = null,
            )

        val coordinator = WidgetMutationCoordinator(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            userRepository = userRepository,
            feedRepository = feedRepository,
            coroutineScope = this,
            ioDispatcher = testDispatcher,
            mainDispatcher = testDispatcher,
            databaseHelper = databaseHelper,
        )
        val observedProgresses = mutableListOf<Int>()
        var finalProgress: Int? = null

        coordinator.saveMediaListEntry(
            id = entry.id.toInt(),
            mediaId = entry.mediaId,
            status = MediaListStatus.CURRENT,
            score = entry.score.toDouble(),
            progress = 6,
            progressVolumes = entry.progressVolumes,
            repeat = entry.repeat,
            priority = entry.priority,
            private = entry.isHidden,
            hiddenFromStatusLists = entry.isHiddenFromStatusLists,
            customLists = null,
            advancedScores = null,
            notes = entry.notes,
            startedAt = null,
            completedAt = null,
        ) { result ->
            finalProgress = result.getOrNull()?.progress
            result.getOrNull()?.progress?.let(observedProgresses::add)
        }
        coordinator.saveMediaListEntry(
            id = entry.id.toInt(),
            mediaId = entry.mediaId,
            status = MediaListStatus.CURRENT,
            score = entry.score.toDouble(),
            progress = 7,
            progressVolumes = entry.progressVolumes,
            repeat = entry.repeat,
            priority = entry.priority,
            private = entry.isHidden,
            hiddenFromStatusLists = entry.isHiddenFromStatusLists,
            customLists = null,
            advancedScores = null,
            notes = entry.notes,
            startedAt = null,
            completedAt = null,
        ) { result ->
            finalProgress = result.getOrNull()?.progress
            result.getOrNull()?.progress?.let(observedProgresses::add)
        }
        advanceUntilIdle()

        assertTrue(continuations.containsKey(6))
        assertTrue(continuations.containsKey(7))

        continuations.getValue(7).resume(Result.success(MediaListFixtures.aMediaList(id = 1, mediaId = 100, progress = 7)))
        advanceUntilIdle()
        continuations.getValue(6).resume(Result.success(MediaListFixtures.aMediaList(id = 1, mediaId = 100, progress = 6)))
        advanceUntilIdle()

        assertEquals(listOf(7, 6), observedProgresses)
        assertEquals(6, finalProgress)
    }
}
