package com.mxt.anitrend.worker

import com.mxt.anitrend.model.entity.container.body.AniListContainer
import com.mxt.anitrend.model.entity.container.body.DataContainer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkerExtTest {

    @Test
    fun `unwrapBody of null returns null`() {
        val result: Int? = unwrapBody(null)
        assertNull(result)
    }

    @Test
    fun `unwrapBody of AniListContainer returns unwrapped data`() {
        val data = DataContainer(result = "hello")
        val container: AniListContainer<String> = AniListContainer(data = data, errors = null)
        val result = unwrapBody<String>(container)
        assertEquals("hello", result)
    }

    @Test
    fun `unwrapBody of AniListContainer with null data returns null`() {
        val container: AniListContainer<String> = AniListContainer(data = null, errors = null)
        val result = unwrapBody<String>(container)
        assertNull(result)
    }

    @Test
    fun `unwrapBody of AniListContainer with null result returns null`() {
        val data = DataContainer<String>(result = null)
        val container: AniListContainer<String> = AniListContainer(data = data, errors = null)
        val result = unwrapBody<String>(container)
        assertNull(result)
    }

    @Test
    fun `unwrapBody of raw body returns the body directly`() {
        val rawBody = 42
        val result = unwrapBody<Int>(rawBody)
        assertEquals(42, result)
    }

    @Test
    fun `unwrapBody of wildcard container with correct type cast`() {
        val data = DataContainer(result = "value")
        // Use a wildcard container (simulating erased type)
        val container: AniListContainer<*> = AniListContainer(data = data, errors = null)
        val result = unwrapBody<String>(container)
        assertEquals("value", result)
    }
}
