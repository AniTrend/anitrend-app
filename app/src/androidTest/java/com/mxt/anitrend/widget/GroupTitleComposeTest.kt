@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupCharacterAdapter
import com.mxt.anitrend.model.entity.group.RecyclerHeaderItem
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupTitleComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun groupHeadersExposeTitlesAndOnlyPositiveCounts() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var adapter: GroupCharacterAdapter
            lateinit var recyclerView: RecyclerView
            runOnUiThread {
                adapter =
                    GroupCharacterAdapter(hostActivity).apply {
                        onItemsInserted(
                            listOf(
                                RecyclerHeaderItem("Adaptations", 20),
                                RecyclerHeaderItem("1984", 0, false),
                            ),
                        )
                    }
                recyclerView =
                    RecyclerView(hostActivity).apply {
                        layoutManager =
                            StaggeredGridLayoutManager(
                                hostActivity.resources.getInteger(R.integer.grid_list_x2),
                                StaggeredGridLayoutManager.VERTICAL,
                            )
                        itemAnimator = null
                        this.adapter = adapter
                    }
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        id = android.view.View.generateViewId()
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                AndroidView(
                                    factory = { recyclerView },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    },
                )
            }
            waitUntil("group headers bind", 5_000L) {
                adapter.itemCount == 2 &&
                    recyclerView.findViewHolderForAdapterPosition(0) != null &&
                    recyclerView.findViewHolderForAdapterPosition(1) != null
            }
            waitForIdle()

            onNodeWithText("Adaptations", substring = true).assertIsDisplayed()
            onNodeWithText("20").assertIsDisplayed()
            onNodeWithText("1984").assertIsDisplayed()
            onNodeWithText("0").assertDoesNotExist()
        }
    }
}
