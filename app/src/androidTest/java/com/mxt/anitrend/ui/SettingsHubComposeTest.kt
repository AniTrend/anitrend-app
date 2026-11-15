@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsHubComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun signedOutCategoryCardsRenderAndPreserveCustomizeNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        Settings(context).theme = KeyUtil.THEME_LIGHT
        val categories =
            SettingsCategoryRegistry.categories(
                isFirebaseVisible = FirebaseApp.getApps(context).isNotEmpty(),
                isAuthenticated = false,
            )

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val navHost =
                mainActivity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
            val navController = navHost.navController

            runOnUiThread { navController.navigate(R.id.action_global_settingsHubFragment) }
            waitForIdle()

            onAllNodesWithTag("settings-category-card").assertCountEquals(categories.size)
            val customizeTitle = mainActivity.getString(R.string.pref_header_customize)
            val customizeSummary = mainActivity.getString(R.string.pref_header_customize_summary)
            val customizeCard = onNodeWithContentDescription(customizeTitle)
            customizeCard
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithText(customizeTitle, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText(customizeSummary, useUnmergedTree = true).assertIsDisplayed()
            customizeCard.performClick()
            waitForIdle()
            assertEquals(R.id.customizeSettingsFragment, navController.currentDestination?.id)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun generalCardNavigatesWithCategoryId() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        Settings(context).theme = KeyUtil.THEME_LIGHT

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val navHost =
                mainActivity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
            val navController = navHost.navController
            runOnUiThread { navController.navigate(R.id.action_global_settingsHubFragment) }
            waitForIdle()

            val generalTitle = mainActivity.getString(R.string.pref_header_general)
            onNodeWithContentDescription(generalTitle)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            waitForIdle()
            assertEquals(R.id.settingsCategoryFragment, navController.currentDestination?.id)
            assertEquals(
                SettingsCategoryRegistry.GENERAL,
                navController.currentBackStackEntry?.arguments?.getString(SettingsCategoryRegistry.ARG_CATEGORY_ID),
            )
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun authenticatedAccountCardIsAccessibleAndNavigates() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = true)
        Settings(context).theme = KeyUtil.THEME_LIGHT
        val accountTitle = context.getString(R.string.pref_header_account)

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            val navHost =
                mainActivity.supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
            val navController = navHost.navController

            runOnUiThread { navController.navigate(R.id.action_global_settingsHubFragment) }
            waitForIdle()

            onNodeWithContentDescription(accountTitle)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            runOnIdle {
                assertEquals(R.id.accountSettingsFragment, navController.currentDestination?.id)
            }
        }
    }
}
