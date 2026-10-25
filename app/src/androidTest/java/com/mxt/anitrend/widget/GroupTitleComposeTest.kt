@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.GroupTitleRow
import kotlin.time.Duration.Companion.minutes
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupTitleComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun groupHeadersExposeTitlesAndOnlyPositiveCounts() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        id = View.generateViewId()
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    GroupTitleRow(title = "Adaptations", count = 20)
                                    GroupTitleRow(title = "1984", count = 0)
                                }
                            }
                        }
                    },
                )
            }
            waitForIdle()

            onNodeWithText("Adaptations", substring = true).assertIsDisplayed()
            onNodeWithText("20").assertIsDisplayed()
            onNodeWithText("1984").assertIsDisplayed()
            onNodeWithText("0").assertDoesNotExist()
        }
    }
}
