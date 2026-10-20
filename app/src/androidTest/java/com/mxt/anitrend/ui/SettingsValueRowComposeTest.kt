@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.viewmodel.AppearanceSettingsViewModel
import kotlin.time.Duration.Companion.minutes
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsValueRowComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun settingsValueRowsPreserveChoiceBehavior() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        val settings = Settings(context)
        settings.theme = KeyUtil.THEME_LIGHT
        settings.userLanguage = AppearanceSettingsViewModel.DEFAULT_LANGUAGE
        settings.edit {
            putString(context.getString(R.string.pref_key_list_view_style), AppearanceSettingsViewModel.DEFAULT_LIST_VIEW_STYLE)
            putString(context.getString(R.string.pref_key_startup_page), "1")
        }
        runAndroidComposeUiTest<MainActivity>(testTimeout = 3.minutes) {
            val mainActivity = requireNotNull(activity)
            onAllNodesWithContentDescription(mainActivity.getString(R.string.action_menu))[0].performClick()
            waitForIdle()
            onNodeWithText(mainActivity.getString(R.string.title_activity_settings)).performClick()
            waitForIdle()
            onNodeWithContentDescription(mainActivity.getString(R.string.pref_header_appearance)).performClick()
            waitForIdle()

            val choiceRows = onAllNodesWithTag("settings-value-row")
            choiceRows.assertCountEquals(3)
            choiceRows[0].assertHeightIsEqualTo(56.dp)
            choiceRows[1].assertHeightIsEqualTo(74.dp)
            choiceRows[2].assertHeightIsEqualTo(56.dp)
            onAllNodesWithTag("settings-value-row-value", useUnmergedTree = true).assertCountEquals(3)
            onAllNodesWithTag("settings-value-row-chevron", useUnmergedTree = true).assertCountEquals(3)
            val themeValues = mainActivity.resources.getStringArray(R.array.pref_selected_theme_values)
            val themeTitles = mainActivity.resources.getStringArray(R.array.pref_selected_theme_titles)
            val selectedThemeLabel = themeTitles[themeValues.indexOf(KeyUtil.THEME_LIGHT)]
            val themeDescription =
                mainActivity.getString(
                    R.string.settings_choice_content_description,
                    mainActivity.getString(R.string.pref_title_app_theme),
                    selectedThemeLabel,
                )
            onNodeWithContentDescription(themeDescription)
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()
                .performClick()
            // Choice rows cycle inline (no dialog): each click advances to the
            // next entry and the merged description announces the new value.
            fun themeDescriptionFor(index: Int) = mainActivity.getString(
                R.string.settings_choice_content_description,
                mainActivity.getString(R.string.pref_title_app_theme),
                themeTitles[index],
            )
            onNodeWithContentDescription(themeDescriptionFor(1)).assertIsDisplayed()
            onNodeWithContentDescription(themeDescriptionFor(1)).performClick()
            waitForIdle()
            onNodeWithContentDescription(themeDescriptionFor(2)).assertIsDisplayed()
            onNodeWithContentDescription(themeDescriptionFor(2)).performClick()
            waitForIdle()
            onNodeWithContentDescription(themeDescriptionFor(0)).assertIsDisplayed()
            waitForIdle()
        }
    }
}
