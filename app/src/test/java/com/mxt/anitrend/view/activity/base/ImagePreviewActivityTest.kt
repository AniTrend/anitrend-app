package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.navigation.extension.ARG_IMAGE_PREVIEW_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImagePreviewActivityTest {

    // ── production parseUrl(): legacy wire bridge (exercises production code) ──

    @Test
    fun `parseUrl returns null for empty string`() {
        assertNull(ImagePreviewActivity.parseUrl(""))
    }

    @Test
    fun `parseUrl returns null for null input`() {
        assertNull(ImagePreviewActivity.parseUrl(null))
    }

    @Test
    fun `parseUrl constructs param for non-empty modelUrl`() {
        val modelUrl = "https://example.com/image.jpg"
        val param = ImagePreviewActivity.parseUrl(modelUrl)
        assertNotNull(param)
        assertEquals(modelUrl, param!!.url)
    }

    @Test
    fun `parseUrl treats blank as valid by pre-existing contract`() {
        // Documenting: isNullOrEmpty allows blank strings, preserving the
        // original getStringExtra + null-check behaviour. A blank URL will
        // reach Glide and fail silently there (pre-existing behaviour).
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        assertTrue(!blank.isNullOrEmpty())

        val param = ImagePreviewActivity.parseUrl(blank)
        assertNotNull(param)
        assertEquals(blank, param!!.url)
    }

    // ── production resolve(): typed/legacy compatibility ──

    @Test
    fun `resolve prefers typed param when present and non-empty`() {
        val result = ImagePreviewActivity.resolve(typed = ImagePreviewScreenParam(url = "https://typed.example/image.jpg"), legacyUrl = "https://legacy.example/image.jpg")
        assertNotNull(result)
        assertEquals("https://typed.example/image.jpg", result!!.url)
    }

    @Test
    fun `resolve returns null when typed param is present but empty, even with valid legacy url`() {
        // Typed parameter wins: an invalid typed param must not fall back to legacy.
        assertNull(ImagePreviewActivity.resolve(typed = ImagePreviewScreenParam(url = ""), legacyUrl = "https://legacy.example/image.jpg"))
    }

    @Test
    fun `resolve falls back to legacy wire extra when typed param is absent`() {
        val result = ImagePreviewActivity.resolve(typed = null, legacyUrl = "https://legacy.example/image.jpg")
        assertNotNull(result)
        assertEquals("https://legacy.example/image.jpg", result!!.url)
    }

    // ── wire keys ──

    @Test
    fun `wire key matches KeyUtil arg_model constant`() {
        assertEquals("arg_model", KeyUtil.arg_model)
    }

    @Test
    fun `stable image preview screen key uses destination-owned namespace`() {
        assertEquals("arg.image.preview.screen", ARG_IMAGE_PREVIEW_SCREEN)
    }

    @Test
    fun `screenParamKey resolves image preview param to its stable key`() {
        assertEquals(ARG_IMAGE_PREVIEW_SCREEN, screenParamKey<ImagePreviewScreenParam>())
    }

    // ── parameter construction ──

    @Test
    fun `ImagePreviewScreenParam holds url correctly`() {
        val modelUrl = "https://example.com/image.jpg"
        val param = ImagePreviewScreenParam(url = modelUrl)

        assertEquals(modelUrl, param.url)
    }
}
