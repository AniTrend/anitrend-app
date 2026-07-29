package com.mxt.anitrend.architecture

import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.domain.model.resolveIncrementResultModel
import com.mxt.anitrend.fixture.MediaListFixtures
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FailedIncrementLeavesProgressUnchangedTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `failed increment keeps committed media list state unchanged`() = runTest {
        val committedModel = MediaListFixtures.aMediaList(
            progress = 5,
            status = KeyUtil.PLANNING,
        )

        val command = buildIncrementMediaProgressCommand(committedModel, MediaListFixtures.aFuzzyDate(2026, 7, 29))
        val renderModel = resolveIncrementResultModel(committedModel, Result.failure(IllegalStateException("save failed")))

        assertEquals(6, command.requestedProgress)
        assertEquals(5, committedModel.progress)
        assertEquals(KeyUtil.PLANNING, committedModel.status)
        assertNull(committedModel.startedAt)
        assertNull(committedModel.completedAt)
        assertEquals(5, renderModel.progress)
        assertEquals(KeyUtil.PLANNING, renderModel.status)
    }
}
