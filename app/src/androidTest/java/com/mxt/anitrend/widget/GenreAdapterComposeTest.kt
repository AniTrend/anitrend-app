@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
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
import com.mxt.anitrend.adapter.recycler.detail.GenreAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenreAdapterComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun genreRowExposesNameInComposeSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: GenreHost
            runOnUiThread {
                host = installGenreHost(
                    activity = hostActivity,
                    onOpenGenre = {},
                )
            }
            waitUntil("genre row binds", 5_000L) {
                host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            onNodeWithText("Slice Of Life").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun genreRowForwardsClickToExistingOwner() {
        val openedGenres = CopyOnWriteArrayList<String>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: GenreHost
            runOnUiThread {
                host = installGenreHost(
                    activity = hostActivity,
                    onOpenGenre = { openedGenres.add(it) },
                )
            }
            waitUntil("genre row binds", 5_000L) {
                host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            runOnUiThread {
                requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView.performClick()
            }
            runOnIdle { assertEquals(listOf("Slice Of Life"), openedGenres.toList()) }
        }
    }

    private fun installGenreHost(
        activity: ProgressLayoutTestActivity,
        onOpenGenre: (String) -> Unit,
    ): GenreHost {
        val adapter = GenreAdapter(activity).apply {
            setClickListener(
                object : ItemClickListener<Genre> {
                    override fun onItemClick(
                        target: View,
                        data: IndexedValue<Genre>,
                    ) {
                        if (target.id == R.id.container) onOpenGenre(data.value.genre.orEmpty())
                    }

                    override fun onItemLongClick(
                        target: View,
                        data: IndexedValue<Genre>,
                    ) = Unit
                },
            )
            onItemsInserted(
                listOf(
                    Genre(genre = "Slice Of Life"),
                    Genre(genre = "Adventure"),
                ),
            )
        }
        val recyclerView = RecyclerView(activity).apply {
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
        return GenreHost(adapter, recyclerView)
    }

    private data class GenreHost(
        val adapter: GenreAdapter,
        val recyclerView: RecyclerView,
    )
}
