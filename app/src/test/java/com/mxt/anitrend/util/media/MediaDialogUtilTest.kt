package com.mxt.anitrend.util.media

import android.content.Context
import com.mxt.anitrend.fixture.MediaListFixtures
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit tests for [MediaDialogUtil].
 *
 * The FragmentActivity path cannot be exercised in unit tests because it
 * requires a real FragmentActivity with a supportFragmentManager.
 * These tests verify the context-guard behavior only.
 */
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class MediaDialogUtilTest {

    @Test
    fun createSeriesManage_nonFragmentActivityContext_shouldNotThrow() {
        val context = mock(Context::class.java)
        val media = MediaListFixtures.anAnimeMediaBase()

        // Should return early without throwing
        MediaDialogUtil.createSeriesManage(context, media)
    }
}
