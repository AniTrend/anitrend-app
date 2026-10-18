@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.profile.AvatarIndicator
import com.mxt.anitrend.ui.profile.AvatarIndicatorState
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
import java.io.File
import org.junit.After

@RunWith(AndroidJUnit4::class)
class AvatarIndicatorComposeTest {
    private val avatarFixtureFiles = mutableListOf<File>()

    @After
    fun deleteAvatarFixtures() {
        avatarFixtureFiles.forEach { it.delete() }
        avatarFixtureFiles.clear()
    }

    private fun avatarUrl(context: Context, color: Int): String {
        val imageFile = solidImageFile(context, color, width = 32, height = 32)
        avatarFixtureFiles.add(imageFile)
        return Uri.fromFile(imageFile).toString()
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun avatarUsesLauncherUntilRenderedAndKeepsUnreadBadge() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val avatarUrl = avatarUrl(hostActivity, Color.MAGENTA)
            val launcherCenterColor = resourceCenterColor(hostActivity, R.mipmap.ic_launcher_round)
            val avatarCenterColor = Color.MAGENTA
            assertFalse(
                "launcher and avatar fixtures should have distinct center pixels",
                launcherCenterColor.isNearColor(avatarCenterColor),
            )

            var composeView: ComposeView? = null
            var container: FrameLayout? = null
            val state =
                mutableStateOf(
                    AvatarIndicatorState(
                        avatarUrl = avatarUrl,
                        hasRenderedAvatar = false,
                        unreadNotificationCount = 2,
                        showNotificationIndicator = true,
                    ),
                )
            runOnUiThread {
                val contentContainer = FrameLayout(hostActivity)
                val avatarComposeView = ComposeView(hostActivity).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        AniTrendTheme {
                            AvatarIndicator(
                                state = state.value,
                                onAvatarClick = {},
                            )
                        }
                    }
                }
                contentContainer.addView(
                    avatarComposeView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
                composeView = avatarComposeView
                container = contentContainer
                hostActivity.setContentView(contentContainer)
            }
            waitUntil("launcher image remains visible before avatar rendering", 5_000L) {
                val imageNodes = onAllNodesWithTag(AVATAR_IMAGE_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                imageNodes.isNotEmpty() &&
                    onNodeWithTag(AVATAR_IMAGE_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(launcherCenterColor)
            }

            val avatarDescription = hostActivity.getString(R.string.profile_avatar_description)
            onNodeWithContentDescription(avatarDescription)
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithTag(AVATAR_IMAGE_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("2").assertIsDisplayed()

            runOnUiThread { state.value = state.value.copy(hasRenderedAvatar = true) }
            waitUntil("Coil avatar renders only after avatar state is available", 60_000L) {
                val imageNodes = onAllNodesWithTag(AVATAR_IMAGE_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                imageNodes.isNotEmpty() &&
                    onNodeWithTag(AVATAR_IMAGE_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(avatarCenterColor)
            }

            runOnUiThread {
                val avatarComposeView = requireNotNull(composeView)
                val contentContainer = requireNotNull(container)
                val layoutParams = avatarComposeView.layoutParams
                contentContainer.removeView(avatarComposeView)
                contentContainer.addView(avatarComposeView, layoutParams)
            }
            waitForIdle()
            onNodeWithText("2").assertIsDisplayed()

            runOnUiThread {
                state.value = state.value.copy(showNotificationIndicator = false)
            }
            waitForIdle()
            onNodeWithText("2").assertDoesNotExist()

            runOnUiThread {
                state.value =
                    state.value.copy(
                        unreadNotificationCount = 10,
                        showNotificationIndicator = true,
                    )
            }
            waitForIdle()
            onNodeWithText("10").assertIsDisplayed()

            runOnUiThread {
                state.value =
                    state.value.copy(
                        unreadNotificationCount = 0,
                        showNotificationIndicator = false,
                    )
            }
            waitForIdle()
            onNodeWithText("10").assertDoesNotExist()
        }
    }

    private companion object {
        const val AVATAR_IMAGE_TAG = "avatar-indicator-image"
    }
}

private fun resourceCenterColor(context: Context, resourceId: Int): Int {
    val drawable = requireNotNull(ContextCompat.getDrawable(context, resourceId))
    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(Canvas(bitmap))
    return bitmap.centerColor().also { bitmap.recycle() }
}

private fun Bitmap.centerColor(): Int = getPixel(width / 2, height / 2)

private fun Int.isNearColor(expectedColor: Int): Boolean = abs(Color.red(this) - Color.red(expectedColor)) <= 32 &&
    abs(Color.green(this) - Color.green(expectedColor)) <= 32 &&
    abs(Color.blue(this) - Color.blue(expectedColor)) <= 32
