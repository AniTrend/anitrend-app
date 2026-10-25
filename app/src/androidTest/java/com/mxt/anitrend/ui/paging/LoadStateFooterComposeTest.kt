@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui.paging

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.paging.LoadState
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.TestSessionUtil
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoadStateFooterComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun loadingErrorRetryAndNotLoadingStatesRenderCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        val retryCalls = AtomicInteger()
        val loadingLabel = context.getString(R.string.Loading)
        val retryLabel = context.getString(R.string.button_try_again)
        val loadState = mutableStateOf<LoadState>(LoadState.Loading)

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                LoadStateFooter(LoadStateFooterUiState.from(loadState.value), onRetry = { retryCalls.incrementAndGet() })
                            }
                        }
                    },
                )
            }
            waitForIdle()

            onNodeWithText(loadingLabel).assertIsDisplayed()
            onNodeWithTag("load-state-footer-progress").assertIsDisplayed()
            onAllNodesWithText(retryLabel).assertCountEquals(0)

            runOnUiThread {
                loadState.value = LoadState.Error(IllegalStateException("append failed"))
            }
            waitForIdle()
            onAllNodesWithText(loadingLabel).assertCountEquals(0)
            onAllNodesWithTag("load-state-footer-progress").assertCountEquals(0)
            onNodeWithText(retryLabel)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            runOnIdle { assertEquals(1, retryCalls.get()) }

            runOnUiThread {
                loadState.value = LoadState.Loading
            }
            waitForIdle()
            onAllNodesWithText(retryLabel).assertCountEquals(0)
            onNodeWithText(loadingLabel).assertIsDisplayed()
            onNodeWithTag("load-state-footer-progress").assertIsDisplayed()

            runOnUiThread {
                loadState.value = LoadState.NotLoading(endOfPaginationReached = false)
            }
            waitForIdle()
            onAllNodesWithText(loadingLabel).assertCountEquals(0)
            onAllNodesWithText(retryLabel).assertCountEquals(0)
            onAllNodesWithTag("load-state-footer-progress").assertCountEquals(0)
        }
    }
}
