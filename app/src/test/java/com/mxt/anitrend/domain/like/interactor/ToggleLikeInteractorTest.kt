package com.mxt.anitrend.domain.like.interactor

import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.data.store.feed.InMemoryFeedStore
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.model.ToggleLikeCommand
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.repository.BaseRepository
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class ToggleLikeInteractorTest {

    @Test
    fun `successful toggle commits FeedLikesReplaced to store`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 1L, revision = 0L)))
        val users = listOf(createUserEntity(11L), createUserEntity(12L))
        doReturn(Result.success(users))
            .`when`(repository)
            .toggleLike(1L, LikeableType.ACTIVITY, false, null, 1L)

        val interactor = ToggleLikeInteractor(
            baseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(ToggleLikeCommand(id = 1L, likeableType = LikeableType.ACTIVITY))

        assertEquals(MutationResult.Success, result)
        assertEquals(listOf(11L, 12L), store.state.value.feedsById.getValue(1L).likes.map(UserSummaryRecord::id))
        assertEquals(1L, store.state.value.feedsById.getValue(1L).revision)
    }

    @Test
    fun `failed toggle returns MutationResult Failure and does not commit`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 1L, revision = 0L)))
        val failure = IllegalStateException("boom")
        doReturn(Result.failure<List<UserBase>>(failure))
            .`when`(repository)
            .toggleLike(1L, LikeableType.ACTIVITY, false, null, 1L)

        val interactor = ToggleLikeInteractor(
            baseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val result = interactor(ToggleLikeCommand(id = 1L, likeableType = LikeableType.ACTIVITY))

        assertTrue(result is MutationResult.Failure)
        assertTrue(store.state.value.feedsById.getValue(1L).likes.isEmpty())
        assertEquals(0L, store.state.value.feedsById.getValue(1L).revision)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `two toggles on same feed execute sequentially`() = runTest {
        val repository = mock(BaseRepository::class.java)
        val store = InMemoryFeedStore()
        store.apply(FeedStoreChange.FeedUpserted(createFeedRecord(id = 1L, revision = 0L)))
        val firstStarted = CompletableDeferred<Unit>()
        val allowFirstReturn = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        val activeCalls = AtomicInteger(0)
        val maxActiveCalls = AtomicInteger(0)

        doAnswer {
            val current = activeCalls.incrementAndGet()
            maxActiveCalls.updateAndGet { previous -> maxOf(previous, current) }
            try {
                firstStarted.complete(Unit)
                runBlocking { allowFirstReturn.await() }
                Result.success(listOf(createUserEntity(1L)))
            } finally {
                activeCalls.decrementAndGet()
            }
        }.`when`(repository)
            .toggleLike(1L, LikeableType.ACTIVITY, false, null, 1L)
        doAnswer {
            val current = activeCalls.incrementAndGet()
            maxActiveCalls.updateAndGet { previous -> maxOf(previous, current) }
            try {
                secondStarted.complete(Unit)
                Result.success(listOf(createUserEntity(2L)))
            } finally {
                activeCalls.decrementAndGet()
            }
        }.`when`(repository)
            .toggleLike(1L, LikeableType.ACTIVITY, false, null, 2L)

        val interactor = ToggleLikeInteractor(
            baseRepository = repository,
            mutationExecutor = DefaultMutationExecutor(applicationScope = backgroundScope, keyedMutex = KeyedMutex(backgroundScope), mutationRegistry = DefaultMutationRegistry(), operationIdGenerator = com.mxt.anitrend.data.store.mutation.DefaultOperationIdGenerator(), sessionEpoch = SessionEpoch()),
            feedStore = store,
            requestSequence = RequestSequence(),
        )

        val first = launch(Dispatchers.Default) {
            interactor(ToggleLikeCommand(id = 1L, likeableType = LikeableType.ACTIVITY))
        }
        firstStarted.await()

        val second = launch(Dispatchers.Default) {
            interactor(ToggleLikeCommand(id = 1L, likeableType = LikeableType.ACTIVITY))
        }
        advanceUntilIdle()
        assertTrue(!secondStarted.isCompleted)

        allowFirstReturn.complete(Unit)

        first.join()
        second.join()
        assertTrue(secondStarted.isCompleted)
        assertEquals(1, maxActiveCalls.get())
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

    private fun createUserEntity(id: Long): UserBase = UserBase(name = "user-$id").apply {
        this.id = id
    }
}
