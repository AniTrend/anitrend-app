package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.navigation.extension.ARG_GIPHY_PREVIEW_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GiphyPreviewActivityTest {

    // ── production parseUrl(): legacy wire bridge (exercises production code) ──

    @Test
    fun `parseUrl returns null for empty string`() {
        assertNull(GiphyPreviewActivity.parseUrl(""))
    }

    @Test
    fun `parseUrl returns null for null input`() {
        assertNull(GiphyPreviewActivity.parseUrl(null))
    }

    @Test
    fun `parseUrl constructs param for non-empty modelUrl`() {
        val modelUrl = "https://media.giphy.com/abc123/giphy.gif"
        val param = GiphyPreviewActivity.parseUrl(modelUrl)
        assertNotNull(param)
        assertEquals(modelUrl, param!!.url)
    }

    @Test
    fun `parseUrl treats blank as valid by pre-existing contract`() {
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        assertTrue(!blank.isNullOrEmpty())

        val param = GiphyPreviewActivity.parseUrl(blank)
        assertNotNull(param)
        assertEquals(blank, param!!.url)
    }

    // ── production resolve(): typed/legacy compatibility ──

    @Test
    fun `resolve prefers typed param when present and non-empty`() {
        val result = GiphyPreviewActivity.resolve(typed = GiphyPreviewScreenParam(url = "https://typed.example/giphy.gif"), legacyUrl = "https://legacy.example/giphy.gif")
        assertNotNull(result)
        assertEquals("https://typed.example/giphy.gif", result!!.url)
    }

    @Test
    fun `resolve returns null when typed param is present but empty, even with valid legacy url`() {
        // Typed parameter wins: an invalid typed param must not fall back to legacy.
        assertNull(GiphyPreviewActivity.resolve(typed = GiphyPreviewScreenParam(url = ""), legacyUrl = "https://legacy.example/giphy.gif"))
    }

    @Test
    fun `resolve falls back to legacy wire extra when typed param is absent`() {
        val result = GiphyPreviewActivity.resolve(typed = null, legacyUrl = "https://legacy.example/giphy.gif")
        assertNotNull(result)
        assertEquals("https://legacy.example/giphy.gif", result!!.url)
    }

    // ── wire keys ──

    @Test
    fun `wire key matches KeyUtil arg_model constant`() {
        assertEquals("arg_model", KeyUtil.arg_model)
    }

    @Test
    fun `stable giphy preview screen key uses destination-owned namespace`() {
        assertEquals("arg.giphy.preview.screen", ARG_GIPHY_PREVIEW_SCREEN)
    }

    @Test
    fun `screenParamKey resolves giphy preview param to its stable key`() {
        assertEquals(ARG_GIPHY_PREVIEW_SCREEN, screenParamKey<GiphyPreviewScreenParam>())
    }

    // ── parameter construction ──

    @Test
    fun `GiphyPreviewScreenParam holds url correctly`() {
        val modelUrl = "https://media.giphy.com/abc123/giphy.gif"
        val param = GiphyPreviewScreenParam(url = modelUrl)

        assertEquals(modelUrl, param.url)
    }
}
