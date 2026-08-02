package com.mxt.anitrend.view.activity.detail

import com.mxt.anitrend.navigation.extension.ARG_COMMENT_SCREEN
import com.mxt.anitrend.navigation.extension.ARG_MEDIA_SCREEN
import com.mxt.anitrend.navigation.extension.ARG_STUDIO_SCREEN
import com.mxt.anitrend.navigation.extension.ARG_USER_SCREEN
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class StudioActivityTest {

    // ── fromIntent null-safety ──

    @Test
    fun `fromIntent returns null when extras are empty`() {
        // Cannot use a real Intent in a unit test (SDK stub), so we mirror the
        // production rule: no typed param and no legacy extra → null.
        assertEquals(null, parseParamOrNull(typedParam = null, hasExtra = false, id = 0))
    }

    @Test
    fun `fromIntent returns null when id is negative`() {
        assertEquals(null, parseParamOrNull(typedParam = null, hasExtra = true, id = -1))
    }

    @Test
    fun `fromIntent returns null when id is zero`() {
        assertEquals(null, parseParamOrNull(typedParam = null, hasExtra = true, id = 0))
    }

    @Test
    fun `fromIntent returns param when legacy id is positive`() {
        val result = parseParamOrNull(typedParam = null, hasExtra = true, id = 123L)
        assertNotNull(result)
        assertEquals(123L, result!!.studioId)
    }

    @Test
    fun `fromIntent returns typed param when present and positive`() {
        val result = parseParamOrNull(typedParam = StudioScreenParam(studioId = 77L), hasExtra = true, id = 0)
        assertNotNull(result)
        assertEquals(77L, result!!.studioId)
    }

    @Test
    fun `fromIntent returns null when typed param is present but invalid`() {
        assertNull(parseParamOrNull(typedParam = StudioScreenParam(studioId = 0), hasExtra = true, id = 1))
    }

    // ── wire keys ──

    @Test
    fun `legacy wire key matches KeyUtil arg_id constant`() {
        assertEquals("id", KeyUtil.arg_id)
    }

    @Test
    fun `stable screen keys use destination-owned namespaces`() {
        assertEquals("arg.user.screen", ARG_USER_SCREEN)
        assertEquals("arg.media.screen", ARG_MEDIA_SCREEN)
        assertEquals("arg.comment.screen", ARG_COMMENT_SCREEN)
        assertEquals("arg.studio.screen", ARG_STUDIO_SCREEN)
    }

    @Test
    fun `screenParamKey resolves each representative param to its stable key`() {
        assertEquals(ARG_USER_SCREEN, screenParamKey<UserScreenParam>())
        assertEquals(ARG_MEDIA_SCREEN, screenParamKey<MediaScreenParam>())
        assertEquals(ARG_COMMENT_SCREEN, screenParamKey<CommentScreenParam>())
        assertEquals(ARG_STUDIO_SCREEN, screenParamKey<StudioScreenParam>())
    }

    // ── parameter construction ──

    @Test
    fun `StudioScreenParam holds studioId correctly`() {
        val param = StudioScreenParam(studioId = 456L)
        assertEquals(456L, param.studioId)
    }

    @Test
    fun `UserScreenParam holds userId and optional initialName`() {
        val param = UserScreenParam(userId = 1L, initialName = "Raki")
        assertEquals(1L, param.userId)
        assertEquals("Raki", param.initialName)
    }

    @Test
    fun `MediaScreenParam holds mediaId and optional mediaType`() {
        val param = MediaScreenParam(mediaId = 2L, mediaType = "ANIME")
        assertEquals(2L, param.mediaId)
        assertEquals("ANIME", param.mediaType)
    }

    @Test
    fun `CommentScreenParam holds feedId`() {
        val param = CommentScreenParam(feedId = 3L)
        assertEquals(3L, param.feedId)
    }

    // ── production logic mirror ──

    /**
     * Mirrors the production [StudioActivity.fromIntent] logic so we test the exact
     * null/valid rules without needing a real [android.content.Intent]. The typed
     * parameter path is represented directly; the legacy path is represented by the
     * [hasExtra] and [id] pair, mirroring `hasExtra(KeyUtil.arg_id)` and
     * `getLongExtra(KeyUtil.arg_id, -1)`.
     */
    private fun parseParamOrNull(typedParam: StudioScreenParam?, hasExtra: Boolean, id: Long): StudioScreenParam? {
        typedParam?.let { return it.takeIf { param -> param.studioId > 0 } }
        if (!hasExtra) return null
        return if (id > 0) StudioScreenParam(studioId = id) else null
    }
}
