@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.changelog.ChangelogScreen
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.viewmodel.ChangelogViewModel
import com.mxt.anitrend.viewmodel.ChangelogViewModel.ChangelogUiState
import io.noties.markwon.Markwon
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangelogComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun bundledReleaseTogglesExpansionAndReturnsToCaller() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        val viewModel = ChangelogViewModel(context.assets)

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val markwon = Markwon.builder(mainActivity).build()
            var returnedToCaller = false

            runOnUiThread {
                mainActivity.setContent {
                    val state by viewModel.state.collectAsState()
                    AniTrendTheme(theme = KeyUtil.THEME_LIGHT) {
                        ChangelogScreen(
                            state = state,
                            markwon = markwon,
                            onLinkClick = {},
                            onRetry = viewModel::retry,
                            onBackClick = { returnedToCaller = true },
                        )
                    }
                }
            }

            waitUntil(timeoutMillis = 30_000) {
                viewModel.state.value !is ChangelogUiState.Loading
            }
            assertTrue(viewModel.state.value is ChangelogUiState.Content)
            waitForIdle()

            // The bundled release renders expanded under the What's new heading.
            onNodeWithText(mainActivity.getString(R.string.changelog_whats_new)).assertIsDisplayed()
            val releaseTitle = mainActivity.getString(R.string.app_name) + " v" + BuildConfig.versionName
            onNodeWithText(releaseTitle).assertIsDisplayed()
            val categoryAdded = mainActivity.getString(R.string.changelog_category_added)
            onNodeWithText(categoryAdded).assertIsDisplayed()

            // Toggling the release card collapses its categories...
            onNodeWithText(releaseTitle).performClick()
            waitForIdle()
            onAllNodesWithText(categoryAdded).assertCountEquals(0)

            // ...and expands them again.
            onNodeWithText(releaseTitle).performClick()
            waitForIdle()
            onNodeWithText(categoryAdded).assertIsDisplayed()

            // Back calls the owning entry's return action.
            onNodeWithText(mainActivity.getString(R.string.action_back)).performClick()
            waitForIdle()
            runOnIdle { assertTrue(returnedToCaller) }
        }
    }
}
