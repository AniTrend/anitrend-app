package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.repository.MediaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SecondaryFeedViewModelStoreObservationTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var feedRepository: FeedRepository
    private lateinit var mediaRepository: MediaRepository
    private lateinit var store: InMemoryFeedStore

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        feedRepository = mock(FeedRepository::class.java)
        mediaRepository = mock(MediaRepository::class.java)
        store = InMemoryFeedStore()
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `message feed view model observes inbox query from feed store`() = runTest {
        val queryKey = FeedQueryKey(
            scope = FeedScope.MESSAGE_INBOX,
            userId = 42L,
            mediaId = null,
            activityType = null,
            isFollowing = null,
            isMixed = null,
        )
        val viewModel = MessageFeedViewModel(feedRepository = feedRepository, feedStore = store)
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        stubMessagePage(queryKey = queryKey, page = 1, generation = 1, feeds = listOf(feed(1L), feed(2L)))
        stubMessagePage(queryKey = queryKey, page = 2, generation = 1, feeds = listOf(feed(3L), feed(4L)))

        viewModel.load(userId = 42L, page = 1, pageLimit = 20, messageType = 0)
        viewModel.load(userId = 42L, page = 2, pageLimit = 20, messageType = 0)
        advanceUntilIdle()

        val state = viewModel.state.value as MessageFeedViewModel.UiState.Success
        assertEquals(listOf(1L, 2L, 3L, 4L), state.content.pageData.map { it.id })
        collector.cancel()
    }

    @Test
    fun `message feed apply returned feed commits detail into store`() = runTest {
        val viewModel = MessageFeedViewModel(feedRepository = feedRepository, feedStore = store)
        val feed = feed(8L).apply {
            replyCount = 1
            replies = listOf(FeedReply(id = 99L, text = "reply", createdAt = 10L))
        }

        viewModel.applyReturnedFeed(feed)
        advanceUntilIdle()

        assertEquals(1, store.state.value.feedsById.getValue(8L).replyCount)
        assertEquals(listOf(99L), store.state.value.replyIdsByFeedId.getValue(8L))
    }

    @Test
    fun `media feed view model observes media query from feed store`() = runTest {
        val queryKey = FeedQueryKey(
            scope = FeedScope.MEDIA,
            userId = null,
            mediaId = 10L,
            activityType = null,
            isFollowing = true,
            isMixed = null,
        )
        val viewModel = MediaFeedViewModel(mediaRepository = mediaRepository, feedStore = store)
        val collector = backgroundScope.launch { viewModel.state.collect {} }

        stubMediaPage(queryKey = queryKey, page = 1, generation = 1, feeds = listOf(feed(10L), feed(11L)))
        stubMediaPage(queryKey = queryKey, page = 2, generation = 1, feeds = listOf(feed(12L)))

        viewModel.load(mediaId = 10L, isFollowing = true, page = 1, pageLimit = 20)
        viewModel.load(mediaId = 10L, isFollowing = true, page = 2, pageLimit = 20)
        advanceUntilIdle()

        val state = viewModel.state.value as MediaFeedViewModel.UiState.Success
        assertEquals(listOf(10L, 11L, 12L), state.content.pageData.map { it.id })
        collector.cancel()
    }

    private fun stubMessagePage(
        queryKey: FeedQueryKey,
        page: Int,
        generation: Int,
        feeds: List<FeedList>,
    ) {
        val content = PageContainer<FeedList>().apply { pageData = feeds }
        runBlocking {
            store.apply(
                FeedStoreChange.PageLoaded(
                    queryKey = queryKey,
                    page = page,
                    generation = generation,
                    feeds = feeds.map { it.toFeedRecord(revision = 0L) },
                    pageInfo = null,
                ),
            )

            doReturn(Result.success(content))
                .`when`(feedRepository)
                .getFeedMessage(
                    page = page,
                    perPage = 20,
                    messengerId = null,
                    userId = 42L,
                    asHtml = false,
                    commitToStore = true,
                    queryKey = queryKey,
                    queryGeneration = generation,
                )
        }
    }

    private fun stubMediaPage(
        queryKey: FeedQueryKey,
        page: Int,
        generation: Int,
        feeds: List<FeedList>,
    ) {
        val content = PageContainer<FeedList>().apply { pageData = feeds }
        runBlocking {
            store.apply(
                FeedStoreChange.PageLoaded(
                    queryKey = queryKey,
                    page = page,
                    generation = generation,
                    feeds = feeds.map { it.toFeedRecord(revision = 0L) },
                    pageInfo = null,
                ),
            )

            doReturn(Result.success(content))
                .`when`(mediaRepository)
                .getMediaSocial(
                    mediaId = 10L,
                    isFollowing = true,
                    page = page,
                    perPage = 20,
                    commitToStore = true,
                    queryKey = queryKey,
                    queryGeneration = generation,
                )
        }
    }

    private fun feed(id: Long): FeedList = FeedList(
        id = id,
        type = "MESSAGE",
        status = "sent",
        text = "feed-$id",
    )
}
