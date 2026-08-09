package com.mxt.anitrend.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.mxt.anitrend.data.mapper.toMediaSearchItemUiModel
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
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
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class MediaSearchPagingSourceTest {

    private lateinit var searchRepository: SearchRepository

    @Before
    fun setUp() {
        searchRepository = mock(SearchRepository::class.java)
    }

    private val config =
        PagingConfig(
            pageSize = 3,
            initialLoadSize = 3,
            enablePlaceholders = false,
        )

    @Test
    fun `refresh loads page one with next key and the query identity parameters`() = runTest {
        stubPage(
            search = "cowboy",
            type = MediaType.ANIME,
            isAdult = false,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = listOf(media(1L), media(2L), media(3L)),
        )
        val pager = TestPager(config, source())

        val result = pager.refresh() as LoadResult.Page

        assertEquals(null, result.prevKey)
        assertEquals(2, result.nextKey)
        assertEquals(listOf(1L, 2L, 3L), result.data.map { it.id })
        verify(searchRepository).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 1,
            perPage = 3,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `refresh on a terminal page returns no next key`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = listOf(media(1L)),
        )
        val pager = TestPager(config, source())

        val result = pager.refresh() as LoadResult.Page

        assertEquals(null, result.prevKey)
        assertEquals(null, result.nextKey)
    }

    @Test
    fun `refresh with a null page info is treated as terminal`() = runTest {
        stubPage(
            page = 1,
            pageInfo = null,
            items = listOf(media(1L), media(2L), media(3L)),
        )
        val pager = TestPager(config, source())

        val result = pager.refresh() as LoadResult.Page

        assertEquals(null, result.prevKey)
        assertEquals(null, result.nextKey)
        assertEquals(listOf(1L, 2L, 3L), result.data.map { it.id })
    }

    @Test
    fun `empty first page emits an empty page without a next key`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = emptyList(),
        )
        val pager = TestPager(config, source())

        val result = pager.refresh() as LoadResult.Page

        assertTrue(result.data.isEmpty())
        assertEquals(null, result.nextKey)
    }

    @Test
    fun `append loads the next page after refresh without a previous key`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = listOf(media(1L), media(2L), media(3L)),
        )
        stubPage(
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            items = listOf(media(4L), media(5L), media(6L)),
        )
        val pager = TestPager(config, source())
        pager.refresh()

        val result = pager.append() as LoadResult.Page

        // One-directional paging: no page ever advertises a previous page.
        assertEquals(null, result.prevKey)
        assertEquals(null, result.nextKey)
        assertEquals(listOf(4L, 5L, 6L), result.data.map { it.id })
        verify(searchRepository).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 2,
            perPage = 3,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `append stops when the last page has no next key`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = listOf(media(1L)),
        )
        val pager = TestPager(config, source())
        pager.refresh()

        val result = pager.append()

        assertEquals(null, result)
        verify(searchRepository, never()).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 2,
            perPage = 3,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `repository failure maps to LoadResult Error`() = runTest {
        stubFailure(page = 1, message = "Media search failed")
        val pager = TestPager(config, source())

        val result = pager.refresh() as LoadResult.Error

        assertEquals("Media search failed", result.throwable.message)
    }

    @Test
    fun `mapping failure maps to LoadResult Error`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = listOf(media(1L)),
        )
        val throwingProject: (MediaBase) -> MediaSearchItemUiModel = {
            throw IllegalStateException("Projection failed")
        }
        val pager = TestPager(config, source(project = throwingProject))

        val result = pager.refresh() as LoadResult.Error

        assertEquals("Projection failed", result.throwable.message)
    }

    @Test
    fun `cancellation in the repository result is rethrown not wrapped`() = runTest {
        doReturn(Result.failure<PageContainer<MediaBase>>(CancellationException("cancelled")))
            .`when`(searchRepository)
            .searchMedia(
                search = "cowboy",
                type = MediaType.ANIME,
                page = 1,
                perPage = 3,
                isAdult = false,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
        val pager = TestPager(config, source())

        assertFailsWith<CancellationException> { pager.refresh() }
    }

    @Test
    fun `cancellation during projection is rethrown not wrapped`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = listOf(media(1L)),
        )
        val cancellingProject: (MediaBase) -> MediaSearchItemUiModel = {
            throw CancellationException("cancelled")
        }
        val pager = TestPager(config, source(project = cancellingProject))

        assertFailsWith<CancellationException> { pager.refresh() }
    }

    @Test
    fun `overlapping media ids across pages are emitted once in first-seen order`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = listOf(media(1L), media(2L), media(3L)),
        )
        stubPage(
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            items = listOf(media(3L), media(4L), media(5L)),
        )
        val pager = TestPager(config, source())
        pager.refresh()
        pager.append()

        val pages = pager.getPages()
        assertEquals(listOf(1L, 2L, 3L), pages[0].data.map { it.id })
        // The overlap (id 3) keeps its first-seen position on page one; the later
        // projection is dropped so no media id is ever emitted twice.
        assertEquals(listOf(4L, 5L), pages[1].data.map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), pages.flatten().map { it.id })
    }

    @Test
    fun `source instances do not share dedup state for the same media id`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = listOf(media(5L)),
        )
        doReturn(
            Result.success(
                pageContainer(
                    pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
                    items = listOf(media(5L)),
                ),
            ),
        )
            .`when`(searchRepository)
            .searchMedia(
                search = "berserk",
                type = MediaType.MANGA,
                page = 1,
                perPage = 3,
                isAdult = null,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
        val pagerA = TestPager(config, source())
        val pagerB = TestPager(config, source(search = "berserk", type = MediaType.MANGA, isAdult = null))

        val resultA = pagerA.refresh() as LoadResult.Page
        val resultB = pagerB.refresh() as LoadResult.Page

        // Dedup state is per source instance: the same media id is emitted once by
        // each source. Shared emitted-id state would drop it from the second source.
        assertEquals(listOf(5L), resultA.data.map { it.id })
        assertEquals(listOf(5L), resultB.data.map { it.id })
        verify(searchRepository).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 1,
            perPage = 3,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
        verify(searchRepository).searchMedia(
            search = "berserk",
            type = MediaType.MANGA,
            page = 1,
            perPage = 3,
            isAdult = null,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `getRefreshKey always returns null for one directional refresh`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = listOf(media(1L), media(2L), media(3L)),
        )
        stubPage(
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            items = listOf(media(4L), media(5L), media(6L)),
        )
        val source = source()
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
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = listOf(media(1L), media(2L), media(3L)),
        )
        doReturn(Result.failure<PageContainer<MediaBase>>(RuntimeException("retry me")))
            .doReturn(
                Result.success(
                    pageContainer(
                        pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
                        items = listOf(media(4L), media(5L), media(6L)),
                    ),
                ),
            )
            .`when`(searchRepository)
            .searchMedia(
                search = "cowboy",
                type = MediaType.ANIME,
                page = 2,
                perPage = 3,
                isAdult = false,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
        val pager = TestPager(config, source())
        pager.refresh()

        val failed = pager.append()
        assertTrue(failed is LoadResult.Error)
        val retried = pager.append() as LoadResult.Page

        assertEquals(listOf(4L, 5L, 6L), retried.data.map { it.id })
        verify(searchRepository, times(2)).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 2,
            perPage = 3,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `refresh resets the dedup state for the same source instance`() = runTest {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = listOf(media(1L), media(2L), media(3L)),
        )
        stubPage(
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            items = listOf(media(3L), media(4L), media(5L)),
        )
        val source = source()
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
                page = 1,
                pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
                items = listOf(media(1L), media(2L), media(3L)),
            )
            stubPage(
                page = 2,
                pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
                items = listOf(media(2L), media(3L), media(4L)),
            )
            val source = source()
            TestPager(config, source).refresh()

            // After seeding, gate both repository calls with latches so the append can
            // be held inside the repository (with the mutex acquired) and the test can
            // observe exactly where a concurrently launched refresh can and cannot go.
            // The proxy wraps doAnswer return values in Result.success, so the answers
            // return the bare page container.
            val appendEntered = CountDownLatch(1)
            val releaseAppend = CountDownLatch(1)
            doAnswer {
                appendEntered.countDown()
                check(releaseAppend.await(5, TimeUnit.SECONDS)) { "append was never released" }
                pageContainer(
                    pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
                    items = listOf(media(2L), media(3L), media(4L)),
                )
            }.`when`(searchRepository)
                .searchMedia(
                    search = "cowboy",
                    type = MediaType.ANIME,
                    page = 2,
                    perPage = 3,
                    isAdult = false,
                    sort = listOf(MediaSort.SEARCH_MATCH),
                )
            val refreshEntered = CountDownLatch(1)
            val releaseRefresh = CountDownLatch(1)
            doAnswer {
                refreshEntered.countDown()
                check(releaseRefresh.await(5, TimeUnit.SECONDS)) { "refresh was never released" }
                pageContainer(
                    pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
                    items = listOf(media(1L), media(2L), media(3L)),
                )
            }.`when`(searchRepository)
                .searchMedia(
                    search = "cowboy",
                    type = MediaType.ANIME,
                    page = 1,
                    perPage = 3,
                    isAdult = false,
                    sort = listOf(MediaSort.SEARCH_MATCH),
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
            verify(searchRepository, times(1)).searchMedia(
                search = "cowboy",
                type = MediaType.ANIME,
                page = 1,
                perPage = 3,
                isAdult = false,
                sort = listOf(MediaSort.SEARCH_MATCH),
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
            verify(searchRepository, times(2)).searchMedia(
                search = "cowboy",
                type = MediaType.ANIME,
                page = 1,
                perPage = 3,
                isAdult = false,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
        }
    }

    private fun source(
        search: String = "cowboy",
        type: MediaType? = MediaType.ANIME,
        isAdult: Boolean? = false,
        project: (MediaBase) -> MediaSearchItemUiModel = MediaBase::toMediaSearchItemUiModel,
    ): MediaSearchPagingSource = MediaSearchPagingSource(
        searchRepository = searchRepository,
        search = search,
        type = type,
        isAdult = isAdult,
        project = project,
    )

    private suspend fun stubPage(
        search: String = "cowboy",
        type: MediaType? = MediaType.ANIME,
        isAdult: Boolean? = false,
        page: Int,
        pageInfo: PageInfo?,
        items: List<MediaBase>,
    ) {
        doReturn(Result.success(pageContainer(pageInfo, items)))
            .`when`(searchRepository)
            .searchMedia(
                search = search,
                type = type,
                page = page,
                perPage = 3,
                isAdult = isAdult,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
    }

    private suspend fun stubFailure(
        page: Int,
        message: String,
    ) {
        doReturn(Result.failure<PageContainer<MediaBase>>(RuntimeException(message)))
            .`when`(searchRepository)
            .searchMedia(
                search = "cowboy",
                type = MediaType.ANIME,
                page = page,
                perPage = 3,
                isAdult = false,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
    }

    private fun pageContainer(
        pageInfo: PageInfo?,
        items: List<MediaBase>,
    ): PageContainer<MediaBase> = PageContainer<MediaBase>().apply {
        pageData = items
        pageInfo?.let { this.pageInfo = it }
    }

    private fun pageInfo(
        currentPage: Int,
        hasNextPage: Boolean,
    ): PageInfo = PageInfo(
        total = 0,
        perPage = 3,
        currentPage = currentPage,
    ).apply {
        setHasNextPage(hasNextPage)
    }

    private fun media(
        id: Long,
        title: String = "Title $id",
    ): MediaBase = MediaBase().apply {
        this.id = id
        this.title =
            MediaTitle(
                romajiRaw = title,
                englishRaw = null,
                originalRaw = null,
                userPreferredRaw = title,
            )
        this.type = KeyUtil.ANIME
        this.format = KeyUtil.TV
        this.status = KeyUtil.RELEASING
        this.episodes = 12
        this.chapters = 0
        this.volumes = 0
        this.averageScore = 80
        this.isFavourite = false
    }
}
