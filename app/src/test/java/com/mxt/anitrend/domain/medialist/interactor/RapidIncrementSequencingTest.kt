package com.mxt.anitrend.domain.medialist.interactor

import com.mxt.anitrend.data.mapper.toMediaListRecord
import com.mxt.anitrend.data.store.medialist.InMemoryMediaListStore
import com.mxt.anitrend.data.store.medialist.MediaListStoreChange
import com.mxt.anitrend.data.store.mutation.DefaultMutationExecutor
import com.mxt.anitrend.data.store.mutation.DefaultMutationRegistry
import com.mxt.anitrend.data.store.mutation.KeyedMutex
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.OperationStatus
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.fixture.MediaListFixtures.aMediaList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RapidIncrementSequencingTest {

    @Test
    fun `rapid increments for same media execute sequentially and keep latest progress`() = runTest {
        val store = InMemoryMediaListStore()
        val registry = DefaultMutationRegistry()
        val executor = DefaultMutationExecutor(
            applicationScope = this,
            keyedMutex = KeyedMutex(this),
            mutationRegistry = registry,
            operationIdGenerator = FixedOperationIdGenerator("increment-1", "increment-2"),
            sessionEpoch = SessionEpoch(),
        )
        val seed = aMediaList(id = 7, mediaId = 303, progress = 5).toRecord(revision = 0L)
        store.apply(MediaListStoreChange.EntryUpserted(seed))

        val order = mutableListOf<String>()
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()

        val firstJob = launch {
            executor.execute(
                resourceKey = ResourceKey.MediaListByMedia(303L),
                operationKey = OperationKey.mediaListIncrementProgress(303L),
            ) {
                order += "first-start"
                firstStarted.complete(Unit)
                releaseFirst.await()
                store.apply(MediaListStoreChange.EntryUpserted(seed.copy(progress = 6, revision = 1L)))
                order += "first-end"
                com.mxt.anitrend.data.store.mutation.MutationResult.Success
            }
        }

        runCurrent()
        assertEquals(listOf("first-start"), order)
        assertEquals(
            OperationStatus.Running(operationId = "increment-1"),
            registry.state.value[OperationKey.mediaListIncrementProgress(303L)],
        )

        val secondJob = launch {
            firstStarted.await()
            executor.execute(
                resourceKey = ResourceKey.MediaListByMedia(303L),
                operationKey = OperationKey.mediaListIncrementProgress(303L),
            ) {
                order += "second-start"
                store.apply(MediaListStoreChange.EntryUpserted(seed.copy(progress = 7, revision = 2L)))
                order += "second-end"
                com.mxt.anitrend.data.store.mutation.MutationResult.Success
            }
        }

        runCurrent()
        assertEquals(listOf("first-start"), order)

        releaseFirst.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf("first-start", "first-end", "second-start", "second-end"), order)
        assertEquals(7, store.state.value.entriesById.getValue(7L).progress)
        assertTrue(registry.state.value.isEmpty())
        firstJob.join()
        secondJob.join()
    }

    private fun com.mxt.anitrend.model.entity.anilist.MediaList.toRecord(revision: Long) = toMediaListRecord(revision = revision, ownerUserId = 1L)

    private class FixedOperationIdGenerator(
        private vararg val operationIds: String,
    ) : com.mxt.anitrend.data.store.mutation.OperationIdGenerator {
        private var index = 0

        override fun generate(): String = operationIds.getOrNull(index++)
            ?: error("No operation ID configured for index $index")
    }
}
