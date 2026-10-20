@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsSectionCardComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun appearanceAndNotificationOwnersRenderSectionCardHeadersAndDividers() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        val settings = Settings(context)
        settings.theme = KeyUtil.THEME_LIGHT
        settings.edit { putString(context.getString(R.string.pref_key_startup_page), "1") }

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            onAllNodesWithContentDescription(mainActivity.getString(R.string.action_menu))[0].performClick()
            waitForIdle()
            onNodeWithText(mainActivity.getString(R.string.title_activity_settings)).performClick()
            waitForIdle()
            onNodeWithContentDescription(mainActivity.getString(R.string.pref_header_appearance)).performClick()
            waitForIdle()
            onAllNodesWithTag("settings-section-card").assertCountEquals(1)
            onNodeWithTag("settings-section-card").assertHasNoClickAction()
            onNodeWithText(mainActivity.getString(R.string.pref_header_appearance), useUnmergedTree = true)
                .assertIsDisplayed()
            onNodeWithText(mainActivity.getString(R.string.pref_header_appearance_summary), useUnmergedTree = true)
                .assertIsDisplayed()
            // 3 dividers: one under the card header plus one between each
            // pair of the section's rows.
            onAllNodesWithTag("settings-divider").assertCountEquals(3)

            onNodeWithText(mainActivity.getString(R.string.action_back)).performClick()
            waitForIdle()
            onNodeWithContentDescription(mainActivity.getString(R.string.pref_header_notifications)).performClick()
            waitForIdle()
            onAllNodesWithTag("settings-section-card").assertCountEquals(1)
            onNodeWithTag("settings-section-card").assertHasNoClickAction()
            onAllNodesWithTag("settings-divider").assertCountEquals(3)
        }
    }
}
