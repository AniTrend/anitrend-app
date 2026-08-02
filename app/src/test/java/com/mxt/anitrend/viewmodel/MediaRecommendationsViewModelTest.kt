package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.domain.model.RecommendationRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.MediaRepository
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
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRecommendationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mediaRepository: MediaRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mediaRepository = mock(MediaRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value is MediaRecommendationsViewModel.UiState.Loading)
    }

    @Test
    fun `load emits Success with projected UI items and pageInfo`() = runTest {
        val page = RecommendationPageResult(
            recommendations = listOf(
                RecommendationRecord(
                    id = 1L,
                    mediaRecommendation = mediaSummary(id = 11L, title = "Alpha"),
                    rating = 88,
                    user = null,
                    userRating = "RATE_UP",
                ),
            ),
            pageInfo = PageInfoRecord(
                currentPage = 1,
                lastPage = 1,
                perPage = 10,
                total = 1,
                hasNextPage = false,
                hasPreviousPage = false,
            ),
        )
        doReturn(Result.success(page))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(null),
                eq(21),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val state = vm.state.value as MediaRecommendationsViewModel.UiState.Success
        assertEquals(1, state.items.size)
        assertEquals(1L, state.items.single().id)
        assertEquals(11L, state.items.single().mediaId)
        assertEquals("Alpha", state.items.single().title)
        assertEquals(1, state.pageInfo?.currentPage)
        verify(mediaRepository).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(null),
            eq(21),
            eq(null),
        )
    }

    @Test
    fun `load emits Success with an empty page for an empty repository result`() = runTest {
        val page = RecommendationPageResult(
            recommendations = emptyList(),
            pageInfo = null,
        )
        doReturn(Result.success(page))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(null),
                eq(21),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val state = vm.state.value as MediaRecommendationsViewModel.UiState.Success
        assertTrue(state.items.isEmpty())
        assertEquals(null, state.pageInfo)
    }

    @Test
    fun `load emits Error when the repository fails`() = runTest {
        doReturn(Result.failure<RecommendationPageResult>(RuntimeException("Recommendations failed")))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(null),
                eq(21),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val state = vm.state.value as MediaRecommendationsViewModel.UiState.Error
        assertEquals("Recommendations failed", state.message)
    }

    @Test
    fun `load falls back to generic message when the failure has no message`() = runTest {
        doReturn(Result.failure<RecommendationPageResult>(RuntimeException()))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(null),
                eq(21),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val state = vm.state.value as MediaRecommendationsViewModel.UiState.Error
        assertEquals("Failed to load recommendations", state.message)
    }

    @Test
    fun `load appends page two to existing items`() = runTest {
        val pageOne = RecommendationPageResult(
            recommendations = listOf(
                RecommendationRecord(
                    id = 1L,
                    mediaRecommendation = mediaSummary(id = 11L),
                    rating = null,
                    user = null,
                    userRating = null,
                ),
            ),
            pageInfo = PageInfoRecord(
                currentPage = 1,
                lastPage = 2,
                perPage = 1,
                total = 2,
                hasNextPage = true,
                hasPreviousPage = false,
            ),
        )
        val pageTwo = RecommendationPageResult(
            recommendations = listOf(
                RecommendationRecord(
                    id = 2L,
                    mediaRecommendation = mediaSummary(id = 12L),
                    rating = null,
                    user = null,
                    userRating = null,
                ),
            ),
            pageInfo = PageInfoRecord(
                currentPage = 2,
                lastPage = 2,
                perPage = 1,
                total = 2,
                hasNextPage = false,
                hasPreviousPage = true,
            ),
        )
        doReturn(Result.success(pageOne))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(null),
                eq(21),
                eq(null),
            )
        doReturn(Result.success(pageTwo))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(2),
                eq(21),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)
        val firstState = vm.state.value as MediaRecommendationsViewModel.UiState.Success
        assertEquals(1, firstState.items.size)

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false, page = 2)
        val secondState = vm.state.value as MediaRecommendationsViewModel.UiState.Success
        assertEquals(2, secondState.items.size)
        assertEquals(2L, secondState.items[1].id)
        assertEquals(2, secondState.pageInfo?.currentPage)
    }

    @Test
    fun `load replaces current items when page is reset to one`() = runTest {
        val pageOne = RecommendationPageResult(
            recommendations = listOf(
                RecommendationRecord(
                    id = 1L,
                    mediaRecommendation = mediaSummary(id = 11L),
                    rating = null,
                    user = null,
                    userRating = null,
                ),
            ),
            pageInfo = PageInfoRecord(
                currentPage = 1,
                lastPage = 1,
                perPage = 1,
                total = 1,
                hasNextPage = false,
                hasPreviousPage = false,
            ),
        )
        val pageTwo = RecommendationPageResult(
            recommendations = listOf(
                RecommendationRecord(
                    id = 2L,
                    mediaRecommendation = mediaSummary(id = 12L),
                    rating = null,
                    user = null,
                    userRating = null,
                ),
            ),
            pageInfo = PageInfoRecord(
                currentPage = 1,
                lastPage = 1,
                perPage = 1,
                total = 1,
                hasNextPage = false,
                hasPreviousPage = false,
            ),
        )
        doReturn(Result.success(pageOne))
            .doReturn(Result.success(pageTwo))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(null),
                eq(21),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)
        val firstState = vm.state.value as MediaRecommendationsViewModel.UiState.Success
        assertEquals(1L, firstState.items.single().id)

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)
        val secondState = vm.state.value as MediaRecommendationsViewModel.UiState.Success
        assertEquals(1, secondState.items.size)
        assertEquals(2L, secondState.items.single().id)
    }

    @Test
    fun `records without a media recommendation are not projected as UI items`() = runTest {
        val page = RecommendationPageResult(
            recommendations = listOf(
                RecommendationRecord(
                    id = 1L,
                    mediaRecommendation = null,
                    rating = null,
                    user = null,
                    userRating = null,
                ),
            ),
            pageInfo = null,
        )
        doReturn(Result.success(page))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(null),
                eq(21),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val state = vm.state.value as MediaRecommendationsViewModel.UiState.Success
        assertTrue(state.items.isEmpty())
    }

    private fun viewModel(): MediaRecommendationsViewModel = MediaRecommendationsViewModel(
        mediaRepository = mediaRepository,
        dispatcher = testDispatcher,
    )

    private fun mediaSummary(
        id: Long,
        title: String = "Title $id",
    ): MediaSummaryRecord = MediaSummaryRecord(
        id = id,
        titleUserPreferred = title,
        titleRomaji = title,
        titleEnglish = null,
        titleOriginal = null,
        coverImage = null,
        type = "MANGA",
        format = "MANGA",
        episodes = 0,
        chapters = 12,
        volumes = 0,
        status = "RELEASING",
        siteUrl = null,
        isFavourite = false,
        startDate = FuzzyDateRecord(year = 2024, month = 1, day = 1),
        nextAiringEpisode = null,
        averageScore = 80,
    )
}
