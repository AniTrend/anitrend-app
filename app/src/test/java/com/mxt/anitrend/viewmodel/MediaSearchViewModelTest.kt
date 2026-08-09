package com.mxt.anitrend.viewmodel

import androidx.paging.testing.ErrorRecovery.RETRY
import androidx.paging.testing.LoadErrorHandler
import androidx.paging.testing.asSnapshot
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.SearchRepository
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
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class MediaSearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchRepository: SearchRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchRepository = mock(SearchRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load establishes the query and emits projected UI items`() = runTest(testDispatcher) {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = (1L..21L).map { media(it) },
        )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot()
        assertEquals((1L..21L).toList(), items.map { it.id })
        assertEquals("Alpha", items.first().title)
        assertEquals("ANIME", items.first().mediaType)
        verify(searchRepository).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `empty first page emits an empty snapshot`() = runTest(testDispatcher) {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = emptyList(),
        )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot()
        assertTrue(items.isEmpty())
    }

    @Test
    fun `repository failure propagates from the paging flow`() = runTest(testDispatcher) {
        doReturn(Result.failure<PageContainer<MediaBase>>(RuntimeException("Media search failed")))
            .`when`(searchRepository)
            .searchMedia(
                search = "cowboy",
                type = MediaType.ANIME,
                page = 1,
                perPage = KeyUtil.PAGING_LIMIT,
                isAdult = false,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val error = assertFailsWith<RuntimeException> { vm.pagingDataFlow.asSnapshot() }
        assertEquals("Media search failed", error.message)
    }

    @Test
    fun `same query reload does not restart the generation`() = runTest(testDispatcher) {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = (1L..21L).map { media(it) },
        )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)
        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot()
        assertEquals((1L..21L).toList(), items.map { it.id })
        verify(searchRepository, times(1)).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `appending pages keeps stable item order`() = runTest(testDispatcher) {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = (1L..21L).map { media(it) },
        )
        stubPage(
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            items = (22L..42L).map { media(it) },
        )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot {
            appendScrollWhile { it.id < 25 }
        }
        assertEquals((1L..42L).toList(), items.map { it.id })
        verify(searchRepository).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `refresh at the top keeps stable item order`() = runTest(testDispatcher) {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = (1L..21L).map { media(it) },
        )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot {
            refresh()
        }
        assertEquals((1L..21L).toList(), items.map { it.id })
        verify(searchRepository, times(2)).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `refresh after appends restarts from page one`() = runTest(testDispatcher) {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = (1L..21L).map { media(it) },
        )
        stubPage(
            page = 2,
            pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
            items = (22L..42L).map { media(it) },
        )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

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
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = true),
            items = (1L..21L).map { media(it) },
        )
        doReturn(Result.failure<PageContainer<MediaBase>>(RuntimeException("retry me")))
            .doReturn(
                Result.success(
                    pageContainer(
                        pageInfo = pageInfo(currentPage = 2, hasNextPage = false),
                        items = (22L..42L).map { media(it) },
                    ),
                ),
            )
            .`when`(searchRepository)
            .searchMedia(
                search = "cowboy",
                type = MediaType.ANIME,
                page = 2,
                perPage = KeyUtil.PAGING_LIMIT,
                isAdult = false,
                sort = listOf(MediaSort.SEARCH_MATCH),
            )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot(
            onError = LoadErrorHandler { RETRY },
        ) {
            appendScrollWhile { it.id < 25 }
        }
        assertEquals((1L..42L).toList(), items.map { it.id })
        verify(searchRepository, times(2)).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 2,
            perPage = KeyUtil.PAGING_LIMIT,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    @Test
    fun `query switch replaces the generation with the new query items`() = runTest(testDispatcher) {
        stubPage(
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = (1L..21L).map { media(it) },
        )
        stubPage(
            search = "berserk",
            type = MediaType.MANGA,
            isAdult = null,
            page = 1,
            pageInfo = pageInfo(currentPage = 1, hasNextPage = false),
            items = (101L..121L).map { media(it) },
        )
        val vm = viewModel()

        vm.load(search = "cowboy", type = MediaType.ANIME, isAdult = false)

        val items = vm.pagingDataFlow.asSnapshot {
            vm.load(search = "berserk", type = MediaType.MANGA, isAdult = null)
        }
        assertEquals((101L..121L).toList(), items.map { it.id })
        verify(searchRepository, times(1)).searchMedia(
            search = "cowboy",
            type = MediaType.ANIME,
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            isAdult = false,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
        verify(searchRepository, times(1)).searchMedia(
            search = "berserk",
            type = MediaType.MANGA,
            page = 1,
            perPage = KeyUtil.PAGING_LIMIT,
            isAdult = null,
            sort = listOf(MediaSort.SEARCH_MATCH),
        )
    }

    private fun viewModel(): MediaSearchViewModel = MediaSearchViewModel(searchRepository = searchRepository)

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
                perPage = KeyUtil.PAGING_LIMIT,
                isAdult = isAdult,
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
        perPage = KeyUtil.PAGING_LIMIT,
        currentPage = currentPage,
    ).apply {
        setHasNextPage(hasNextPage)
    }

    private fun media(
        id: Long,
        title: String = if (id == 1L) "Alpha" else "Title $id",
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
