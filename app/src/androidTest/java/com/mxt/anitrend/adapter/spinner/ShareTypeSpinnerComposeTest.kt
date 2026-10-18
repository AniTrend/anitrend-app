@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.adapter.spinner

import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.endsWith
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareTypeSpinnerComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun selectedAndDropdownRowsExposeLabelsAndKeepSpinnerSelection() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val options = hostActivity.resources.getStringArray(R.array.post_share_types).toList()
            val spinner = createSpinner(hostActivity, options)

            runOnUiThread {
                hostActivity.setContentView(
                    ComposeView(hostActivity).apply {
                        id = View.generateViewId()
                        setViewCompositionStrategy(
                            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                        )
                        setContent {
                            AniTrendTheme {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AndroidView(
                                        factory = { spinner },
                                        modifier = Modifier.wrapContentSize(Alignment.TopStart),
                                    )
                                }
                            }
                        }
                    },
                )
            }
            waitForIdle()

            val plainText = onNodeWithText(options[KeyUtil.PLAIN_TYPE])
            plainText.assertIsDisplayed()
            val expectedLabelStartPx =
                hostActivity.resources.getDimension(R.dimen.mg_margin) +
                    hostActivity.resources.getDimension(R.dimen.xl_margin) +
                    hostActivity.resources.getDimension(R.dimen.lg_margin)
            assertEquals(
                expectedLabelStartPx,
                plainText.fetchSemanticsNode().boundsInRoot.left,
                hostActivity.resources.displayMetrics.density,
            )
            val xmlTextProbe =
                SingleLineTextView(hostActivity).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    text = options[KeyUtil.PLAIN_TYPE]
                }
            xmlTextProbe.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            )
            val composeTextBounds = plainText.fetchSemanticsNode().boundsInRoot
            assertEquals(
                "nativeSize=${xmlTextProbe.textSize / hostActivity.resources.displayMetrics.scaledDensity}sp, " +
                    "nativeLetterSpacing=${xmlTextProbe.letterSpacing}, nativeTypeface=${xmlTextProbe.typeface}, " +
                    "nativeWidth=${xmlTextProbe.measuredWidth}px, composeWidth=${composeTextBounds.width}px",
                xmlTextProbe.measuredWidth.toFloat(),
                composeTextBounds.width,
                hostActivity.resources.displayMetrics.density,
            )
            runOnUiThread { spinner.performClick() }
            waitForIdle()

            onData(equalTo(options[KeyUtil.WEBM_TYPE]))
                .inAdapterView(withClassName(endsWith("DropDownListView")))
                .inRoot(isPlatformPopup())
                .perform(click())
            runOnUiThread { assertEquals(KeyUtil.WEBM_TYPE, spinner.selectedItemPosition) }
            waitUntil("Spinner popup dismissal", 5_000L) {
                onAllNodesWithText(options[KeyUtil.WEBM_TYPE]).fetchSemanticsNodes().size == 1
            }
            onNodeWithText(options[KeyUtil.WEBM_TYPE]).assertIsDisplayed()

            runOnUiThread { spinner.performClick() }
            waitForIdle()
            onData(equalTo(options[KeyUtil.IMAGE_TYPE]))
                .inAdapterView(withClassName(endsWith("DropDownListView")))
                .inRoot(isPlatformPopup())
                .perform(click())
            runOnUiThread { assertEquals(KeyUtil.IMAGE_TYPE, spinner.selectedItemPosition) }
        }
    }

    private fun createSpinner(
        context: ProgressLayoutTestActivity,
        options: List<String>,
    ): Spinner {
        val adapter =
            IconArrayAdapter(context, context, options).apply {
                setIndexIconMap(
                    mapOf(
                        KeyUtil.PLAIN_TYPE to R.drawable.ic_textsms_white_24dp,
                        KeyUtil.LINK_TYPE to R.drawable.ic_link_white_24dp,
                        KeyUtil.IMAGE_TYPE to R.drawable.ic_crop_original_white_24dp,
                        KeyUtil.YOUTUBE_TYPE to R.drawable.ic_youtube,
                        KeyUtil.WEBM_TYPE to R.drawable.ic_slow_motion_video_white_24dp,
                    ),
                )
            }
        return Spinner(context).apply {
            id = View.generateViewId()
            this.adapter = adapter
        }
    }
}
