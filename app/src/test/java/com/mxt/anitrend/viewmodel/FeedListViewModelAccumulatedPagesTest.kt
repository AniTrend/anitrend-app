package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseMutation
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.FeedMutation
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FeedListViewModelAccumulatedPagesTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var feedRepository: FeedRepository
    private lateinit var baseRepository: BaseRepository
    private lateinit var feedMutations: MutableSharedFlow<FeedMutation>
    private lateinit var baseMutations: MutableSharedFlow<BaseMutation>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        feedRepository = mock(FeedRepository::class.java)
        baseRepository = mock(BaseRepository::class.java)
        feedMutations = MutableSharedFlow(extraBufferCapacity = 1)
        baseMutations = MutableSharedFlow(extraBufferCapacity = 1)
        doReturn(feedMutations)
            .`when`(feedRepository)
            .mutationEvents
        doReturn(baseMutations)
            .`when`(baseRepository)
            .mutationEvents
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load page one then page two accumulates all loaded items`() = runTest {
        stubPage(1, feed(id = 1L), feed(id = 2L))
        stubPage(2, feed(id = 3L), feed(id = 4L))
        val viewModel = createViewModel()

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(page = 2, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), state.content.pageData.map { it.id })
        assertEquals(listOf(1L, 2L, 3L, 4L), state.items.map { it.id })
        assertEquals(setOf(1, 2), state.loadedPages)
    }

    @Test
    fun `duplicate IDs across pages are merged into one rendered item`() = runTest {
        stubPage(1, feed(id = 1L, text = "first"))
        stubPage(2, feed(id = 1L, text = "updated"), feed(id = 2L))
        val viewModel = createViewModel()

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(page = 2, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(1L, 2L), state.content.pageData.map { it.id })
        assertEquals(1, state.content.pageData.count { it.id == 1L })
        assertEquals("updated", state.content.pageData.first { it.id == 1L }.text)
    }

    @Test
    fun `mutation updates item from previously loaded page`() = runTest {
        val original = feed(id = 1L, text = "before")
        val updated = feed(id = 1L, text = "after")
        stubPage(1, original, feed(id = 2L))
        stubPage(2, feed(id = 3L), feed(id = 4L))
        val viewModel = createViewModel()

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(page = 2, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        feedMutations.tryEmit(FeedMutation.FeedSaved(updated))
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), state.content.pageData.map { it.id })
        assertSame(updated, state.content.pageData.first { it.id == 1L })
    }

    @Test
    fun `refresh replaces previously accumulated pages`() = runTest {
        doReturn(
            Result.success(pageOf(feed(id = 1L), feed(id = 2L))),
            Result.success(pageOf(feed(id = 10L), feed(id = 11L))),
        ).`when`(feedRepository).getFeedList(
            page = 1,
            perPage = 20,
            id = null,
            isFollowing = null,
            userId = null,
            type = null,
            isMixed = null,
            asHtml = false,
        )
        stubPage(2, feed(id = 3L), feed(id = 4L))
        val viewModel = createViewModel()

        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(page = 2, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        viewModel.load(page = 1, pageLimit = 20, isFollowing = null, type = null, isMixed = null)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(10L, 11L), state.content.pageData.map { it.id })
        assertEquals(setOf(1), state.loadedPages)
    }

    @Test
    fun `older refresh response cannot overwrite newer refresh`() = runTest {
        val viewModel = createViewModel()

        val olderGeneration = viewModel.beginRequestGeneration(page = 1)
        val newerGeneration = viewModel.beginRequestGeneration(page = 1)
        viewModel.applyLoadResult(
            page = 1,
            generation = newerGeneration,
            content = pageOf(feed(id = 20L, text = "new")),
        )
        viewModel.applyLoadResult(
            page = 1,
            generation = olderGeneration,
            content = pageOf(feed(id = 10L, text = "old")),
        )

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(20L), state.content.pageData.map { it.id })
        assertEquals("new", state.content.pageData.first().text)
    }

    private fun createViewModel() = FeedListViewModel(
        feedRepository = feedRepository,
        baseRepository = baseRepository,
        ioDispatcher = testDispatcher,
    )

    private suspend fun stubPage(
        page: Int,
        vararg feeds: FeedList,
    ) {
        doReturn(Result.success(pageOf(*feeds)))
            .`when`(feedRepository)
            .getFeedList(
                page = page,
                perPage = 20,
                id = null,
                isFollowing = null,
                userId = null,
                type = null,
                isMixed = null,
                asHtml = false,
            )
    }

    private fun pageOf(vararg feeds: FeedList) = PageContainer<FeedList>().apply {
        pageData = feeds.toList()
    }

    private fun feed(
        id: Long,
        text: String? = null,
    ) = FeedList(
        id = id,
        text = text,
        type = "TEXT",
    )

    private fun user(
        id: Long,
        name: String,
    ) = UserBase(name = name).apply {
        this.id = id
    }
}
