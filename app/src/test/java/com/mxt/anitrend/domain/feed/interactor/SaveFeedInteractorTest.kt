package com.mxt.anitrend.domain.feed.interactor

import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

/**
 * Focused tests for the composer save path (ADR Phase 2 lane A).
 *
 * These prove that a successful composer submission commits through the
 * interactor/store path and that a failed submission leaves committed state
 * unchanged, without the sheet mutating a parceled legacy feed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SaveFeedInteractorTest {

    @Test
    fun `successful text save commits FeedUpserted to store`() = runTest {
        val repository = mock(FeedRepository::class.java)
        val store = InMemoryFeedStore()
        doReturn(Result.success(feed(id = 5L, text = "updated")))
            .`when`(repository)
            .saveTextActivity(5L, "updated", false, false, 1L)

        val interactor = SaveFeedInteractor(
            feedRepository = repository,
            mutationExecutor = mutationExecutor(),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(SaveFeedRequest.Text(id = 5L, text = "updated"))

        assertEquals(MutationResult.Success, result)
        assertEquals("updated", store.state.value.feedsById.getValue(5L).text)
    }

    @Test
    fun `successful message save commits FeedUpserted to store`() = runTest {
        val repository = mock(FeedRepository::class.java)
        val store = InMemoryFeedStore()
        doReturn(Result.success(feed(id = 5L, text = "hi")))
            .`when`(repository)
            .saveMessageActivity(5L, "hi", 9L, false, false, 1L)

        val interactor = SaveFeedInteractor(
            feedRepository = repository,
            mutationExecutor = mutationExecutor(),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(SaveFeedRequest.Message(id = 5L, message = "hi", recipientId = 9L))

        assertEquals(MutationResult.Success, result)
        assertEquals("hi", store.state.value.feedsById.getValue(5L).text)
    }

    @Test
    fun `failed text save returns Failure and leaves store unchanged`() = runTest {
        val repository = mock(FeedRepository::class.java)
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 5L, text = "original", revision = 0L)))
        doReturn(Result.failure<FeedList>(IllegalStateException("save failed")))
            .`when`(repository)
            .saveTextActivity(5L, "updated", false, false, 1L)

        val interactor = SaveFeedInteractor(
            feedRepository = repository,
            mutationExecutor = mutationExecutor(),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(SaveFeedRequest.Text(id = 5L, text = "updated"))

        assertTrue(result is MutationResult.Failure)
        assertEquals("original", store.state.value.feedsById.getValue(5L).text)
    }

    @Test
    fun `failed message save returns Failure and leaves store unchanged`() = runTest {
        val repository = mock(FeedRepository::class.java)
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 5L, text = "original", revision = 0L)))
        doReturn(Result.failure<FeedList>(IllegalStateException("save failed")))
            .`when`(repository)
            .saveMessageActivity(5L, "hi", 9L, false, false, 1L)

        val interactor = SaveFeedInteractor(
            feedRepository = repository,
            mutationExecutor = mutationExecutor(),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(SaveFeedRequest.Message(id = 5L, message = "hi", recipientId = 9L))

        assertTrue(result is MutationResult.Failure)
        assertEquals("original", store.state.value.feedsById.getValue(5L).text)
    }

    private fun feed(
        id: Long,
        text: String,
    ): FeedList = FeedList(
        id = id,
        type = "TEXT",
        status = "watched",
        text = text,
        replyCount = 0,
    )

    private fun createFeedRecord(
        id: Long,
        text: String,
        revision: Long,
    ): FeedRecord = FeedRecord(
        id = id,
        type = "TEXT",
        status = "watched",
        text = text,
        createdAt = 1L,
        user = null,
        messenger = null,
        recipient = null,
        media = null,
        likes = emptyList(),
        replyCount = 0,
        siteUrl = null,
        revision = revision,
    )

    private fun kotlinx.coroutines.test.TestScope.mutationExecutor(): DefaultMutationExecutor = DefaultMutationExecutor(
        applicationScope = backgroundScope,
        keyedMutex = KeyedMutex(backgroundScope),
        mutationRegistry = DefaultMutationRegistry(),
        operationIdGenerator = DefaultOperationIdGenerator(),
        sessionEpoch = SessionEpoch(),
    )
}
