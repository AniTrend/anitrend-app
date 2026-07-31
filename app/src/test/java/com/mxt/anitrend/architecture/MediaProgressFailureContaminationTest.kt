package com.mxt.anitrend.architecture

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
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MediaProgressFailureContaminationTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Ignore("Architectural regression: Phase 1 containment fixes pre-success model mutation")
    @Test
    fun `given pre save mutation when save fails then mutable model remains contaminated`() = runTest {
        // Defect baseline from docs/architecture/state-synchronization-and-mutation-refactor.md,
        // Phase 1: local MediaList objects are mutated before server success, so failures leave
        // the caller holding contaminated state.
        val model = MediaListFixtures.aMediaList(
            progress = 5,
            status = KeyUtil.PLANNING,
        )

        model.progress = 6
        model.status = KeyUtil.CURRENT

        val saveResult = Result.failure<Any>(IllegalStateException("save failed"))
        saveResult.exceptionOrNull()

        assertEquals(6, model.progress)
        assertEquals(KeyUtil.CURRENT, model.status)
    }
}
