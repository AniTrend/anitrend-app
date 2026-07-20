package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GiphyPreviewActivityTest {

    // ── parseArgs null-safety (exercises production code) ──

    @Test
    fun `parseArgs returns null for empty string`() {
        assertNull(GiphyPreviewActivity.parseArgs(""))
    }

    @Test
    fun `parseArgs returns null for null input`() {
        assertNull(GiphyPreviewActivity.parseArgs(null))
    }

    @Test
    fun `parseArgs constructs Args for non-empty modelUrl`() {
        val modelUrl = "https://media.giphy.com/abc123/giphy.gif"
        val args = GiphyPreviewActivity.parseArgs(modelUrl)
        assertNotNull(args)
        assertEquals(modelUrl, args!!.modelUrl)
    }

    // ── wire key ──

    @Test
    fun `wire key matches KeyUtil arg_model constant`() {
        assertEquals("arg_model", KeyUtil.arg_model)
    }

    // ── Args data class ──

    @Test
    fun `Args holds modelUrl correctly`() {
        val modelUrl = "https://media.giphy.com/abc123/giphy.gif"
        val args = GiphyPreviewActivity.Args(modelUrl)

        assertEquals(modelUrl, args.modelUrl)
    }

    @Test
    fun `parseArgs treats blank as valid by pre-existing contract`() {
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        assertTrue(!blank.isNullOrEmpty())

        val args = GiphyPreviewActivity.parseArgs(blank)
        assertNotNull(args)
        assertEquals(blank, args!!.modelUrl)
    }
}
