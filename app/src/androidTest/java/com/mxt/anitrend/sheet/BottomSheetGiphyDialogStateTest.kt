package com.mxt.anitrend.sheet

import android.os.Bundle
import android.widget.TextView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import com.mxt.anitrend.R
import com.mxt.anitrend.model.api.retro.base.GiphyService
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.model.entity.giphy.GiphyContainer
import com.mxt.anitrend.model.entity.giphy.Pagination
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.sheet.BottomSheetGiphy
import com.mxt.anitrend.widget.ProgressLayout
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class BottomSheetGiphyDialogStateTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun deferredFailureRetryAndSelectionReachTheDialogSurface() {
        val koin = GlobalContext.get()
        val original = koin.get<GiphyService>()
        val service = mockk<GiphyService>()
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var recovered = false
        val gif = Giphy(id = "kept", title = "Selected GIF")
        coEvery { service.getTrending(any(), any(), any(), any()) } coAnswers {
            if (!recovered) {
                started.complete(Unit)
                release.await()
                throw IOException("GIF connection failed")
            }
            Response.success(GiphyContainer(listOf(gif), Pagination(total_count = 1, count = 1)))
        }
        koin.declare(service)
        try {
            ActivityScenario.launch(ProgressLayoutTestActivity::class.java).use { scenario ->
                lateinit var sheet: BottomSheetGiphy
                var selected: Giphy? = null
                scenario.onActivity { host ->
                    sheet = BottomSheetGiphy().apply {
                        arguments = Bundle().apply { putInt(KeyUtil.arg_title, R.string.title_giphy_preview) }
                        onGiphySelected = { selected = it }
                    }
                    sheet.showNow(host.supportFragmentManager, "giphy")
                }
                compose.waitUntil(10_000L) { started.isCompleted }
                scenario.onActivity {
                    assertTrue(requireNotNull(sheet.dialog).findViewById<ProgressLayout>(R.id.stateLayout).isLoading)
                }
                release.complete(Unit)
                compose.waitUntil(10_000L) {
                    var error = false
                    scenario.onActivity {
                        error = requireNotNull(sheet.dialog).findViewById<ProgressLayout>(R.id.stateLayout).isError
                    }
                    error
                }
                scenario.onActivity {
                    val dialog = requireNotNull(sheet.dialog)
                    assertEquals("GIF connection failed", dialog.findViewById<TextView>(R.id.progressStateErrorText).text.toString())
                    recovered = true
                    dialog.findViewById<MaterialButton>(R.id.progressStateErrorAction).performClick()
                }
                compose.waitUntil(10_000L) {
                    var content = false
                    scenario.onActivity {
                        content = requireNotNull(sheet.dialog).findViewById<ProgressLayout>(R.id.stateLayout).isContent
                    }
                    content
                }
                compose.onNodeWithContentDescription("Selected GIF").assertIsDisplayed().performClick()
                compose.waitUntil(10_000L) { selected != null }
                assertEquals("kept", selected?.id)
            }
        } finally {
            release.cancel()
            koin.declare(original)
        }
    }
}
