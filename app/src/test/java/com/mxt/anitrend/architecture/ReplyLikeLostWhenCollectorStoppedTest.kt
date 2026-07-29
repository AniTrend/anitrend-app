package com.mxt.anitrend.architecture

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReplyLikeLostWhenCollectorStoppedTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    sealed interface TestEvent {
        data object Ping : TestEvent
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given no active collector when shared flow emits then later collector receives nothing`() = runTest {
        val events = MutableSharedFlow<TestEvent>(replay = 0, extraBufferCapacity = 64)

        assertTrue(events.tryEmit(TestEvent.Ping))

        val received = mutableListOf<TestEvent>()
        val collector = backgroundScope.launch(testDispatcher) {
            events.collect { received += it }
        }
        advanceUntilIdle()

        assertTrue(received.isEmpty())

        collector.cancel()
    }
}
