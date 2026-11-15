@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Bitmap
import android.view.View
import android.view.Window
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedListAdapter
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedProgressRowComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun progressRowExposesAccessibleActionsAndMediaContent() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = localCoverUrl(hostActivity)
            val item = progressItem(id = 51L, coverUrl = imageUrl)
            lateinit var host: FeedProgressHost
            runOnUiThread {
                host = installFeedProgressHost(hostActivity)
                host.adapter.submitList(listOf(item))
            }
            waitUntil("progress row binds", 5_000L) {
                host.adapter.currentList.singleOrNull() == item &&
                    host.recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.isShown == true
            }
            waitForIdle()

            val profileDescription =
                hostActivity.getString(
                    R.string.description_comment_open_profile,
                    requireNotNull(item.userName),
                )
            val mediaDescription = requireNotNull(item.mediaTitleEnglish)
            val likesDescription = hostActivity.getString(R.string.title_view_likes)
            val commentsDescription = hostActivity.getString(R.string.description_feed_open_comments)
            val favouriteDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            val deleteDescription = hostActivity.getString(R.string.dialog_title_delete_activity)

            val profileNode = onNodeWithContentDescription(profileDescription)
            profileNode.assertIsDisplayed().assertHasClickAction()
            val profileBounds = profileNode.fetchSemanticsNode().boundsInRoot
            lateinit var avatarRow: View
            runOnUiThread {
                avatarRow = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            }
            val avatarBitmap = captureDisplayedView(avatarRow, hostActivity.window)
            try {
                assertEquals(
                    "a missing avatar displays the shared Glide error drawable",
                    android.graphics.Color.rgb(255, 224, 74),
                    avatarBitmap.getPixel(profileBounds.center.x.toInt(), profileBounds.center.y.toInt()),
                )
            } finally {
                avatarBitmap.recycle()
            }
            onNodeWithContentDescription(mediaDescription)
                .assertIsDisplayed()
                .assertHasClickAction()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            onNodeWithContentDescription(likesDescription).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(commentsDescription).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(favouriteDescription).assertIsDisplayed().assertIsSelected()
            onNodeWithContentDescription(deleteDescription).assertIsDisplayed().assertHasClickAction()
            onNodeWithText(item.headline.toString()).assertIsDisplayed()
            onNodeWithText(requireNotNull(item.mediaTitleEnglish)).assertIsDisplayed()
            onNodeWithText(requireNotNull(item.mediaTitleOriginal)).assertIsDisplayed()
            onNodeWithText(" 3 ").assertIsDisplayed()
            onNodeWithText("12", useUnmergedTree = true, substring = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun listVariantHidesLikeControlsAndDoesNotRouteMedia() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageUrl = localCoverUrl(hostActivity)
            val item =
                progressItem(
                    id = 52L,
                    type = KeyUtil.MEDIA_LIST,
                    headline = "plans to read: Sample Manga",
                    mediaType = KeyUtil.MANGA,
                    mediaTitleEnglish = "Sample Manga",
                    mediaTitleOriginal = "サンプル漫画",
                    coverUrl = imageUrl,
                    likeCount = 0,
                    isLiked = false,
                )
            lateinit var host: FeedProgressHost
            runOnUiThread {
                host = installFeedProgressHost(hostActivity)
                host.adapter.submitList(listOf(item))
            }
            waitUntil("list row binds", 5_000L) {
                host.adapter.currentList.singleOrNull() == item &&
                    host.recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.isShown == true
            }
            waitForIdle()

            val profileDescription =
                hostActivity.getString(
                    R.string.description_comment_open_profile,
                    requireNotNull(item.userName),
                )
            val likesDescription = hostActivity.getString(R.string.title_view_likes)
            val addFavouriteDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val removeFavouriteDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            val commentsDescription = hostActivity.getString(R.string.description_feed_open_comments)
            val deleteDescription = hostActivity.getString(R.string.dialog_title_delete_activity)

            onNodeWithContentDescription(likesDescription).assertDoesNotExist()
            onNodeWithContentDescription(addFavouriteDescription).assertDoesNotExist()
            onNodeWithContentDescription(removeFavouriteDescription).assertDoesNotExist()
            onNodeWithContentDescription(profileDescription).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(commentsDescription).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(deleteDescription).assertIsDisplayed().assertHasClickAction()
            onNodeWithText(item.headline.toString()).assertIsDisplayed()
            onNodeWithText(requireNotNull(item.mediaTitleEnglish)).assertIsDisplayed()

            val mediaCover = onNodeWithContentDescription(requireNotNull(item.mediaTitleEnglish))
            mediaCover.assertIsDisplayed()
            val mediaSemantics = mediaCover.fetchSemanticsNode().config
            assertFalse("media-list cover has no click action", mediaSemantics.contains(SemanticsActions.OnClick))
            assertFalse("media-list cover has no long-click action", mediaSemantics.contains(SemanticsActions.OnLongClick))
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingProgressRowDisablesFavoriteAndDeleteActions() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val item =
                progressItem(
                    id = 53L,
                    coverUrl = null,
                    isLiked = false,
                    isLikePending = true,
                    isDeletePending = true,
                )
            lateinit var host: FeedProgressHost
            runOnUiThread {
                host = installFeedProgressHost(hostActivity)
                host.adapter.submitList(listOf(item))
            }
            waitUntil("pending progress row binds", 5_000L) {
                host.adapter.currentList.singleOrNull() == item &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val addFavouriteDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val deleteDescription = hostActivity.getString(R.string.dialog_title_delete_activity)
            val busyDescription = hostActivity.getString(R.string.busy_please_wait)
            onNodeWithContentDescription(addFavouriteDescription).assertIsNotEnabled()
            onNodeWithContentDescription(deleteDescription).assertIsNotEnabled()
            onAllNodesWithContentDescription(busyDescription, useUnmergedTree = true).assertCountEquals(2)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun rebindClearsOldProgressCover() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val initialItem = progressItem(id = 54L, coverUrl = localCoverUrl(hostActivity))
            lateinit var host: FeedProgressHost
            runOnUiThread {
                host = installFeedProgressHost(hostActivity)
                host.adapter.submitList(listOf(initialItem))
            }
            waitUntil("initial progress row binds", 5_000L) {
                host.adapter.currentList.singleOrNull() == initialItem &&
                    host.recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.isShown == true
            }
            waitForIdle()
            val emptyCoverColor = hostActivity.getCompatColorAttr(R.attr.colorSurfaceContainerHighest)
            val initialCoverNode = onNodeWithContentDescription(requireNotNull(initialItem.mediaTitleEnglish))
            val initialCoverBounds = initialCoverNode.fetchSemanticsNode().boundsInRoot
            var initialCenterColor = 0
            lateinit var initialRow: View
            runOnUiThread {
                initialRow = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            }
            val initialCoverBitmap = captureDisplayedView(initialRow, hostActivity.window)
            try {
                initialCenterColor =
                    initialCoverBitmap.getPixel(initialCoverBounds.center.x.toInt(), initialCoverBounds.center.y.toInt())
            } finally {
                initialCoverBitmap.recycle()
            }
            assertTrue("the initial cover differs from its empty surface", initialCenterColor != emptyCoverColor)

            val reboundItem =
                initialItem.copy(
                    headline = "Progress without media title",
                    mediaTitleEnglish = null,
                    mediaTitleOriginal = null,
                    mediaCoverImageUrl = null,
                )
            runOnUiThread { host.adapter.submitList(listOf(reboundItem)) }
            waitUntil("cover-less progress model commits", 5_000L) {
                host.adapter.currentList.singleOrNull() == reboundItem &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val reboundCoverNode = onNodeWithContentDescription(reboundItem.headline.toString())
            reboundCoverNode.assertIsDisplayed()
            val reboundCoverBounds = reboundCoverNode.fetchSemanticsNode().boundsInRoot
            var reboundCenterColor = 0
            lateinit var reboundRow: View
            runOnUiThread {
                reboundRow = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
            }
            val reboundCoverBitmap = captureDisplayedView(reboundRow, hostActivity.window)
            try {
                reboundCenterColor =
                    reboundCoverBitmap.getPixel(reboundCoverBounds.center.x.toInt(), reboundCoverBounds.center.y.toInt())
            } finally {
                reboundCoverBitmap.recycle()
            }
            assertEquals("cover clears when the current feed item loses its media", emptyCoverColor, reboundCenterColor)
            onNodeWithText("Progress without media title").assertIsDisplayed()
        }
    }

    private fun captureDisplayedView(view: View, window: Window): Bitmap {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val location = IntArray(2)
        var width = 0
        var height = 0
        instrumentation.runOnMainSync {
            view.getLocationInWindow(location)
            width = view.width
            height = view.height
        }

        val screenshot =
            requireNotNull(instrumentation.uiAutomation.takeScreenshot(window)) {
                "The instrumentation runner could not capture the activity window."
            }
        try {
            check(width > 0 && height > 0) { "The visible row must have non-zero dimensions." }
            check(
                location[0] >= 0 &&
                    location[1] >= 0 &&
                    location[0] + width <= screenshot.width &&
                    location[1] + height <= screenshot.height,
            ) {
                "The visible row must fit inside the activity window screenshot."
            }
            val rowBitmap = Bitmap.createBitmap(screenshot, location[0], location[1], width, height)
            return if (rowBitmap === screenshot) {
                screenshot.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                rowBitmap
            }
        } finally {
            screenshot.recycle()
        }
    }

    private fun installFeedProgressHost(activity: ProgressLayoutTestActivity): FeedProgressHost {
        val adapter =
            FeedListAdapter(
                onToggleLikeAction = {},
                onDeleteFeedAction = {},
                onOpenMedia = { _, _ -> },
                onOpenComments = {},
                onEditFeed = {},
                onShowLikes = {},
                onOpenProfile = { _, _ -> },
                onLongPressMedia = { _, _ -> false },
            )
        val recyclerView =
            RecyclerView(activity).apply {
                layoutManager = LinearLayoutManager(activity)
                itemAnimator = null
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                this.adapter = adapter
            }
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        AndroidView(factory = { recyclerView }, modifier = Modifier.fillMaxSize())
                    }
                }
            },
        )
        return FeedProgressHost(adapter, recyclerView)
    }

    private fun progressItem(
        id: Long,
        type: String = KeyUtil.ANIME,
        headline: String = "Watching episode 4 of: Sample Series",
        mediaType: String = KeyUtil.ANIME,
        mediaTitleEnglish: String? = "Sample Series",
        mediaTitleOriginal: String? = "サンプルシリーズ",
        coverUrl: String?,
        likeCount: Int = 3,
        isLiked: Boolean = true,
        replyCount: Int = 12,
        canDelete: Boolean = true,
        isLikePending: Boolean = false,
        isDeletePending: Boolean = false,
    ) = FeedItemUiModel(
        id = id,
        type = type,
        headline = headline,
        body = null,
        likeCount = likeCount,
        isLikedByCurrentUser = isLiked,
        replyCount = replyCount,
        canEdit = false,
        canDelete = canDelete,
        isLikePending = isLikePending,
        isDeletePending = isDeletePending,
        createdAt = 1_600_000_000L,
        userAvatarUrl = null,
        userName = "AniTrend user",
        userId = 10L,
        messengerAvatarUrl = null,
        messengerName = null,
        messengerId = null,
        recipientAvatarUrl = null,
        recipientName = null,
        recipientId = null,
        mediaId = id + 100L,
        mediaType = mediaType,
        mediaTitleEnglish = mediaTitleEnglish,
        mediaTitleOriginal = mediaTitleOriginal,
        mediaCoverImageUrl = coverUrl,
        hasLikes = type != KeyUtil.MEDIA_LIST,
        likes = emptyList(),
        feedText = null,
    )

    private fun localCoverUrl(activity: ProgressLayoutTestActivity): String = "android.resource://${activity.packageName}/${R.drawable.reg_bg}"

    private data class FeedProgressHost(
        val adapter: FeedListAdapter,
        val recyclerView: RecyclerView,
    )
}
