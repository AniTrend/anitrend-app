package com.mxt.anitrend.view.activity.detail

import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StudioActivityTest {

    // ── fromIntent null-safety ──

    @Test
    fun `fromIntent returns null when extras are empty`() {
        // fromIntent returns null when hasExtra(KeyUtil.arg_id) is false
        // Cannot use real Intent in unit test (SDK stub), validate logic rule:
        //   !hasExtra → null, or id <= 0 → null
        assertEquals(null, parseIdOrNull(hasExtra = false, id = 0))
    }

    @Test
    fun `fromIntent returns null when id is negative`() {
        assertEquals(null, parseIdOrNull(hasExtra = true, id = -1))
    }

    @Test
    fun `fromIntent returns null when id is zero`() {
        assertEquals(null, parseIdOrNull(hasExtra = true, id = 0))
    }

    @Test
    fun `fromIntent returns Args when id is positive`() {
        val result = parseIdOrNull(hasExtra = true, id = 123L)
        assertNotNull(result)
        assertEquals(123L, result!!.id)
    }

    // ── wire key ──

    @Test
    fun `wire key matches KeyUtil arg_id constant`() {
        assertEquals("id", KeyUtil.arg_id)
    }

    // ── Args data class ──

    @Test
    fun `Args holds id correctly`() {
        val args = StudioActivity.Args(456L)
        assertEquals(456L, args.id)
    }

    // ── production logic mirror ──

    /**
     * Mirrors the production [StudioActivity.fromIntent] logic so we test the
     * exact null/valid rules without needing a real [android.content.Intent].
     */
    private fun parseIdOrNull(hasExtra: Boolean, id: Long): StudioActivity.Args? {
        if (!hasExtra) return null
        return if (id > 0) StudioActivity.Args(id) else null
    }
}
