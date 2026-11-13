@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import android.os.SystemClock
import android.view.MotionEvent
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
import com.mxt.anitrend.adapter.recycler.detail.LinkAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkAdapterComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun linkRowExposesSiteAndOpensOwnerOnNativeTap() {
        val openedUrls = CopyOnWriteArrayList<String>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: LinkHost
            runOnUiThread {
                host =
                    installLinkHost(
                        activity = hostActivity,
                        onOpenLink = openedUrls::add,
                        onCopyLink = {},
                    )
            }
            waitUntil("link row binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val itemView = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            onNodeWithText("Twitter").assertIsDisplayed()
            runOnUiThread {
                val downTime = SystemClock.uptimeMillis()
                val x = itemView.width / 2f
                val y = itemView.height / 2f
                val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
                val up = MotionEvent.obtain(downTime, downTime + 10, MotionEvent.ACTION_UP, x, y, 0)
                try {
                    itemView.dispatchTouchEvent(down)
                    itemView.dispatchTouchEvent(up)
                } finally {
                    down.recycle()
                    up.recycle()
                }
            }
            runOnIdle { assertEquals(listOf("https://twitter.com"), openedUrls.toList()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun linkRowLongClickReachesTheExistingCopyOwnerAction() {
        val copiedUrls = CopyOnWriteArrayList<String>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: LinkHost
            runOnUiThread {
                host =
                    installLinkHost(
                        activity = hostActivity,
                        onOpenLink = {},
                        onCopyLink = copiedUrls::add,
                    )
            }
            waitUntil("link row binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val itemView = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            onNodeWithText("Twitter").assertIsDisplayed()
            runOnUiThread { assertTrue(itemView.performLongClick()) }
            runOnIdle { assertEquals(listOf("https://twitter.com"), copiedUrls.toList()) }
        }
    }

    private fun installLinkHost(
        activity: ProgressLayoutTestActivity,
        onOpenLink: (String) -> Unit,
        onCopyLink: (String) -> Unit,
    ): LinkHost {
        val adapter =
            LinkAdapter(activity).apply {
                onItemsInserted(listOf(ExternalLink(url = "https://twitter.com", site = "Twitter")))
                setClickListener(
                    object : ItemClickListener<ExternalLink> {
                        override fun onItemClick(
                            target: View,
                            data: IndexedValue<ExternalLink>,
                        ) {
                            if (target.id == R.id.container) data.value.url?.let(onOpenLink)
                        }

                        override fun onItemLongClick(
                            target: View,
                            data: IndexedValue<ExternalLink>,
                        ) {
                            if (target.id == R.id.container) data.value.url?.let(onCopyLink)
                        }
                    },
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
        return LinkHost(adapter, recyclerView)
    }

    private data class LinkHost(
        val adapter: LinkAdapter,
        val recyclerView: RecyclerView,
    )
}
