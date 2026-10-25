@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.GenreGrid
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GenreGridComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun genreNamesExposeTextSemanticsInTwoColumns() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installGenreGrid(
                    activity = hostActivity,
                    genres = sampleGenres,
                    onOpenGenre = {},
                )
            }
            waitForIdle()

            val sliceOfLife = onNodeWithText("Slice Of Life")
            val adventure = onNodeWithText("Adventure")
            val comedy = onNodeWithText("Comedy")
            sliceOfLife.assertIsDisplayed()
            adventure.assertIsDisplayed()
            comedy.assertIsDisplayed()

            val firstBounds = sliceOfLife.fetchSemanticsNode().boundsInRoot
            val secondBounds = adventure.fetchSemanticsNode().boundsInRoot
            val thirdBounds = comedy.fetchSemanticsNode().boundsInRoot
            assertEquals(firstBounds.top, secondBounds.top, 1f)
            assertTrue(secondBounds.left > firstBounds.left)
            assertTrue(thirdBounds.top > firstBounds.top)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun genreClickRoutesTheSelectedNameToOwner() {
        val openedGenres = CopyOnWriteArrayList<String>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installGenreGrid(
                    activity = hostActivity,
                    genres = sampleGenres,
                    onOpenGenre = openedGenres::add,
                )
            }
            waitForIdle()

            onNodeWithText("Adventure").performClick()

            runOnIdle { assertEquals(listOf("Adventure"), openedGenres.toList()) }
        }
    }

    private fun installGenreGrid(
        activity: ProgressLayoutTestActivity,
        genres: List<Genre>,
        onOpenGenre: (String) -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        GenreGrid(
                            genres = genres,
                            modifier = Modifier.fillMaxSize(),
                            onOpenGenre = onOpenGenre,
                        )
                    }
                }
            },
        )
    }

    private companion object {
        val sampleGenres = listOf(
            Genre(genre = "Slice Of Life"),
            Genre(genre = "Adventure"),
            Genre(genre = "Comedy"),
        )
    }
}
