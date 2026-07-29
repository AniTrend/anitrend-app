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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FeedListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
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
    fun `feed saved prepends new feed to current page`() = runTest {
        val existing = feed(id = 1L, text = "old")
        val inserted = feed(id = 2L, text = "new")
        val viewModel = viewModelWithPage(existing)

        feedMutations.tryEmit(FeedMutation.FeedSaved(inserted))
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertTrue(state.replaceExisting)
        assertEquals(listOf(2L, 1L), state.content.pageData.map { it.id })
        assertEquals(listOf(2L, 1L), state.items.map { it.id })
        assertEquals(setOf(1), state.loadedPages)
        assertSame(inserted, state.content.pageData.first())
    }

    @Test
    fun `feed deleted removes matching feed from current page`() = runTest {
        val keep = feed(id = 1L)
        val remove = feed(id = 2L)
        val viewModel = viewModelWithPage(keep, remove)

        feedMutations.tryEmit(FeedMutation.FeedDeleted(remove.id))
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertTrue(state.replaceExisting)
        assertEquals(listOf(1L), state.content.pageData.map { it.id })
        assertEquals(setOf(1), state.loadedPages)
    }

    @Test
    fun `feed saved replaces existing feed in place`() = runTest {
        val original = feed(id = 5L, text = "before")
        val updated = feed(id = 5L, text = "after")
        val sibling = feed(id = 6L, text = "keep")
        val viewModel = viewModelWithPage(original, sibling)

        feedMutations.tryEmit(FeedMutation.FeedSaved(updated))
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertTrue(state.replaceExisting)
        assertEquals(listOf(5L, 6L), state.content.pageData.map { it.id })
        assertEquals(listOf(5L, 6L), state.items.map { it.id })
        assertSame(updated, state.content.pageData.first())
    }

    @Test
    fun `like toggled updates likes for matching activity feed`() = runTest {
        val feed = feed(id = 7L)
        val likes = listOf(user(id = 99L, name = "max"))
        val viewModel = viewModelWithPage(feed)

        baseMutations.tryEmit(
            BaseMutation.LikeToggled(
                users = likes,
                targetId = feed.id,
                targetType = LikeableType.ACTIVITY,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertTrue(state.replaceExisting)
        assertSame(likes, state.content.pageData.first().likes)
        assertEquals(1, state.items.first().likeCount)
    }

    @Test
    fun `like toggled ignores non activity targets`() = runTest {
        val originalLikes = listOf(user(id = 1L, name = "before"))
        val feed = feed(id = 7L).apply { likes = originalLikes }
        val otherLikes = listOf(user(id = 99L, name = "after"))
        val viewModel = viewModelWithPage(feed)

        baseMutations.tryEmit(
            BaseMutation.LikeToggled(
                users = otherLikes,
                targetId = feed.id,
                targetType = LikeableType.ACTIVITY_REPLY,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(7L), state.content.pageData.map { it.id })
        assertSame(originalLikes, state.content.pageData.first().likes)
    }

    @Test
    fun `apply returned feed updates existing feed and ignores missing feed`() = runTest {
        val original = feed(id = 3L, text = "before")
        val updated = feed(id = 3L, text = "after")
        val missing = feed(id = 4L, text = "missing")
        val viewModel = viewModelWithPage(original)

        viewModel.applyReturnedFeed(updated)
        advanceUntilIdle()

        var state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertTrue(state.replaceExisting)
        assertEquals(listOf(3L), state.content.pageData.map { it.id })
        assertEquals(setOf(1), state.loadedPages)
        assertSame(updated, state.content.pageData.first())

        viewModel.applyReturnedFeed(missing)
        advanceUntilIdle()

        state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(3L), state.content.pageData.map { it.id })
        assertSame(updated, state.content.pageData.first())
    }

    private suspend fun viewModelWithPage(vararg feeds: FeedList): FeedListViewModel {
        val page = PageContainer<FeedList>().apply { pageData = feeds.toList() }
        doReturn(Result.success(page))
            .`when`(feedRepository)
            .getFeedList(
                page = 1,
                perPage = 20,
                id = null,
                isFollowing = null,
                userId = null,
                type = null,
                isMixed = null,
                asHtml = false,
            )

        return FeedListViewModel(
            feedRepository = feedRepository,
            baseRepository = baseRepository,
            ioDispatcher = testDispatcher,
        ).also {
            it.load(
                page = 1,
                pageLimit = 20,
                isFollowing = null,
                type = null,
                isMixed = null,
            )
        }
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
