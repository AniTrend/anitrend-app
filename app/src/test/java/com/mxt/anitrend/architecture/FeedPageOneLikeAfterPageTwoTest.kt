package com.mxt.anitrend.architecture

import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BaseMutation
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.FeedMutation
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.viewmodel.FeedListViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class FeedPageOneLikeAfterPageTwoTest {

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
    fun `given page one then page two when page one like toggles then accumulated state reduces it`() = runTest {
        doReturn(Result.success(pageOf(feed(id = 1L), feed(id = 2L))))
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
        doReturn(Result.success(pageOf(feed(id = 3L), feed(id = 4L))))
            .`when`(feedRepository)
            .getFeedList(
                page = 2,
                perPage = 20,
                id = null,
                isFollowing = null,
                userId = null,
                type = null,
                isMixed = null,
                asHtml = false,
            )

        val viewModel = FeedListViewModel(
            feedRepository = feedRepository,
            baseRepository = baseRepository,
            ioDispatcher = testDispatcher,
        )

        viewModel.load(
            page = 1,
            pageLimit = 20,
            isFollowing = null,
            type = null,
            isMixed = null,
        )
        advanceUntilIdle()

        viewModel.load(
            page = 2,
            pageLimit = 20,
            isFollowing = null,
            type = null,
            isMixed = null,
        )
        advanceUntilIdle()

        val newLikes = listOf(user(id = 99L, name = "max"))
        baseMutations.tryEmit(
            BaseMutation.LikeToggled(
                users = newLikes,
                targetId = 1L,
                targetType = LikeableType.ACTIVITY,
            ),
        )
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), state.content.pageData.map { it.id })
        assertEquals(newLikes, state.content.pageData.first { it.id == 1L }.likes)
        assertTrue(state.loadedPages.containsAll(setOf(1, 2)))
    }

    private fun pageOf(vararg feeds: FeedList) = PageContainer<FeedList>().apply {
        pageData = feeds.toList()
    }

    private fun feed(id: Long) = FeedList(
        id = id,
        type = "TEXT",
    )

    private fun user(
        id: Long,
        name: String,
    ) = UserBase(name = name).apply {
        this.id = id
    }
}
