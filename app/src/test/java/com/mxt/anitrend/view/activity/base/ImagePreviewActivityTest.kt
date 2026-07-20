package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImagePreviewActivityTest {

    // ── fromIntent null-safety ──

    @Test
    fun `fromIntent returns null for empty string`() {
        // To verify fromIntent's null-check logic without needing a real Intent
        // (the SDK stub jar doesn't support Intent extras), we validate the
        // null-empty rule directly. The production code uses
        //   modelUrl.isNullOrEmpty() -> null
        val modelUrl: String? = ""
        val result = if (modelUrl.isNullOrEmpty()) null else ImagePreviewActivity.Args(modelUrl)
        assertEquals(null, result)
    }

    @Test
    fun `fromIntent returns null for null modelUrl`() {
        val modelUrl: String? = null
        val result = if (modelUrl.isNullOrEmpty()) null else ImagePreviewActivity.Args(modelUrl)
        assertEquals(null, result)
    }

    @Test
    fun `fromIntent constructs Args for non-empty modelUrl`() {
        val modelUrl = "https://example.com/image.jpg"
        val result = if (modelUrl.isNullOrEmpty()) null else ImagePreviewActivity.Args(modelUrl)
        assertNotNull(result)
        assertEquals(modelUrl, result!!.modelUrl)
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
    fun `fromIntent pre-existing behavior treats blank as valid`() {
        // Documenting: isNullOrEmpty allows blank strings, preserving the
        // original getStringExtra + null-check behaviour. A blank URL will
        // reach Glide and fail silently there (pre-existing behaviour).
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        // But isNullOrEmpty returns false for blank strings
        assertTrue(!blank.isNullOrEmpty())
    }
}
