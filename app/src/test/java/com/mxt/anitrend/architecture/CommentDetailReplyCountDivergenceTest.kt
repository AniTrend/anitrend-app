package com.mxt.anitrend.architecture

import com.mxt.anitrend.model.entity.anilist.FeedList
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
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class CommentDetailReplyCountDivergenceTest {

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
    fun `given returned feed patch when sibling also changed elsewhere then only returned feed is synchronised`() = runTest {
        val originalFeed = feed(id = 10L, replyCount = 2)
        val siblingFeed = feed(id = 11L, replyCount = 4)
        val updatedFeed = feed(id = 10L, replyCount = 5)
        val independentlyChangedSibling = feed(id = 11L, replyCount = 8)

        doReturn(Result.success(pageOf(originalFeed, siblingFeed)))
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

        viewModel.applyReturnedFeed(updatedFeed)
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals(5, state.content.pageData.first { it.id == 10L }.replyCount)
        assertEquals(4, state.content.pageData.first { it.id == 11L }.replyCount)
        assertNotEquals(
            independentlyChangedSibling.replyCount,
            state.content.pageData.first { it.id == 11L }.replyCount,
        )
        // Defect baseline: CommentActivity returns one complete FeedList, so this manual
        // synchronisation channel can patch only the explicitly returned entity. Fix: Phase 5.
    }

    private fun pageOf(vararg feeds: FeedList) = PageContainer<FeedList>().apply {
        pageData = feeds.toList()
    }

    private fun feed(
        id: Long,
        replyCount: Int,
    ) = FeedList(
        id = id,
        replyCount = replyCount,
        type = "TEXT",
    )
}
