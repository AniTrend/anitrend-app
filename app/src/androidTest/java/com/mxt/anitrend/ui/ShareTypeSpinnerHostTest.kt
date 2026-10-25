@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.widget.FrameLayout
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.share.SharedContentScreen
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareTypeSpinnerHostTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun shareSheetChipsKeepSelection() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val submitted = mutableListOf<String>()

            runOnUiThread {
                val content = hostActivity.findViewById<FrameLayout>(android.R.id.content)
                content.removeAllViews()
                val composeView =
                    androidx.compose.ui.platform.ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(
                            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                        )
                        setContent {
                            AniTrendTheme("light") {
                                SharedContentScreen(
                                    sharedText = "https://example.com",
                                    subject = null,
                                    onSubmit = { submitted.add(it) },
                                    onClose = {},
                                )
                            }
                        }
                    }
                content.addView(
                    composeView,
                    FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
            waitForIdle()

            val typeLabelResIds =
                listOf(
                    R.string.post_share_type_plain_text,
                    R.string.post_share_type_website,
                    R.string.post_share_type_picture,
                    R.string.post_share_type_youtube,
                    R.string.post_share_type_video,
                )
            val labels = typeLabelResIds.map { hostActivity.getString(it) }
            // The sheet renders its header as text, not a content description.
            onAllNodesWithText(hostActivity.getString(R.string.menu_title_new_activity_post))[0].assertIsDisplayed()
            // Later chips can sit below the fold; existence, not display.
            labels.forEach { label -> onAllNodesWithText(label).assertCountEquals(1) }

            // Plain is the default selection; picking the image chip moves it.
            onNodeWithText(labels[0]).assertIsSelected()
            onNodeWithText(labels[2]).assertIsNotSelected().performClick()
            onNodeWithText(labels[2]).assertIsSelected()
            onNodeWithText(labels[0]).assertIsNotSelected()
        }
    }
}
