@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.StudioAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudioAdapterComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun studioRowsExposeNamesAndOnlyShowFavoriteMarkerForFavoriteModels() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: StudioHost
            runOnUiThread {
                host =
                    installStudioHost(
                        activity = hostActivity,
                        onOpenStudio = {},
                    )
            }
            waitUntil("studio rows bind", 5_000L) {
                host.adapter.itemCount == 2 &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null &&
                    host.recyclerView.findViewHolderForAdapterPosition(1) != null
            }
            waitForIdle()

            onNodeWithText("Studio One").assertIsDisplayed()
            onNodeWithText("Studio Two").assertIsDisplayed()
            onAllNodesWithTag("studio-favourite-marker", useUnmergedTree = true).assertCountEquals(1)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun studioRowClickOpensTheExistingStudioOwner() {
        val openedStudioIds = CopyOnWriteArrayList<Long>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: StudioHost
            runOnUiThread {
                host =
                    installStudioHost(
                        activity = hostActivity,
                        onOpenStudio = openedStudioIds::add,
                    )
            }
            waitUntil("studio rows bind", 5_000L) {
                host.adapter.itemCount == 2 &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            onNodeWithText("Studio One").assertIsDisplayed()
            runOnUiThread {
                requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView.performClick()
            }
            runOnIdle { assertEquals(listOf(101L), openedStudioIds.toList()) }
        }
    }

    private fun installStudioHost(
        activity: ProgressLayoutTestActivity,
        onOpenStudio: (Long) -> Unit,
    ): StudioHost {
        val adapter =
            StudioAdapter(activity).apply {
                setClickListener(
                    object : ItemClickListener<StudioBase> {
                        override fun onItemClick(
                            target: View,
                            data: IndexedValue<StudioBase>,
                        ) {
                            if (target.id == R.id.container) onOpenStudio(data.value.id)
                        }

                        override fun onItemLongClick(
                            target: View,
                            data: IndexedValue<StudioBase>,
                        ) = Unit
                    },
                )
                onItemsInserted(
                    listOf(
                        studio(id = 101L, name = "Studio One", isFavourite = false),
                        studio(id = 202L, name = "Studio Two", isFavourite = true),
                    ),
                )
            }
        val recyclerView =
            RecyclerView(activity).apply {
                layoutManager =
                    StaggeredGridLayoutManager(
                        activity.resources.getInteger(R.integer.grid_list_x2),
                        StaggeredGridLayoutManager.VERTICAL,
                    )
                itemAnimator = null
                this.adapter = adapter
            }
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
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
        return StudioHost(adapter, recyclerView)
    }

    private fun studio(
        id: Long,
        name: String,
        isFavourite: Boolean,
    ): StudioBase = StudioBase().apply {
        this.id = id
        this.name = name
        this.isFavourite = isFavourite
    }

    private data class StudioHost(
        val adapter: StudioAdapter,
        val recyclerView: RecyclerView,
    )
}
