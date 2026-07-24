package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImagePreviewActivityTest {

    // ── parseArgs null-safety (exercises production code) ──

    @Test
    fun `parseArgs returns null for empty string`() {
        assertNull(ImagePreviewActivity.parseArgs(""))
    }

    @Test
    fun `parseArgs returns null for null input`() {
        assertNull(ImagePreviewActivity.parseArgs(null))
    }

    @Test
    fun `parseArgs constructs Args for non-empty modelUrl`() {
        val modelUrl = "https://example.com/image.jpg"
        val args = ImagePreviewActivity.parseArgs(modelUrl)
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
        val modelUrl = "https://example.com/image.jpg"
        val args = ImagePreviewActivity.Args(modelUrl)

        assertEquals(modelUrl, args.modelUrl)
    }

    @Test
    fun `parseArgs treats blank as valid by pre-existing contract`() {
        // Documenting: isNullOrEmpty allows blank strings, preserving the
        // original getStringExtra + null-check behaviour. A blank URL will
        // reach Glide and fail silently there (pre-existing behaviour).
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        assertTrue(!blank.isNullOrEmpty())

        val args = ImagePreviewActivity.parseArgs(blank)
        assertNotNull(args)
        assertEquals(blank, args!!.modelUrl)
    }
}
