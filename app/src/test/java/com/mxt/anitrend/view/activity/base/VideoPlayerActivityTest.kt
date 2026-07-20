package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoPlayerActivityTest {

    // ── parseArgs null-safety (exercises production code) ──

    @Test
    fun `parseArgs returns null for empty string`() {
        assertNull(VideoPlayerActivity.parseArgs(""))
    }

    @Test
    fun `parseArgs returns null for null input`() {
        assertNull(VideoPlayerActivity.parseArgs(null))
    }

    @Test
    fun `parseArgs constructs Args for non-empty contentLink`() {
        val link = "https://example.com/video.mp4"
        val args = VideoPlayerActivity.parseArgs(link)
        assertNotNull(args)
        assertEquals(link, args!!.contentLink)
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
    fun `parseArgs treats blank as valid by pre-existing contract`() {
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        assertTrue(!blank.isNullOrEmpty())

        val args = VideoPlayerActivity.parseArgs(blank)
        assertNotNull(args)
        assertEquals(blank, args!!.contentLink)
    }
}
