@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.widget

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ViewFlipper

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.google.android.material.button.MaterialButton
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.UserAdapter
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class UserAdapterComposeTest {
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
    fun userRowDisplaysLargeAvatarAndContent() {
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
            runOnUiThread {
                installUserHost(hostActivity, currentUser, listOf(user))
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
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun followControlTracksCommittedRebindAndHidesForSelf() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val imageUrl = avatarUrl(hostActivity, Color.BLUE)
            val reboundAvatarUrl = avatarUrl(hostActivity, Color.RED)
            val expectedInitialCenterColor = Color.BLUE
            val expectedReboundCenterColor = Color.RED
            val expectedFallbackCenterColor = resourceCenterColor(hostActivity, R.drawable.ic_emoji_sweat)
            val currentUser = userFixture(1L, "current-user", false, null)
            val user = userFixture(2L, "Follow target", false, imageUrl)
            lateinit var host: UserRowHost
            runOnUiThread {
                host = installUserHost(hostActivity, currentUser, listOf(user))
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
            var row = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            var followWidget = requireNotNull(findFollowStateWidget(row))
            assertEquals(View.VISIBLE, followWidget.visibility)
            var button = followButton(followWidget)
            assertEquals(hostActivity.getString(R.string.follow), button.text.toString())

            runOnUiThread { button.performClick() }
            runOnIdle {
                assertFalse(user.isFollowing)
                assertEquals(FollowStateWidget.LOADING_STATE, followFlipper(followWidget).displayedChild)
            }

            runOnUiThread {
                user.name = "Following target"
                user.avatar = ImageBase(extraLarge = null, large = reboundAvatarUrl, medium = null)
                host.adapter.onItemChanged(user, 0)
            }
            waitUntil("user name rebinds", 5_000L) {
                onAllNodesWithText("Following target").fetchSemanticsNodes().isNotEmpty()
            }
            waitUntil("user avatar updates when the adapter rebinds", 60_000L) {
                val avatarNodes = onAllNodesWithTag(USER_AVATAR_TAG, useUnmergedTree = true).fetchSemanticsNodes()
                avatarNodes.isNotEmpty() &&
                    onNodeWithTag(USER_AVATAR_TAG, useUnmergedTree = true)
                        .captureToImage()
                        .asAndroidBitmap()
                        .centerColor()
                        .isNearColor(expectedReboundCenterColor)
            }
            row = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            followWidget = requireNotNull(findFollowStateWidget(row))
            button = followButton(followWidget)
            runOnIdle {
                assertEquals(hostActivity.getString(R.string.follow), button.text.toString())
                assertEquals(FollowStateWidget.CONTENT_STATE, followFlipper(followWidget).displayedChild)
            }
            runOnUiThread {
                user.isFollowing = true
                host.adapter.onItemChanged(user, 0)
            }
            waitUntil("committed following state rebinds", 5_000L) {
                row = host.recyclerView.findViewHolderForAdapterPosition(0)?.itemView ?: return@waitUntil false
                followWidget = findFollowStateWidget(row) ?: return@waitUntil false
                button = followButton(followWidget)
                followWidget.visibility == View.VISIBLE &&
                    button.text.toString() == hostActivity.getString(R.string.following) &&
                    followFlipper(followWidget).displayedChild == FollowStateWidget.CONTENT_STATE
            }
            runOnIdle {
                assertEquals(hostActivity.getString(R.string.following), button.text.toString())
                assertEquals(FollowStateWidget.CONTENT_STATE, followFlipper(followWidget).displayedChild)
            }

            val recycledRow = row
            runOnUiThread {
                host.recyclerView.setItemViewCacheSize(0)
                host.adapter.onItemRemoved(0)
            }
            waitUntil("user row detaches after removal", 5_000L) {
                host.adapter.itemCount == 0 && recycledRow.parent == null
            }
            waitForIdle()

            val selfUser = userFixture(1L, "current-user", false, null)
            runOnUiThread { host.adapter.onItemsInserted(listOf(selfUser)) }
            waitUntil("self row reuses the recycled ComposeView", 5_000L) {
                val selfRow = host.recyclerView.findViewHolderForAdapterPosition(0)?.itemView ?: return@waitUntil false
                selfRow === recycledRow && findFollowStateWidget(selfRow)?.visibility == View.GONE
            }
            val selfRow = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            assertSame(recycledRow, selfRow)
            waitUntil("recycled row shows the missing-avatar fallback", 5_000L) {
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
    fun missingAndFailedAvatarsShowComposeFallbackPixels() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity>(testTimeout = 3.minutes) {
            val hostActivity = requireNotNull(activity)
            val expectedFallbackCenterColor = resourceCenterColor(hostActivity, R.drawable.ic_emoji_sweat)
            val expectedAvatarCenterColor = Color.BLUE
            val avatarUrl = avatarUrl(hostActivity, Color.BLUE)
            val invalidResourceUrl = "android.resource://${hostActivity.packageName}/0"
            val currentUser = userFixture(1L, "current-user", false, null)
            val user = userFixture(3L, "Placeholder user", false, null)
            lateinit var host: UserRowHost
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
                host.adapter.onItemChanged(user, 0)
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
                host.adapter.onItemChanged(user, 0)
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
    ): UserRowHost {
        val adapter =
            UserAdapter(activity, currentUser) { _, _ -> }.apply {
                onItemsInserted(users)
            }
        val recyclerView =
            RecyclerView(activity).apply {
                setHasFixedSize(true)
                layoutManager =
                    StaggeredGridLayoutManager(
                        activity.resources.getInteger(R.integer.single_list_x1),
                        StaggeredGridLayoutManager.VERTICAL,
                    )
                itemAnimator = null
                this.adapter = adapter
            }
        activity.setContentView(
            ComposeView(activity).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        AndroidView(
                            factory = { recyclerView },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            },
        )
        return UserRowHost(adapter, recyclerView)
    }

    private fun userFixture(
        id: Long,
        name: String,
        isFollowing: Boolean,
        avatarUrl: String?,
    ): UserBase = UserBase(name = name, isFollowing = isFollowing).apply {
        this.id = id
        avatar = avatarUrl?.let { ImageBase(extraLarge = null, large = it, medium = null) }
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

    private data class UserRowHost(
        val adapter: UserAdapter,
        val recyclerView: RecyclerView,
    )

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
