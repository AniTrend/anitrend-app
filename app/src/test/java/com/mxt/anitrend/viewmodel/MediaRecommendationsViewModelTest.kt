package com.mxt.anitrend.viewmodel

import androidx.paging.testing.ErrorRecovery.RETRY
import androidx.paging.testing.LoadErrorHandler
import androidx.paging.testing.asSnapshot
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.domain.model.RecommendationRecord
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class MediaRecommendationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
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
    fun `load establishes the query and emits projected UI items`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = (1L..21L).map { recommendation(it) },
        )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot()
        assertEquals((1L..21L).toList(), items.map { it.id })
        assertEquals("Alpha", items.first().title)
        assertEquals(11L, items.first().mediaId)
        verify(mediaRepository).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(1),
            eq(KeyUtil.PAGING_LIMIT),
            eq(null),
        )
    }

    @Test
    fun `empty first page emits an empty snapshot`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = emptyList(),
        )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot()
        assertTrue(items.isEmpty())
    }

    @Test
    fun `repository failure propagates from the paging flow`() = runTest(testDispatcher) {
        doReturn(Result.failure<RecommendationPageResult>(RuntimeException("Recommendations failed")))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(1),
                eq(KeyUtil.PAGING_LIMIT),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val error = assertFailsWith<RuntimeException> { vm.pagingDataFlow.asSnapshot() }
        assertEquals("Recommendations failed", error.message)
    }

    @Test
    fun `same query reload does not restart the generation`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = (1L..21L).map { recommendation(it) },
        )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)
        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot()
        assertEquals((1L..21L).toList(), items.map { it.id })
        verify(mediaRepository, times(1)).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(1),
            eq(KeyUtil.PAGING_LIMIT),
            eq(null),
        )
    }

    @Test
    fun `appending pages keeps stable item order`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = (1L..21L).map { recommendation(it) },
        )
        stubPage(
            id = 7L,
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            records = (22L..42L).map { recommendation(it) },
        )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot {
            appendScrollWhile { it.id < 25 }
        }
        assertEquals((1L..42L).toList(), items.map { it.id })
        verify(mediaRepository).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(2),
            eq(KeyUtil.PAGING_LIMIT),
            eq(null),
        )
    }

    @Test
    fun `refresh at the top keeps stable item order`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = (1L..21L).map { recommendation(it) },
        )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot {
            refresh()
        }
        assertEquals((1L..21L).toList(), items.map { it.id })
        verify(mediaRepository, times(2)).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(1),
            eq(KeyUtil.PAGING_LIMIT),
            eq(null),
        )
    }

    @Test
    fun `refresh after appends restarts from page one`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = (1L..21L).map { recommendation(it) },
        )
        stubPage(
            id = 7L,
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            records = (22L..42L).map { recommendation(it) },
        )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot {
            appendScrollWhile { it.id < 25 }
            refresh()
        }
        // One-directional refresh: the source never exposes an anchor-based refresh
        // key, so a refresh always restarts from page one in stable order.
        assertEquals((1L..21L).toList(), items.map { it.id })
    }

    @Test
    fun `append failure is retried through the load state and continues`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = (1L..21L).map { recommendation(it) },
        )
        doReturn(Result.failure<RecommendationPageResult>(RuntimeException("retry me")))
            .doReturn(
                Result.success(
                    RecommendationPageResult(
                        recommendations = (22L..42L).map { recommendation(it) },
                        pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
                    ),
                ),
            )
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(2),
                eq(KeyUtil.PAGING_LIMIT),
                eq(null),
            )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot(
            onError = LoadErrorHandler { RETRY },
        ) {
            appendScrollWhile { it.id < 25 }
        }
        assertEquals((1L..42L).toList(), items.map { it.id })
        verify(mediaRepository, times(2)).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(2),
            eq(KeyUtil.PAGING_LIMIT),
            eq(null),
        )
    }

    @Test
    fun `query switch replaces the generation with the new query items`() = runTest(testDispatcher) {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = (1L..21L).map { recommendation(it) },
        )
        stubPage(
            id = 8L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = (101L..121L).map { recommendation(it) },
        )
        val vm = viewModel()

        vm.load(mediaId = 7L, type = MediaType.MANGA, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot {
            vm.load(mediaId = 8L, type = MediaType.MANGA, isAdult = false)
        }
        assertEquals((101L..121L).toList(), items.map { it.id })
        verify(mediaRepository, times(1)).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(1),
            eq(KeyUtil.PAGING_LIMIT),
            eq(null),
        )
        verify(mediaRepository, times(1)).getMediaRecommendations(
            eq(8L),
            eq(MediaType.MANGA),
            eq(false),
            eq(1),
            eq(KeyUtil.PAGING_LIMIT),
            eq(null),
        )
    }

    private fun viewModel(): MediaRecommendationsViewModel = MediaRecommendationsViewModel(mediaRepository = mediaRepository)

    private suspend fun stubPage(
        id: Long,
        page: Int,
        pageInfo: PageInfoRecord?,
        records: List<RecommendationRecord>,
    ) {
        doReturn(Result.success(RecommendationPageResult(records, pageInfo)))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(id),
                eq(MediaType.MANGA),
                eq(false),
                eq(page),
                eq(KeyUtil.PAGING_LIMIT),
                eq(null),
            )
    }

    private fun recommendation(
        id: Long,
        mediaId: Long = id + 10,
        title: String = if (id == 1L) "Alpha" else "Title $mediaId",
    ): RecommendationRecord = RecommendationRecord(
        id = id,
        mediaRecommendation = mediaSummary(id = mediaId, title = title),
        rating = null,
        user = null,
        userRating = null,
    )

    private fun pageInfo(
        currentPage: Int,
        hasNextPage: Boolean,
    ): PageInfoRecord = PageInfoRecord(
        currentPage = currentPage,
        lastPage = if (hasNextPage) currentPage + 1 else currentPage,
        perPage = KeyUtil.PAGING_LIMIT,
        total = 0,
        hasNextPage = hasNextPage,
        hasPreviousPage = currentPage > 1,
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
