package com.mxt.anitrend.ui

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.ui.navigation.MAIN_NAV_DRAWER_LIST_TAG
import kotlin.math.min
import kotlin.math.roundToInt
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.navigation.MainNavigationDrawer
import com.mxt.anitrend.ui.navigation.MainNavigationDrawerState
import com.mxt.anitrend.ui.navigation.MAIN_NAV_DRAWER_HEADER_TAG
import com.mxt.anitrend.ui.profile.AvatarIndicatorState
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import com.mxt.anitrend.widget.installComposeContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainNavigationDrawerComposeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ProgressLayoutTestActivity>()

    private fun renderDrawer(
        state: MainNavigationDrawerState,
        onItemClick: (Int) -> Unit = {},
        onHeaderClick: () -> Unit = {},
        onAvatarClick: () -> Unit = {},
    ) {
        composeRule.activity.installComposeContent {
            AniTrendTheme {
                MainNavigationDrawer(
                    state = state,
                    onItemClick = onItemClick,
                    onHeaderClick = onHeaderClick,
                    onAvatarClick = onAvatarClick,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun signedOutDrawerShowsSignInAndHidesAuthenticatedDestinations() {
        var headerClicks = 0
        renderDrawer(
            MainNavigationDrawerState(
                selectedItemId = R.id.nav_anime,
                isAuthenticated = false,
            ),
            onHeaderClick = { headerClicks++ },
        )

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_anime))
            .assertIsDisplayed()
            .assertIsSelected()
            .assertHasClickAction()
            .assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag(MAIN_NAV_DRAWER_HEADER_TAG).assertHeightIsEqualTo(150.dp)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_sign_in))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_home))
            .assertDoesNotExist()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.nav_header_manage))
            .assertDoesNotExist()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_sign_out))
            .assertDoesNotExist()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_update))
            .assertDoesNotExist()
        composeRule.onNodeWithTag(MAIN_NAV_DRAWER_HEADER_TAG)
            .assertHasClickAction()
            .performClick()
        composeRule.runOnIdle { assertEquals(1, headerClicks) }
    }

    @Test
    fun signedInDrawerShowsFeedAndManageRowsAndRestoresSelection() {
        renderDrawer(
            MainNavigationDrawerState(
                selectedItemId = R.id.nav_myanime,
                isAuthenticated = true,
            ),
        )

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_home))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.nav_header_manage))
            .assertIsDisplayed()
        scrollTo(R.string.drawer_title_sign_out)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_sign_out))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_sign_in))
            .assertDoesNotExist()
        scrollTo(R.string.drawer_title_myanime)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_myanime))
            .assertIsSelected()
    }

    @Test
    fun headerAndUnreadAvatarUseTheirSeparateActions() {
        var headerClicks = 0
        var avatarClicks = 0
        renderDrawer(
            MainNavigationDrawerState(
                isAuthenticated = true,
                headerTitle = "Miyuki",
                avatarIndicatorState =
                AvatarIndicatorState(
                    hasRenderedAvatar = true,
                    unreadNotificationCount = 3,
                    showNotificationIndicator = true,
                ),
            ),
            onHeaderClick = { headerClicks++ },
            onAvatarClick = { avatarClicks++ },
        )

        composeRule.onNodeWithTag(MAIN_NAV_DRAWER_HEADER_TAG)
            .assertHasClickAction()
            .performClick()
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.profile_avatar_description),
        ).assertHasClickAction().performClick()
        composeRule.runOnIdle {
            assertEquals(1, headerClicks)
            assertEquals(1, avatarClicks)
        }
    }

    @Test
    fun updateBadgeDisplaysAndSendsItsDrawerItemId() {
        var selectedItemId = 0
        val updateLabel = composeRule.activity.getString(R.string.app_update, "9.9.9")
        renderDrawer(
            MainNavigationDrawerState(
                isAuthenticated = true,
                showUpdateAction = true,
                updateBadgeText = updateLabel,
            ),
            onItemClick = { selectedItemId = it },
        )

        scrollTo(R.string.drawer_title_update)
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_update))
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText(updateLabel)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(R.id.nav_check_update, selectedItemId) }
    }

    private fun scrollTo(title: Int) {
        composeRule.onNodeWithTag(MAIN_NAV_DRAWER_LIST_TAG)
            .performScrollToNode(hasText(composeRule.activity.getString(title)))
    }

    @Test
    fun nativeDrawerLayoutConstrainsWidthOnlyOnce() {
        lateinit var drawerLayout: DrawerLayout
        lateinit var drawer: ComposeView
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val host = composeRule.activity
            drawerLayout = DrawerLayout(host)
            drawerLayout.addView(
                FrameLayout(host),
                DrawerLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
            drawer = ComposeView(host).apply {
                id = android.view.View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        MainNavigationDrawer(
                            state = MainNavigationDrawerState(),
                            onItemClick = {},
                            onHeaderClick = {},
                            onAvatarClick = {},
                        )
                    }
                }
            }
            drawerLayout.addView(
                drawer,
                DrawerLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, GravityCompat.START),
            )
            host.setContentView(drawerLayout)
            drawerLayout.openDrawer(GravityCompat.START, false)
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            val density = composeRule.activity.resources.displayMetrics.density
            val nativeMargin = (64 * density).roundToInt()
            val maximumWidth = (320 * density).roundToInt()
            assertEquals(min(drawerLayout.width - nativeMargin, maximumWidth), drawer.width)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun unselectedLabelsRemainVisibleOnDarkAndBlackSurfaces() {
        for (surface in listOf(darkColorScheme().surface, Color.Black)) {
            composeRule.activity.installComposeContent {
                MaterialTheme(colorScheme = darkColorScheme(surface = surface, onSurface = Color.White)) {
                    MainNavigationDrawer(
                        state = MainNavigationDrawerState(selectedItemId = R.id.nav_anime),
                        onItemClick = {},
                        onHeaderClick = {},
                        onAvatarClick = {},
                    )
                }
            }
            composeRule.waitForIdle()
            val pixels = composeRule.onNodeWithText(
                composeRule.activity.getString(R.string.drawer_title_manga),
                useUnmergedTree = true,
            ).captureToImage().toPixelMap()
            val readableForeground = (0 until pixels.width).any { x ->
                (0 until pixels.height).any { y -> pixels[x, y].luminance() > 0.5f }
            }
            assertTrue("Unselected label needs a visible foreground on $surface", readableForeground)
        }
    }

    @Test
    fun scrollingToExtrasMovesTheHeaderOutOfTheViewport() {
        composeRule.activity.installComposeContent {
            AniTrendTheme {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.height(400.dp)) {
                        MainNavigationDrawer(
                            state = MainNavigationDrawerState(isAuthenticated = true),
                            onItemClick = {},
                            onHeaderClick = {},
                            onAvatarClick = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        scrollTo(R.string.drawer_title_sign_out)
        composeRule.onNodeWithTag(MAIN_NAV_DRAWER_HEADER_TAG).assertIsNotDisplayed()
    }

    @Test
    fun keyboardTraversalActivatesTheNextDestination() {
        var selectedId = 0
        lateinit var inputModeManager: InputModeManager
        composeRule.activity.installComposeContent {
            inputModeManager = LocalInputModeManager.current
            AniTrendTheme {
                MainNavigationDrawer(
                    state = MainNavigationDrawerState(),
                    onItemClick = { selectedId = it },
                    onHeaderClick = {},
                    onAvatarClick = {},
                )
            }
        }
        composeRule.runOnIdle {
            assertTrue(inputModeManager.requestInputMode(InputMode.Keyboard))
        }
        val anime = composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_anime))
        anime.performSemanticsAction(SemanticsActions.RequestFocus) { request -> request() }
        anime.assertIsFocused()
        anime.performKeyInput { pressKey(Key.DirectionDown) }
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.drawer_title_manga))
            .assertIsFocused()
            .performKeyInput { pressKey(Key.Enter) }
        composeRule.runOnIdle { assertEquals(R.id.nav_manga, selectedId) }
    }
}
