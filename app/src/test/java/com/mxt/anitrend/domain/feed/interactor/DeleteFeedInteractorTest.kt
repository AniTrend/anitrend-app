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
import com.mxt.anitrend.domain.model.DeleteFeedCommand
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class DeleteFeedInteractorTest {

    @Test
    fun `successful delete commits FeedDeleted to store`() = runTest {
        val repository = mock(FeedRepository::class.java)
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 5L, revision = 0L)))
        doReturn(Result.success(DeleteState(isDeleted = true)))
            .`when`(repository)
            .deleteActivity(5L, false, 1L)

        val interactor = DeleteFeedInteractor(
            feedRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(DeleteFeedCommand(feedId = 5L))

        assertEquals(MutationResult.Success, result)
        assertFalse(store.state.value.feedsById.containsKey(5L))
    }

    @Test
    fun `failed delete returns Failure`() = runTest {
        val repository = mock(FeedRepository::class.java)
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 5L, revision = 0L)))
        val failure = IllegalStateException("delete failed")
        doReturn(Result.failure<DeleteState>(failure))
            .`when`(repository)
            .deleteActivity(5L, false, 1L)

        val interactor = DeleteFeedInteractor(
            feedRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(DeleteFeedCommand(feedId = 5L))

        assertTrue(result is MutationResult.Failure)
        assertTrue(store.state.value.feedsById.containsKey(5L))
    }

    private fun createFeedRecord(
        id: Long,
        revision: Long,
    ): FeedRecord = FeedRecord(
        id = id,
        type = "TEXT",
        status = "watched",
        text = "feed-$id",
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
}
