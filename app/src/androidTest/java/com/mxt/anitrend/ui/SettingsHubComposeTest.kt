@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.index.MainActivity
import com.mxt.anitrend.view.fragment.settings.SettingsCategoryRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsHubComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun signedOutCategoryCardsRenderAndPreserveAppearanceNavigation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = false)
        val settings = Settings(context)
        settings.theme = KeyUtil.THEME_LIGHT
        settings.edit { putString(context.getString(R.string.pref_key_startup_page), "1") }
        val categories =
            SettingsCategoryRegistry.categories(
                isFirebaseVisible = FirebaseApp.getApps(context).isNotEmpty(),
            )

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            onAllNodesWithContentDescription(mainActivity.getString(R.string.action_menu))[0].performClick()
            waitForIdle()
            onNodeWithText(mainActivity.getString(R.string.title_activity_settings)).performClick()
            waitForIdle()

            onAllNodesWithTag("settings-category-card").assertCountEquals(categories.size)
            val appearanceTitle = mainActivity.getString(R.string.pref_header_appearance)
            val appearanceSummary = mainActivity.getString(R.string.pref_header_appearance_summary)
            val appearanceCard = onNodeWithContentDescription(appearanceTitle)
            appearanceCard
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithText(appearanceTitle, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText(appearanceSummary, useUnmergedTree = true).assertIsDisplayed()
            appearanceCard.performClick()
            waitForIdle()
            onAllNodesWithTag("settings-value-row").assertCountEquals(3)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun generalCardOpensSettingsCategoryRows() {
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

            val generalTitle = mainActivity.getString(R.string.pref_header_general)
            onNodeWithContentDescription(generalTitle)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            waitForIdle()
            onAllNodesWithTag("settings-value-row")[0].assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun authenticatedAccountCardIsAccessibleAndNavigates() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestSessionUtil.setAuthenticated(context, authenticated = true)
        val settings = Settings(context)
        settings.theme = KeyUtil.THEME_LIGHT
        settings.edit { putString(context.getString(R.string.pref_key_startup_page), "1") }
        val accountTitle = context.getString(R.string.pref_header_account)

        runAndroidComposeUiTest<MainActivity> {
            val mainActivity = requireNotNull(activity)
            onAllNodesWithContentDescription(mainActivity.getString(R.string.action_menu))[0].performClick()
            waitForIdle()
            onNodeWithText(mainActivity.getString(R.string.title_activity_settings)).performClick()
            waitForIdle()

            onNodeWithContentDescription(accountTitle)
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            waitForIdle()
            onAllNodesWithTag("settings-category-card").assertCountEquals(0)
            onNodeWithText(mainActivity.getString(R.string.action_back)).assertIsDisplayed()
        }
    }
}
