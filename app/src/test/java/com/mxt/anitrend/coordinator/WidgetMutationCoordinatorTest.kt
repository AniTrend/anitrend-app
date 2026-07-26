package com.mxt.anitrend.coordinator

import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetMutationCoordinatorTest {

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

    @Test
    fun `deleteMediaListEntry delegates to BrowseRepository and returns result`() = runTest {
        val deleteState = DeleteState(isDeleted = true)
        doReturn(Result.success(deleteState))
            .`when`(browseRepository)
            .deleteMediaListEntry(42L)
        val coordinator = WidgetMutationCoordinator(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            userRepository = userRepository,
            feedRepository = feedRepository,
            databaseHelper = databaseHelper,
        )
        var callbackResult: Result<DeleteState>? = null

        coordinator.deleteMediaListEntry(42L) { result ->
            callbackResult = result
        }

        verify(browseRepository).deleteMediaListEntry(42L)
        assertTrue(callbackResult?.isSuccess == true)
        assertEquals(deleteState, callbackResult?.getOrNull())
    }

    @Test
    fun `saveMediaListEntry delegates to BrowseRepository and returns result`() = runTest {
        val mediaList = MediaList().apply { id = 7L }
        val startedAt = FuzzyDateInput(day = 1, month = 2, year = 2024)
        val completedAt = FuzzyDateInput(day = 3, month = 4, year = 2024)
        doReturn(Result.success(mediaList))
            .`when`(browseRepository)
            .saveMediaListEntry(
                id = 7,
                mediaId = 99L,
                status = MediaListStatus.CURRENT,
                score = 8.5,
                progress = 12,
                progressVolumes = 2,
                repeat = 1,
                priority = 3,
                private = true,
                hiddenFromStatusLists = false,
                customLists = listOf("Custom"),
                advancedScores = listOf(1.5, 2.5),
                notes = "note",
                scoreFormat = ScoreFormat.POINT_100,
                startedAt = startedAt,
                completedAt = completedAt,
            )
        val coordinator = WidgetMutationCoordinator(
            baseRepository = baseRepository,
            browseRepository = browseRepository,
            userRepository = userRepository,
            feedRepository = feedRepository,
            databaseHelper = databaseHelper,
        )
        var callbackResult: Result<MediaList>? = null

        coordinator.saveMediaListEntry(
            id = 7,
            mediaId = 99L,
            status = MediaListStatus.CURRENT,
            score = 8.5,
            progress = 12,
            progressVolumes = 2,
            repeat = 1,
            priority = 3,
            private = true,
            hiddenFromStatusLists = false,
            customLists = listOf("Custom"),
            advancedScores = listOf(1.5, 2.5),
            notes = "note",
            startedAt = startedAt,
            completedAt = completedAt,
        ) { result ->
            callbackResult = result
        }

        verify(browseRepository).saveMediaListEntry(
            id = 7,
            mediaId = 99L,
            status = MediaListStatus.CURRENT,
            score = 8.5,
            progress = 12,
            progressVolumes = 2,
            repeat = 1,
            priority = 3,
            private = true,
            hiddenFromStatusLists = false,
            customLists = listOf("Custom"),
            advancedScores = listOf(1.5, 2.5),
            notes = "note",
            scoreFormat = ScoreFormat.POINT_100,
            startedAt = startedAt,
            completedAt = completedAt,
        )
        assertTrue(callbackResult?.isSuccess == true)
        assertEquals(mediaList, callbackResult?.getOrNull())
    }
}
