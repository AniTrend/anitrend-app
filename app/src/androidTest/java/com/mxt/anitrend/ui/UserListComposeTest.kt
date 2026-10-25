@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ViewFlipper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.button.MaterialButton
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.user.UserList
import com.mxt.anitrend.ui.user.UserRowUiState
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import com.mxt.anitrend.widget.solidImageFile
import java.io.File
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserListComposeTest {
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
    fun userRowDisplaysLargeAvatarAndRoutesRowClick() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val imageUrl = avatarUrl(hostActivity, Color.BLUE)
            val extraLargeImageUrl = avatarUrl(hostActivity, Color.RED)
            val expectedLargeCenterColor = Color.BLUE
            val expectedExtraLargeCenterColor = Color.RED
            assertFalse(
                "fixture large and extra-large avatars should have distinct center pixels",
                expectedLargeCenterColor.isNearColor(expectedExtraLargeCenterColor),
            )
            val currentUser = userFixture(1L, "current-user", false, null)
            val user = userFixture(2L, "Compose user", false, imageUrl).apply {
                avatar = ImageBase(extraLarge = extraLargeImageUrl, large = imageUrl, medium = null)
            }
            var clickedUserId: Long? = null
            runOnUiThread {
                installUserHost(hostActivity, currentUser, listOf(user), onUserClick = { clickedUserId = it })
            }
            waitUntil("user row renders the large avatar image", 60_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedLargeCenterColor)
            }
            waitForIdle()

            onNodeWithTag(USER_ROW_TAG, useUnmergedTree = true)
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithText("Compose user").assertIsDisplayed()
            onNodeWithTag(USER_ROW_TAG, useUnmergedTree = true).performClick()
            runOnIdle { assertEquals(2L, clickedUserId) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun followControlTracksCommittedProjectionAndHidesForSelf() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val imageUrl = avatarUrl(hostActivity, Color.BLUE)
            val reboundAvatarUrl = avatarUrl(hostActivity, Color.RED)
            val expectedInitialCenterColor = Color.BLUE
            val expectedReboundCenterColor = Color.RED
            val expectedFallbackCenterColor = resourceCenterColor(hostActivity, R.drawable.ic_emoji_sweat)
            val currentUser = userFixture(1L, "current-user", false, null)
            val user = userFixture(2L, "Follow target", false, imageUrl)
            lateinit var host: UserListHost
            val followClicks = mutableListOf<Long>()
            runOnUiThread {
                host = installUserHost(
                    hostActivity,
                    currentUser,
                    listOf(user),
                    onToggleFollow = { followClicks.add(it) },
                )
            }
            waitUntil("initial user avatar renders", 60_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedInitialCenterColor)
            }
            waitForIdle()

            onNodeWithTag(USER_ROW_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(USER_FOLLOW_TAG, useUnmergedTree = true).assertIsDisplayed()
            lateinit var followWidget: FollowStateWidget
            lateinit var button: MaterialButton
            runOnIdle {
                followWidget = requireNotNull(findFollowStateWidget(hostActivity))
                assertEquals(View.VISIBLE, followWidget.visibility)
                button = followButton(followWidget)
                assertEquals(hostActivity.getString(R.string.follow), button.text.toString())
            }

            runOnUiThread { button.performClick() }
            runOnIdle {
                assertFalse(user.isFollowing)
                assertEquals(listOf(2L), followClicks)
                assertEquals(FollowStateWidget.LOADING_STATE, followFlipper(followWidget).displayedChild)
            }

            lateinit var oldProjection: UserRowUiState
            runOnIdle { oldProjection = host.rows.value.single() }
            runOnUiThread {
                user.name = "Following target"
                user.avatar = ImageBase(extraLarge = null, large = reboundAvatarUrl, medium = null)
                host.replaceUsers(listOf(user))
            }
            runOnIdle { assertNotSame(oldProjection, host.rows.value.single()) }
            waitUntil("user name rebinds", 5_000L) {
                onAllNodesWithText("Following target").fetchSemanticsNodes().isNotEmpty()
            }
            waitUntil("user avatar updates when the projection is replaced", 60_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedReboundCenterColor)
            }
            runOnIdle {
                followWidget = requireNotNull(findFollowStateWidget(hostActivity))
                button = followButton(followWidget)
                assertEquals(hostActivity.getString(R.string.follow), button.text.toString())
                assertEquals(FollowStateWidget.CONTENT_STATE, followFlipper(followWidget).displayedChild)
            }

            lateinit var uncommittedProjection: UserRowUiState
            runOnIdle { uncommittedProjection = host.rows.value.single() }
            runOnUiThread {
                host.rows.value = listOf(uncommittedProjection.copy(isFollowing = true))
            }
            runOnIdle { assertNotSame(uncommittedProjection, host.rows.value.single()) }
            waitUntil("committed following projection rebinds", 5_000L) {
                var committed = false
                runOnUiThread {
                    findFollowStateWidget(hostActivity)?.let { widget ->
                        followWidget = widget
                        button = followButton(widget)
                        committed =
                            widget.visibility == View.VISIBLE &&
                            button.text.toString() == hostActivity.getString(R.string.following) &&
                            followFlipper(widget).displayedChild == FollowStateWidget.CONTENT_STATE
                    }
                }
                committed
            }
            runOnIdle {
                assertFalse("rendering committed state must not mutate the submitted entity", user.isFollowing)
                assertEquals(hostActivity.getString(R.string.following), button.text.toString())
                assertEquals(FollowStateWidget.CONTENT_STATE, followFlipper(followWidget).displayedChild)
            }

            runOnUiThread { host.currentUser.value = user }
            waitUntil("follow control hides when current user becomes the listed user", 5_000L) {
                findFollowStateWidget(hostActivity)?.visibility == View.GONE
            }
            runOnUiThread { host.currentUser.value = currentUser }
            waitUntil("follow control returns when current user changes", 5_000L) {
                findFollowStateWidget(hostActivity)?.visibility == View.VISIBLE
            }

            val selfUser = userFixture(1L, "current-user", false, null)
            runOnUiThread { host.replaceUsers(listOf(selfUser)) }
            waitUntil("follow control hides for the current user's own row", 5_000L) {
                findFollowStateWidget(hostActivity)?.visibility == View.GONE
            }
            waitUntil("self row shows the missing-avatar fallback", 5_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedFallbackCenterColor)
            }
            onNodeWithText("current-user").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun missingAndFailedAvatarsShowFallbackPixels() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val expectedFallbackCenterColor = resourceCenterColor(hostActivity, R.drawable.ic_emoji_sweat)
            val expectedAvatarCenterColor = Color.BLUE
            val avatarUrl = avatarUrl(hostActivity, Color.BLUE)
            val invalidResourceUrl = "android.resource://${hostActivity.packageName}/0"
            val currentUser = userFixture(1L, "current-user", false, null)
            val user = userFixture(3L, "Placeholder user", false, null)
            lateinit var host: UserListHost
            runOnUiThread {
                host = installUserHost(hostActivity, currentUser, listOf(user))
            }
            waitUntil("missing avatar renders the fallback resource", 5_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedFallbackCenterColor)
            }

            runOnUiThread {
                user.avatar = ImageBase(extraLarge = null, large = avatarUrl, medium = null)
                host.replaceUsers(listOf(user))
            }
            waitUntil("rebound avatar request renders its image", 60_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedAvatarCenterColor)
            }

            runOnUiThread {
                user.avatar = ImageBase(extraLarge = null, large = invalidResourceUrl, medium = null)
                host.replaceUsers(listOf(user))
            }
            waitUntil("failed avatar request renders the same fallback resource", 60_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedFallbackCenterColor)
            }
            onNodeWithTag(USER_ROW_TAG, useUnmergedTree = true).assertIsDisplayed()
            onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    private fun installUserHost(
        activity: ProgressLayoutTestActivity,
        currentUser: UserBase?,
        users: List<UserBase>,
        onUserClick: (Long) -> Unit = {},
        onToggleFollow: (Long) -> Unit = {},
    ): UserListHost {
        val host = UserListHost(
            rows = mutableStateOf(users.map { it.toRowState() }),
            currentUser = mutableStateOf(currentUser),
            onUserClick = onUserClick,
            onToggleFollow = onToggleFollow,
        )
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        UserList(
                            items = host.rows.value,
                            currentUser = host.currentUser.value,
                            onUserClick = host.onUserClick,
                            onToggleFollow = host.onToggleFollow,
                        )
                    }
                }
            },
        )
        return host
    }

    private fun UserBase.toRowState(): UserRowUiState = UserRowUiState(
        model = this,
        name = name,
        avatar = avatar,
        isFollowing = isFollowing,
    )

    private fun userFixture(
        id: Long,
        name: String,
        isFollowing: Boolean,
        avatarUrl: String?,
    ): UserBase = UserBase(name = name, isFollowing = isFollowing).apply {
        this.id = id
        avatar = avatarUrl?.let { ImageBase(extraLarge = null, large = it, medium = null) }
    }

    private fun findFollowStateWidget(activity: ProgressLayoutTestActivity): FollowStateWidget? {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return findFollowStateWidget(activity.findViewById(android.R.id.content))
        }
        var result: FollowStateWidget? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            result = findFollowStateWidget(activity.findViewById(android.R.id.content))
        }
        return result
    }

    private fun findFollowStateWidget(view: View): FollowStateWidget? {
        if (view is FollowStateWidget) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findFollowStateWidget(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private fun followButton(widget: FollowStateWidget): MaterialButton = requireNotNull(widget.findViewById(R.id.button_state_text))

    private fun followFlipper(widget: FollowStateWidget): ViewFlipper = requireNotNull(widget.findViewById(R.id.widget_flipper))

    private inner class UserListHost(
        val rows: MutableState<List<UserRowUiState>>,
        val currentUser: MutableState<UserBase?>,
        val onUserClick: (Long) -> Unit,
        val onToggleFollow: (Long) -> Unit,
    ) {
        fun replaceUsers(users: List<UserBase>) {
            rows.value = users.map { it.toRowState() }
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

    private companion object {
        const val USER_ROW_TAG = "user-row-card"
        const val USER_AVATAR_TAG = "user-row-avatar"
        const val USER_FOLLOW_TAG = "user-row-follow"
    }
}

private fun Bitmap.centerColor(): Int = getPixel(width / 2, height / 2)

private fun Int.isNearColor(expectedColor: Int): Boolean = abs(Color.red(this) - Color.red(expectedColor)) <= 32 &&
    abs(Color.green(this) - Color.green(expectedColor)) <= 32 &&
    abs(Color.blue(this) - Color.blue(expectedColor)) <= 32
