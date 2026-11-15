@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.extension.asBundle
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsSectionCardComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun customizeAndContentOwnersRenderSectionCardHeadersAndDividers() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        Settings(context).theme = KeyUtil.THEME_LIGHT

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val navHost =
                mainActivity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
            val navController = navHost.navController

            runOnUiThread { navController.navigate(R.id.customizeSettingsFragment) }
            waitForIdle()
            onAllNodesWithTag("settings-section-card").assertCountEquals(1)
            onNodeWithTag("settings-section-card").assertHasNoClickAction()
            onNodeWithText(mainActivity.getString(R.string.pref_header_customize), useUnmergedTree = true)
                .assertIsDisplayed()
            onNodeWithText(mainActivity.getString(R.string.pref_header_customize_summary), useUnmergedTree = true)
                .assertIsDisplayed()
            onAllNodesWithTag("settings-divider").assertCountEquals(3)

            runOnUiThread {
                navController.navigate(
                    R.id.settingsCategoryFragment,
                    SettingsCategoryScreenParam(categoryId = SettingsCategoryRegistry.CONTENT).asBundle(),
                )
            }
            waitForIdle()
            onAllNodesWithTag("settings-section-card").assertCountEquals(1)
            onNodeWithTag("settings-section-card").assertHasNoClickAction()
            onAllNodesWithTag("settings-divider").assertCountEquals(4)
        }
    }
}
