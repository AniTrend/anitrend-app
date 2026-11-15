@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.extension.asBundle
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsDividerComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun settingsScreensRenderNonInteractiveSectionAndRowDividers() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val navHost =
                mainActivity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
            val navController = navHost.navController

            runOnUiThread {
                navController.navigate(R.id.customizeSettingsFragment)
            }
            waitForIdle()

            val customizeDividers = onAllNodesWithTag("settings-divider")
            customizeDividers.assertCountEquals(3)
            for (index in 0 until 3) {
                customizeDividers[index]
                    .assertIsDisplayed()
                    .assertHeightIsEqualTo(1.dp)
                    .assertHasNoClickAction()
            }

            runOnUiThread {
                navController.navigate(
                    R.id.settingsCategoryFragment,
                    SettingsCategoryScreenParam(categoryId = SettingsCategoryRegistry.CONTENT).asBundle(),
                )
            }
            waitForIdle()

            val contentDividers = onAllNodesWithTag("settings-divider")
            contentDividers.assertCountEquals(4)
            for (index in 0 until 4) {
                contentDividers[index]
                    .assertIsDisplayed()
                    .assertHeightIsEqualTo(1.dp)
                    .assertHasNoClickAction()
            }
        }
    }
}
