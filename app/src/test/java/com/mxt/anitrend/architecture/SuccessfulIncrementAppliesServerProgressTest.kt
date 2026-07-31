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
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SuccessfulIncrementAppliesServerProgressTest {

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
    fun `successful increment renders authoritative server progress`() = runTest {
        val committedModel = MediaListFixtures.aMediaList(
            progress = 5,
            status = KeyUtil.CURRENT,
        )
        val command = buildIncrementMediaProgressCommand(committedModel)
        val serverResult = MediaListFixtures.aMediaList(
            id = committedModel.id,
            mediaId = committedModel.mediaId,
            progress = 6,
            status = KeyUtil.CURRENT,
        )

        val renderModel = resolveIncrementResultModel(committedModel, Result.success(serverResult))

        assertEquals(6, command.requestedProgress)
        assertEquals(6, renderModel.progress)
        assertSame(committedModel.media, renderModel.media)
        assertEquals(5, committedModel.progress)
    }
}
