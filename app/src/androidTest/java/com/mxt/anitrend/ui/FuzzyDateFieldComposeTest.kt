@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.medialist.FuzzyDateField
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FuzzyDateFieldComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun dateFieldDisplaysUnsetPartialAndFullDatesAndOpensPickerCallback() {
        var pickerRequests = 0

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val testActivity = requireNotNull(activity)
            runOnUiThread {
                val content = testActivity.findViewById<FrameLayout>(android.R.id.content)
                content.removeAllViews()
                val composeView = ComposeView(testActivity).apply {
                    id = View.generateViewId()
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                    setContent {
                        AniTrendTheme("light") {
                            Column {
                                FuzzyDateField(value = "TBA", contentDescription = "Start date, TBA", onClick = { pickerRequests++ })
                                FuzzyDateField(value = "2024", contentDescription = "Start date, 2024", onClick = {})
                                FuzzyDateField(value = "Jun, 2024", contentDescription = "Start date, Jun, 2024", onClick = {})
                                FuzzyDateField(value = "Jun 15, 2024", contentDescription = "Start date, Jun 15, 2024", onClick = {})
                            }
                        }
                    }
                }
                content.addView(
                    composeView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ),
                )
            }
            waitForIdle()

            onNodeWithText("TBA").assertIsDisplayed()
            onNodeWithContentDescription("Start date, TBA")
                .assertHeightIsEqualTo(48.dp)
                .assertHasClickAction()
                .performClick()
            onNodeWithText("2024").assertIsDisplayed()
            onNodeWithText("Jun, 2024").assertIsDisplayed()
            onNodeWithText("Jun 15, 2024").assertIsDisplayed()

            runOnIdle {
                assertEquals(1, pickerRequests)
            }
        }
    }
}
