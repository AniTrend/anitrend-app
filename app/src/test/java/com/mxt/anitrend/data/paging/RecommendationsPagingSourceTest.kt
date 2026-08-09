package com.mxt.anitrend.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.domain.model.RecommendationRecord
import com.mxt.anitrend.domain.model.toRecommendationItemUiModel
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.repository.MediaRepository
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationsPagingSourceTest {

    private lateinit var mediaRepository: MediaRepository

    @Before
    fun setUp() {
        mediaRepository = mock(MediaRepository::class.java)
    }

    private val config =
        PagingConfig(
            pageSize = 3,
            initialLoadSize = 3,
            enablePlaceholders = false,
        )

    @Test
    fun `refresh loads page one with next key when more pages exist`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
        )
        val pager = TestPager(config, source(mediaId = 7L))

        val result = pager.refresh() as LoadResult.Page

        assertEquals(null, result.prevKey)
        assertEquals(2, result.nextKey)
        assertEquals(listOf(1L, 2L, 3L), result.data.map { it.id })
        verify(mediaRepository).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(1),
            eq(3),
            eq(null),
        )
    }

    @Test
    fun `refresh on a terminal page returns no next key`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = listOf(recommendation(1L)),
        )
        val pager = TestPager(config, source(mediaId = 7L))

        val result = pager.refresh() as LoadResult.Page

        assertEquals(null, result.prevKey)
        assertEquals(null, result.nextKey)
    }

    @Test
    fun `refresh with a null page info is treated as terminal`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = null,
            records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
        )
        val pager = TestPager(config, source(mediaId = 7L))

        val result = pager.refresh() as LoadResult.Page

        assertEquals(null, result.prevKey)
        assertEquals(null, result.nextKey)
        assertEquals(listOf(1L, 2L, 3L), result.data.map { it.id })
    }

    @Test
    fun `append loads the next page after refresh without a previous key`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
        )
        stubPage(
            id = 7L,
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            records = listOf(recommendation(4L), recommendation(5L), recommendation(6L)),
        )
        val pager = TestPager(config, source(mediaId = 7L))
        pager.refresh()

        val result = pager.append() as LoadResult.Page

        // One-directional paging: no page ever advertises a previous page.
        assertEquals(null, result.prevKey)
        assertEquals(null, result.nextKey)
        assertEquals(listOf(4L, 5L, 6L), result.data.map { it.id })
        verify(mediaRepository).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(2),
            eq(3),
            eq(null),
        )
    }

    @Test
    fun `append stops when the last page has no next key`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = listOf(recommendation(1L)),
        )
        val pager = TestPager(config, source(mediaId = 7L))
        pager.refresh()

        val result = pager.append()

        assertEquals(null, result)
        verify(mediaRepository, never()).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(2),
            eq(3),
            eq(null),
        )
    }

    @Test
    fun `repository failure maps to LoadResult Error`() = runTest {
        stubFailure(id = 7L, page = 1, message = "Recommendations failed")
        val pager = TestPager(config, source(mediaId = 7L))

        val result = pager.refresh() as LoadResult.Error

        assertEquals("Recommendations failed", result.throwable.message)
    }

    @Test
    fun `mapping failure maps to LoadResult Error`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = listOf(recommendation(1L)),
        )
        val throwingProject: (RecommendationRecord) -> RecommendationItemUiModel? = {
            throw IllegalStateException("Projection failed")
        }
        val pager = TestPager(config, source(mediaId = 7L, project = throwingProject))

        val result = pager.refresh() as LoadResult.Error

        assertEquals("Projection failed", result.throwable.message)
    }

    @Test
    fun `cancellation in the repository result is rethrown not wrapped`() = runTest {
        doReturn(Result.failure<RecommendationPageResult>(CancellationException("cancelled")))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(1),
                eq(3),
                eq(null),
            )
        val pager = TestPager(config, source(mediaId = 7L))

        assertFailsWith<CancellationException> { pager.refresh() }
    }

    @Test
    fun `cancellation during projection is rethrown not wrapped`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = listOf(recommendation(1L)),
        )
        val cancellingProject: (RecommendationRecord) -> RecommendationItemUiModel? = {
            throw CancellationException("cancelled")
        }
        val pager = TestPager(config, source(mediaId = 7L, project = cancellingProject))

        assertFailsWith<CancellationException> { pager.refresh() }
    }

    @Test
    fun `records without a media recommendation are dropped from the page`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = listOf(
                RecommendationRecord(
                    id = 1L,
                    mediaRecommendation = null,
                    rating = null,
                    user = null,
                    userRating = null,
                ),
                recommendation(2L),
            ),
        )
        val pager = TestPager(config, source(mediaId = 7L))

        val result = pager.refresh() as LoadResult.Page

        assertEquals(listOf(2L), result.data.map { it.id })
    }

    @Test
    fun `overlapping ids across pages are emitted once in first-seen order`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
        )
        stubPage(
            id = 7L,
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            records = listOf(recommendation(3L), recommendation(4L), recommendation(5L)),
        )
        val pager = TestPager(config, source(mediaId = 7L))
        pager.refresh()
        pager.append()

        val pages = pager.getPages()
        assertEquals(listOf(1L, 2L, 3L), pages[0].data.map { it.id })
        // The overlap (id 3) keeps its first-seen position on page one; the later
        // projection is dropped so no id is ever emitted twice.
        assertEquals(listOf(4L, 5L), pages[1].data.map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), pages.flatten().map { it.id })
    }

    @Test
    fun `source instances do not share dedup state for the same recommendation id`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            records = listOf(recommendation(5L)),
        )
        doReturn(
            Result.success(
                RecommendationPageResult(
                    recommendations = listOf(recommendation(5L)),
                    pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
                ),
            ),
        )
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(8L),
                eq(MediaType.ANIME),
                isNull(),
                eq(1),
                eq(3),
                eq(null),
            )
        val pagerA = TestPager(config, source(mediaId = 7L))
        val pagerB = TestPager(config, source(mediaId = 8L, type = MediaType.ANIME, isAdult = null))

        val resultA = pagerA.refresh() as LoadResult.Page
        val resultB = pagerB.refresh() as LoadResult.Page

        // Dedup state is per source instance: the same recommendation id is emitted
        // once by each source. Shared emitted-id state would drop it from the
        // second source.
        assertEquals(listOf(5L), resultA.data.map { it.id })
        assertEquals(listOf(5L), resultB.data.map { it.id })
        verify(mediaRepository).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(1),
            eq(3),
            eq(null),
        )
        verify(mediaRepository).getMediaRecommendations(
            eq(8L),
            eq(MediaType.ANIME),
            isNull(),
            eq(1),
            eq(3),
            eq(null),
        )
    }

    @Test
    fun `getRefreshKey always returns null for one directional refresh`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
        )
        stubPage(
            id = 7L,
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            records = listOf(recommendation(4L), recommendation(5L), recommendation(6L)),
        )
        val source = source(mediaId = 7L)
        val pager = TestPager(config, source)
        pager.refresh()
        pager.append()

        val anchorState = pager.getPagingState(anchorPosition = 4)

        // One-directional paging: no anchor-based refresh, refresh always restarts
        // at page one even when the user is scrolled deep into the list.
        assertEquals(null, source.getRefreshKey(anchorState))
    }

    @Test
    fun `append retry after failure reuses the failed page key`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
        )
        doReturn(Result.failure<RecommendationPageResult>(RuntimeException("retry me")))
            .doReturn(
                Result.success(
                    RecommendationPageResult(
                        recommendations = listOf(recommendation(4L), recommendation(5L), recommendation(6L)),
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
                eq(3),
                eq(null),
            )
        val pager = TestPager(config, source(mediaId = 7L))
        pager.refresh()

        val failed = pager.append()
        assertTrue(failed is LoadResult.Error)
        val retried = pager.append() as LoadResult.Page

        assertEquals(listOf(4L, 5L, 6L), retried.data.map { it.id })
        verify(mediaRepository, times(2)).getMediaRecommendations(
            eq(7L),
            eq(MediaType.MANGA),
            eq(false),
            eq(2),
            eq(3),
            eq(null),
        )
    }

    @Test
    fun `refresh resets the dedup state for the same source instance`() = runTest {
        stubPage(
            id = 7L,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
        )
        stubPage(
            id = 7L,
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            records = listOf(recommendation(3L), recommendation(4L), recommendation(5L)),
        )
        val source = source(mediaId = 7L)
        val pager = TestPager(config, source)
        pager.refresh()
        pager.append()

        // A second refresh on the same instance deterministically resets the dedup
        // state: page one is re-emitted in full, and the previously-seen overlap on
        // page two (ids 3, 4, 5) is allowed again.
        val refreshed = source.load(LoadParams.Refresh(null, 3, false)) as LoadResult.Page
        assertEquals(listOf(1L, 2L, 3L), refreshed.data.map { it.id })

        val appended = source.load(LoadParams.Append(2, 3, false)) as LoadResult.Page
        assertEquals(listOf(4L, 5L), appended.data.map { it.id })
    }

    @Test
    fun `append and refresh loads are serialized by the source mutex`() {
        runBlocking {
            stubPage(
                id = 7L,
                page = 1,
                pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
                records = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
            )
            stubPage(
                id = 7L,
                page = 2,
                pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
                records = listOf(recommendation(2L), recommendation(3L), recommendation(4L)),
            )
            val source = source(mediaId = 7L)
            TestPager(config, source).refresh()

            // After seeding, gate both repository calls with latches so the append can
            // be held inside the repository (with the mutex acquired) and the test can
            // observe exactly where a concurrently launched refresh can and cannot go.
            // The proxy wraps doAnswer return values in Result.success, so the answers
            // return the bare page result (see MediaListViewModelStoreObservationTest).
            val appendEntered = CountDownLatch(1)
            val releaseAppend = CountDownLatch(1)
            doAnswer {
                appendEntered.countDown()
                check(releaseAppend.await(5, TimeUnit.SECONDS)) { "append was never released" }
                RecommendationPageResult(
                    recommendations = listOf(recommendation(2L), recommendation(3L), recommendation(4L)),
                    pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
                )
            }.`when`(mediaRepository)
                .getMediaRecommendations(
                    eq(7L),
                    eq(MediaType.MANGA),
                    eq(false),
                    eq(2),
                    eq(3),
                    eq(null),
                )
            val refreshEntered = CountDownLatch(1)
            val releaseRefresh = CountDownLatch(1)
            doAnswer {
                refreshEntered.countDown()
                check(releaseRefresh.await(5, TimeUnit.SECONDS)) { "refresh was never released" }
                RecommendationPageResult(
                    recommendations = listOf(recommendation(1L), recommendation(2L), recommendation(3L)),
                    pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
                )
            }.`when`(mediaRepository)
                .getMediaRecommendations(
                    eq(7L),
                    eq(MediaType.MANGA),
                    eq(false),
                    eq(1),
                    eq(3),
                    eq(null),
                )

            // The append acquires the mutex and is held inside the repository call.
            val appendDeferred = async(Dispatchers.Default) { source.load(LoadParams.Append(2, 3, false)) }
            assertTrue("append must hold the repository call", appendEntered.await(5, TimeUnit.SECONDS))

            // A refresh launched while the append holds the mutex must not reach the
            // repository, and therefore cannot reach the dedup-state mutation that
            // follows the repository call, until the append completes.
            val refreshDeferred = async(Dispatchers.Default) { source.load(LoadParams.Refresh(null, 3, false)) }
            assertFalse(
                "refresh must not reach the repository while the append holds the mutex",
                refreshEntered.await(1, TimeUnit.SECONDS),
            )
            verify(mediaRepository, times(1)).getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(1),
                eq(3),
                eq(null),
            )

            // Release the append: only then does the refresh acquire the mutex and enter
            // the repository, proving the append ran against the pre-refresh dedup state.
            releaseAppend.countDown()
            assertTrue(
                "refresh must enter the repository only after the append completes",
                refreshEntered.await(5, TimeUnit.SECONDS),
            )
            releaseRefresh.countDown()

            val appendResult = withTimeout(10_000) { appendDeferred.await() } as LoadResult.Page
            val refreshResult = withTimeout(10_000) { refreshDeferred.await() } as LoadResult.Page

            // The append was filtered against the pre-refresh state (ids 2 and 3
            // dropped), and the refresh reset the dedup state and re-emitted page one
            // in full, deterministically.
            assertEquals(listOf(4L), appendResult.data.map { it.id })
            assertEquals(listOf(1L, 2L, 3L), refreshResult.data.map { it.id })
            verify(mediaRepository, times(2)).getMediaRecommendations(
                eq(7L),
                eq(MediaType.MANGA),
                eq(false),
                eq(1),
                eq(3),
                eq(null),
            )
        }
    }

    private fun source(
        mediaId: Long,
        type: MediaType? = MediaType.MANGA,
        isAdult: Boolean? = false,
        project: (RecommendationRecord) -> RecommendationItemUiModel? =
            RecommendationRecord::toRecommendationItemUiModel,
    ): RecommendationsPagingSource = RecommendationsPagingSource(
        mediaRepository = mediaRepository,
        mediaId = mediaId,
        type = type,
        isAdult = isAdult,
        project = project,
    )

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
                eq(3),
                eq(null),
            )
    }

    private suspend fun stubFailure(
        id: Long,
        page: Int,
        message: String,
    ) {
        doReturn(Result.failure<RecommendationPageResult>(RuntimeException(message)))
            .`when`(mediaRepository)
            .getMediaRecommendations(
                eq(id),
                eq(MediaType.MANGA),
                eq(false),
                eq(page),
                eq(3),
                eq(null),
            )
    }

    private fun recommendation(
        id: Long,
        mediaId: Long = id + 10,
        title: String = "Title $mediaId",
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
        perPage = 3,
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
