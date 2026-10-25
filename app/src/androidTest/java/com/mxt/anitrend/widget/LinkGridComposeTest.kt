@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.model.entity.anilist.ExternalLink
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.LinkGrid
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LinkGridComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun linkRowExposesSiteAndOpensOwnerOnTap() {
        val openedUrls = CopyOnWriteArrayList<String>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installLinkGrid(
                    activity = hostActivity,
                    onLinkClick = { link -> openedUrls += link.url.orEmpty() },
                    onLinkLongClick = {},
                )
            }
            waitForIdle()

            onNodeWithText("Twitter").assertIsDisplayed()
            onNodeWithText("Twitter").performClick()

            runOnIdle { assertEquals(listOf("https://twitter.com"), openedUrls.toList()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun linkRowLongClickReachesTheExistingCopyOwnerAction() {
        val copiedUrls = CopyOnWriteArrayList<String>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installLinkGrid(
                    activity = hostActivity,
                    onLinkClick = {},
                    onLinkLongClick = { link -> copiedUrls += link.url.orEmpty() },
                )
            }
            waitForIdle()

            val row = onNodeWithText("Twitter")
            row.assertIsDisplayed()
            row.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            row.performTouchInput { longClick() }

            runOnIdle { assertEquals(listOf("https://twitter.com"), copiedUrls.toList()) }
        }
    }

    private fun installLinkGrid(
        activity: ProgressLayoutTestActivity,
        onLinkClick: (ExternalLink) -> Unit,
        onLinkLongClick: (ExternalLink) -> Unit,
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        LinkGrid(
                            links = listOf(ExternalLink(url = "https://twitter.com", site = "Twitter")),
                            modifier = Modifier.fillMaxSize(),
                            onLinkClick = onLinkClick,
                            onLinkLongClick = onLinkLongClick,
                        )
                    }
                }
            },
        )
    }
}
