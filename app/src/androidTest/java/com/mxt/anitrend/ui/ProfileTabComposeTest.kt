@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.view.fragment.detail.ProfileFragment
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileTabComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun profileTabsAndStatsExposeComposeSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val fragmentContainerId = View.generateViewId()

            runOnUiThread {
                val content = hostActivity.findViewById<FrameLayout>(android.R.id.content)
                content.removeAllViews()
                content.addView(
                    ComposeView(hostActivity).apply {
                        id = View.generateViewId()
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            AniTrendTheme {
                                AndroidView(
                                    factory = { context -> FrameLayout(context).apply { id = fragmentContainerId } },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    },
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
            waitForIdle()
            runOnUiThread {
                hostActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(fragmentContainerId, ProfileFragment(), "profile-tabs")
                    .commitNow()
                hostActivity.findViewById<View>(R.id.profile_state_overlay).visibility = View.GONE
            }
            waitForIdle()

            val overview = hostActivity.getString(R.string.profile_section_overview)
            val progress = hostActivity.getString(R.string.profile_section_media)
            val status = hostActivity.getString(R.string.profile_section_text)
            onNodeWithText(overview).assertIsDisplayed().assertIsSelected()
            onNodeWithText(progress).assertIsDisplayed().assertIsNotSelected().performClick()
            onNodeWithText(progress).assertIsSelected()
            onNodeWithText(status).assertIsDisplayed().assertIsNotSelected().performClick()
            onNodeWithText(status).assertIsSelected()
            val animeLabel = hostActivity.getString(R.string.anime)
            onNodeWithContentDescription(animeLabel)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }
}
