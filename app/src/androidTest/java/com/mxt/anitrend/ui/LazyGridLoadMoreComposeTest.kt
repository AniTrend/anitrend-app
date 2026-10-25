@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.ui.media.LazyGridLoadMoreEffect
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.minutes

/**
 * Regression seam for the load-more effect's reactivity: an append that keeps
 * the grid within the near-end threshold must be able to request again once the
 * caller's gate reopens. Folding the condition into a Boolean suppressed the
 * follow-up because true followed by true emits nothing.
 */
@RunWith(AndroidJUnit4::class)
class LazyGridLoadMoreComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun shortAppendThatStaysNearEndRequestsAgain() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val requestCount = AtomicInteger(0)
            val rows = androidx.compose.runtime.mutableStateOf(listOf(1, 2, 3, 4))

            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            val listState = rememberLazyGridState()
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(rows.value, key = { it }) { item ->
                                    androidx.compose.material3.Text(
                                        text = "row-$item",
                                        modifier = Modifier.testTag("load-more-row-$item"),
                                    )
                                }
                            }
                            LazyGridLoadMoreEffect(gridState = listState) {
                                requestCount.incrementAndGet()
                            }
                        }
                    },
                )
            }
            waitForIdle()
            waitUntil(timeoutMillis = 5_000L) { requestCount.get() > 0 }

            // The initial short page is already near end; the caller's gate would
            // consume the first request. Appending a short page re-enters the
            // region and must re-trigger despite the condition staying true.
            val initial = requestCount.get()
            runOnUiThread { rows.value = rows.value + (rows.value.size + 1) }
            waitForIdle()
            waitUntil(timeoutMillis = 5_000L) { requestCount.get() > initial }
            assertTrue(
                "append that stays near end must request again (before=$initial after=${requestCount.get()})",
                requestCount.get() > initial,
            )
        }
    }
}
