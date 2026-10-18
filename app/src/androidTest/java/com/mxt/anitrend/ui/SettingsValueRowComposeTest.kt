@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.content.edit
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.extension.asBundle
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry
import com.mxt.anitrend.viewmodel.CustomizeSettingsViewModel
import kotlin.time.Duration.Companion.minutes
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsValueRowComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun settingsValueRowsPreserveChoiceAndInfoBehavior() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        val settings = Settings(context)
        settings.theme = KeyUtil.THEME_LIGHT
        settings.userLanguage = CustomizeSettingsViewModel.DEFAULT_LANGUAGE
        settings.edit {
            putString(
                context.getString(R.string.pref_key_list_view_style),
                CustomizeSettingsViewModel.DEFAULT_LIST_VIEW_STYLE,
            )
        }
        runAndroidComposeUiTest<MainActivity>(testTimeout = 3.minutes) {
            val mainActivity = requireNotNull(activity)
            val navHost =
                mainActivity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
            val navController = navHost.navController

            runOnUiThread {
                navController.navigate(R.id.customizeSettingsFragment)
            }
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
            InstrumentationRegistry.getInstrumentation()
                .waitForFocusedWindowContainingText(
                    themeTitles[1],
                    AccessibilityWindowInfo.TYPE_APPLICATION,
                )
            onView(withText(themeTitles[1])).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withText(mainActivity.getString(R.string.Cancel)))
                .inRoot(isDialog())
                .perform(click())
            waitForIdle()

            runOnUiThread {
                navController.navigate(
                    R.id.settingsCategoryFragment,
                    SettingsCategoryScreenParam(categoryId = SettingsCategoryRegistry.CONTENT).asBundle(),
                )
            }
            waitForIdle()

            val infoRows = onAllNodesWithTag("settings-value-row")
            infoRows.assertCountEquals(4)
            for (index in 0 until 4) {
                infoRows[index].assertHeightIsEqualTo(58.dp)
            }
            infoRows[0]
                .assertIsDisplayed()
                .assertHasNoClickAction()
            onNodeWithText(mainActivity.getString(R.string.pref_title_autoplay))
                .assertIsDisplayed()
                .assertIsNotEnabled()
            onAllNodesWithText(mainActivity.getString(R.string.pref_summary_placeholder)).assertCountEquals(4)
            onAllNodesWithTag("settings-value-row-value", useUnmergedTree = true).assertCountEquals(0)
            onAllNodesWithTag("settings-value-row-chevron", useUnmergedTree = true).assertCountEquals(0)
        }
    }
}
