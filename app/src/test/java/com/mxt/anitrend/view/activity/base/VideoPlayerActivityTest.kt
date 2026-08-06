package com.mxt.anitrend.view.activity.base

import com.mxt.anitrend.navigation.extension.ARG_VIDEO_PLAYER_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoPlayerActivityTest {

    // ── production parseUrl(): legacy wire bridge (exercises production code) ──

    @Test
    fun `parseUrl returns null for empty string`() {
        assertNull(VideoPlayerActivity.parseUrl(""))
    }

    @Test
    fun `parseUrl returns null for null input`() {
        assertNull(VideoPlayerActivity.parseUrl(null))
    }

    @Test
    fun `parseUrl constructs param for non-empty contentLink`() {
        val link = "https://example.com/video.mp4"
        val param = VideoPlayerActivity.parseUrl(link)
        assertNotNull(param)
        assertEquals(link, param!!.url)
    }

    @Test
    fun `parseUrl treats blank as valid by pre-existing contract`() {
        val blank = "   "
        assertTrue(blank.isNullOrBlank())
        assertTrue(!blank.isNullOrEmpty())

        val param = VideoPlayerActivity.parseUrl(blank)
        assertNotNull(param)
        assertEquals(blank, param!!.url)
    }

    // ── production resolve(): typed/legacy compatibility ──

    @Test
    fun `resolve prefers typed param when present and non-empty`() {
        val result = VideoPlayerActivity.resolve(typed = VideoPlayerScreenParam(url = "https://typed.example/video.mp4"), legacyUrl = "https://legacy.example/video.mp4")
        assertNotNull(result)
        assertEquals("https://typed.example/video.mp4", result!!.url)
    }

    @Test
    fun `resolve returns null when typed param is present but empty, even with valid legacy url`() {
        // Typed parameter wins: an invalid typed param must not fall back to legacy.
        assertNull(VideoPlayerActivity.resolve(typed = VideoPlayerScreenParam(url = ""), legacyUrl = "https://legacy.example/video.mp4"))
    }

    @Test
    fun `resolve falls back to legacy wire extra when typed param is absent`() {
        val result = VideoPlayerActivity.resolve(typed = null, legacyUrl = "https://legacy.example/video.mp4")
        assertNotNull(result)
        assertEquals("https://legacy.example/video.mp4", result!!.url)
    }

    // ── wire keys ──

    @Test
    fun `wire key matches KeyUtil arg_model constant`() {
        assertEquals("arg_model", KeyUtil.arg_model)
    }

    @Test
    fun `stable video player screen key uses destination-owned namespace`() {
        assertEquals("arg.video.player.screen", ARG_VIDEO_PLAYER_SCREEN)
    }

    @Test
    fun `screenParamKey resolves video player param to its stable key`() {
        assertEquals(ARG_VIDEO_PLAYER_SCREEN, screenParamKey<VideoPlayerScreenParam>())
    }

    // ── parameter construction ──

    @Test
    fun `VideoPlayerScreenParam holds url correctly`() {
        val link = "https://example.com/video.mp4"
        val param = VideoPlayerScreenParam(url = link)

        assertEquals(link, param.url)
    }
}
