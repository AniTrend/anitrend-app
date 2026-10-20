@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
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
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsToggleRowComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun notificationSwitchesExposeStateAndPersistUserChanges() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        Settings(context).theme = KeyUtil.THEME_LIGHT
        val settings = Settings(context)
        val messageKey = context.getString(R.string.pref_key_new_message_notifications)
        val workaroundKey = context.getString(R.string.pref_key_notification_work_around)
        settings.edit {
            putBoolean(messageKey, true)
            putBoolean(workaroundKey, false)
            putString(context.getString(R.string.pref_key_startup_page), "1")
        }

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            onAllNodesWithContentDescription(mainActivity.getString(R.string.action_menu))[0].performClick()
            waitForIdle()
            onNodeWithText(mainActivity.getString(R.string.title_activity_settings)).performClick()
            waitForIdle()
            onNodeWithContentDescription(mainActivity.getString(R.string.pref_header_notifications)).performClick()
            waitForIdle()

            onAllNodesWithTag("settings-toggle-row").assertCountEquals(3)

            val messageTitle = mainActivity.getString(R.string.pref_title_new_message_notifications)
            val messageOnDescription =
                mainActivity.getString(
                    R.string.settings_toggle_content_description,
                    messageTitle,
                    mainActivity.getString(R.string.accessibility_switch_on),
                )
            onNodeWithContentDescription(messageOnDescription)
                .assertIsDisplayed()
                .assertIsOn()
                .assertHasClickAction()
                .performClick()

            waitForIdle()
            assertFalse(settings.getBoolean(messageKey, true))
            val messageOffDescription =
                mainActivity.getString(
                    R.string.settings_toggle_content_description,
                    messageTitle,
                    mainActivity.getString(R.string.accessibility_switch_off),
                )
            onNodeWithContentDescription(messageOffDescription).assertIsOff()

            val workaroundTitle = mainActivity.getString(R.string.pref_title_notification_work_around)
            val workaroundDescription =
                mainActivity.getString(
                    R.string.settings_toggle_content_description,
                    workaroundTitle,
                    mainActivity.getString(R.string.accessibility_switch_off),
                )
            onNodeWithContentDescription(workaroundDescription)
                .assertIsDisplayed()
                .assertIsOff()
                .assertIsNotEnabled()
                .assertHasNoClickAction()
            onNodeWithText(mainActivity.getString(R.string.pref_summary_notification_work_around), useUnmergedTree = true)
                .assertIsDisplayed()
            assertFalse(settings.getBoolean(workaroundKey, false))
        }
    }
}
