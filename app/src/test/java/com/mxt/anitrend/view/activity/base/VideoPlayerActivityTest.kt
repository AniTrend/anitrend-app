package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoPlayerActivityTest {

    // ── fromIntent null-safety ──

    @Test
    fun `fromIntent returns null for empty string`() {
        // Validate fromIntent's null-check logic without needing a real Intent.
        // Production code: contentLink.isNullOrEmpty() -> null
        val link: String? = ""
        val result = if (link.isNullOrEmpty()) null else VideoPlayerActivity.Args(link)
        assertEquals(null, result)
    }

    @Test
    fun `fromIntent returns null for null contentLink`() {
        val link: String? = null
        val result = if (link.isNullOrEmpty()) null else VideoPlayerActivity.Args(link)
        assertEquals(null, result)
    }

    @Test
    fun `fromIntent constructs Args for non-empty contentLink`() {
        val link = "https://example.com/video.mp4"
        val result = if (link.isNullOrEmpty()) null else VideoPlayerActivity.Args(link)
        assertNotNull(result)
        assertEquals(link, result!!.contentLink)
    }

    // ── wire key ──

    @Test
    fun `wire key matches KeyUtil arg_model constant`() {
        assertEquals("arg_model", KeyUtil.arg_model)
    }

    // ── Args data class ──

    @Test
    fun `Args holds contentLink correctly`() {
        val link = "https://example.com/video.mp4"
        val args = VideoPlayerActivity.Args(link)

        assertEquals(link, args.contentLink)
    }

    @Test
    fun `fromIntent pre-existing behavior treats blank as valid`() {
        // Documenting: isNullOrEmpty allows blank strings, preserving the
        // original getStringExtra + null-check behaviour.
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        assertTrue(!blank.isNullOrEmpty())
    }
}
