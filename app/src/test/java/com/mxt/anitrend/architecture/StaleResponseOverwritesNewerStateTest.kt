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
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class StaleResponseOverwritesNewerStateTest {

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

    @Ignore("Architectural regression: Phase 3 introduces revision rejection in stores")
    @Test
    fun `given newer feed saved when older response arrives later then older state overwrites newer`() = runTest {
        // Defect baseline from docs/architecture/state-synchronization-and-mutation-refactor.md,
        // Phase 3: there is no revision ordering, so later stale responses still overwrite state.
        doReturn(Result.success(pageOf(feed(id = 1L, text = "original"))))
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

        feedMutations.tryEmit(FeedMutation.FeedSaved(feed(id = 1L, text = "newer")))
        feedMutations.tryEmit(FeedMutation.FeedSaved(feed(id = 1L, text = "older")))
        advanceUntilIdle()

        val state = viewModel.state.value as FeedListViewModel.UiState.Success
        assertEquals("older", state.content.pageData.first().text)
    }

    private fun pageOf(vararg feeds: FeedList) = PageContainer<FeedList>().apply {
        pageData = feeds.toList()
    }

    private fun feed(
        id: Long,
        text: String,
    ) = FeedList(
        id = id,
        text = text,
        type = "TEXT",
    )
}
