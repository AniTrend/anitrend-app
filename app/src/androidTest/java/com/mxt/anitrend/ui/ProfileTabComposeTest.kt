@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui

import android.widget.FrameLayout
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.ui.profile.AboutPanelStats
import com.mxt.anitrend.ui.profile.AvatarIndicatorState
import com.mxt.anitrend.ui.profile.ProfileScreen
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileTabComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun profileTabsAndStatsExposeComposeSemantics() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val selectedTabIndex = mutableIntStateOf(0)

            runOnUiThread {
                val content = hostActivity.findViewById<FrameLayout>(android.R.id.content)
                content.removeAllViews()
                val composeView =
                    androidx.compose.ui.platform.ComposeView(hostActivity).apply {
                        setViewCompositionStrategy(
                            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                        )
                        setContent {
                            AniTrendTheme("light") {
                                ProfileScreen(
                                    avatar = AvatarIndicatorState(avatarUrl = null),
                                    displayName = "AniTrend user",
                                    about = null,
                                    stats = null,
                                    aboutStats = AboutPanelStats(followingCount = "0", followersCount = "0", favouritesCount = "0"),
                                    selectedTabIndex = selectedTabIndex.intValue,
                                    markwon = createMarkwon(hostActivity) {},
                                    onLinkClick = {},
                                    onAvatarClick = {},
                                    onTabSelected = { selectedTabIndex.intValue = it },
                                    onAnimeTotalClick = {},
                                    onAnimeTimeClick = {},
                                    onMangaTotalClick = {},
                                    onMangaChaptersClick = {},
                                    onSeeAll = {},
                                    onBackClick = {},
                                )
                            }
                        }
                    }
                content.addView(
                    composeView,
                    FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
            waitForIdle()

            val overview = hostActivity.getString(R.string.profile_section_overview)
            val progress = hostActivity.getString(R.string.profile_section_media)
            val status = hostActivity.getString(R.string.profile_section_text)
            // The section header reuses the Overview label; scope each
            // assertion to the selectable chip (text + click action).
            onNode(hasText(overview) and hasClickAction()).assertIsDisplayed().assertIsSelected()
            onNode(hasText(progress) and hasClickAction()).assertIsDisplayed().assertIsNotSelected().performClick()
            onNode(hasText(progress) and hasClickAction()).assertIsSelected()
            onNode(hasText(status) and hasClickAction()).assertIsDisplayed().assertIsNotSelected().performClick()
            onNode(hasText(status) and hasClickAction()).assertIsSelected()
            val animeLabel = hostActivity.getString(R.string.anime)
            onNodeWithContentDescription(animeLabel)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }
}
