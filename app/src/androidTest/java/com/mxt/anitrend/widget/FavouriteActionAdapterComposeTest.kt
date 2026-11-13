@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.detail.CommentListAdapter
import com.mxt.anitrend.adapter.recycler.index.FeedListAdapter
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteActionAdapterComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun feedFavoriteForwardsActionAndRendersPendingAndCanonicalState() {
        val toggledIds = CopyOnWriteArrayList<Long>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: FeedListHost
            runOnUiThread {
                host = installFeedListAdapter(
                    activity = hostActivity,
                    item = feedItem(likeCount = 5, isLiked = false, isLikePending = false),
                    onToggleLike = { toggledIds.add(it) },
                )
            }
            waitUntil("feed favorite row binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            val busyDescription = hostActivity.getString(R.string.busy_please_wait)
            onNodeWithContentDescription(addDescription).assertIsDisplayed().performClick()
            runOnIdle { assertEquals(listOf(42L), toggledIds.toList()) }

            runOnUiThread {
                host.adapter.submitList(listOf(feedItem(likeCount = 5, isLiked = false, isLikePending = true)))
            }
            waitUntil("feed favorite enters pending state", 5_000L) {
                host.adapter.currentList.singleOrNull()?.isLikePending == true
            }
            waitForIdle()
            onNodeWithContentDescription(addDescription).assertIsNotEnabled()
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread {
                host.adapter.submitList(listOf(feedItem(likeCount = 6, isLiked = true, isLikePending = false)))
            }
            waitUntil("feed favorite receives canonical state", 5_000L) {
                host.adapter.currentList.singleOrNull()?.isLikedByCurrentUser == true
            }
            waitForIdle()
            onNodeWithContentDescription(removeDescription).assertIsSelected()
            onNodeWithText(" 6 ").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun commentFavoriteForwardsReplyIdAndRendersCanonicalState() {
        val toggledIds = CopyOnWriteArrayList<Long>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: CommentListHost
            runOnUiThread {
                host = installCommentListAdapter(
                    activity = hostActivity,
                    item = commentReply(likeCount = 2, isLiked = false, isLikePending = false),
                    onToggleLike = { toggledIds.add(it) },
                )
            }
            waitUntil("comment favorite row binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            onNodeWithContentDescription(addDescription).assertIsDisplayed().performClick()
            runOnIdle { assertEquals(listOf(77L), toggledIds.toList()) }

            runOnUiThread {
                host.adapter.submitList(listOf(commentReply(likeCount = 3, isLiked = true, isLikePending = false)))
            }
            waitUntil("comment favorite receives canonical state", 5_000L) {
                host.adapter.currentList.singleOrNull()?.isLikedByCurrentUser == true
            }
            waitForIdle()
            onNodeWithContentDescription(removeDescription).assertIsSelected()
            onNodeWithText(" 3 ").assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun recycledFavoriteControllerDoesNotDispatchUntilRebound() {
        val toggledIds = CopyOnWriteArrayList<Long>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: FeedListHost
            runOnUiThread {
                host = installFeedListAdapter(
                    activity = hostActivity,
                    item = feedItem(likeCount = 5, isLiked = false, isLikePending = false),
                    onToggleLike = { toggledIds.add(it) },
                )
            }
            waitUntil("recycled favorite row binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            onNodeWithContentDescription(addDescription).performClick()
            runOnIdle { assertEquals(listOf(42L), toggledIds.toList()) }

            var recycledHolder: RecyclerView.ViewHolder? = null
            runOnUiThread {
                val holder = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0))
                recycledHolder = holder
                host.adapter.onViewRecycled(holder)
            }
            waitForIdle()
            onNodeWithContentDescription(addDescription).assertIsEnabled().performClick()
            runOnIdle { assertEquals(listOf(42L), toggledIds.toList()) }

            runOnUiThread { host.adapter.onBindViewHolder(requireNotNull(recycledHolder), 0) }
            waitForIdle()
            onNodeWithContentDescription(addDescription).assertIsEnabled().performClick()
            runOnIdle { assertEquals(listOf(42L, 42L), toggledIds.toList()) }
        }
    }

    private fun installFeedListAdapter(
        activity: ProgressLayoutTestActivity,
        item: FeedItemUiModel,
        onToggleLike: (Long) -> Unit,
    ): FeedListHost {
        val adapter = FeedListAdapter(
            onToggleLikeAction = onToggleLike,
            onDeleteFeedAction = {},
            onOpenMedia = { _, _ -> },
            onOpenComments = {},
            onEditFeed = {},
            onShowLikes = {},
            onOpenProfile = { _, _ -> },
            onLongPressMedia = { _, _ -> false },
        )
        val recyclerView = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = null
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
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
        adapter.submitList(listOf(item))
        return FeedListHost(adapter, recyclerView)
    }

    private fun installCommentListAdapter(
        activity: ProgressLayoutTestActivity,
        item: CommentReplyUiModel,
        onToggleLike: (Long) -> Unit,
    ): CommentListHost {
        val adapter = CommentListAdapter(
            currentUser = null,
            onToggleLike = onToggleLike,
            onDeleteReply = {},
            onEditReply = {},
            onMentionReply = {},
            onShowLikes = {},
            onOpenProfile = { _, _ -> },
        )
        val recyclerView = RecyclerView(activity).apply {
            layoutManager = LinearLayoutManager(activity)
            itemAnimator = null
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
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
        adapter.submitList(listOf(item))
        return CommentListHost(adapter, recyclerView)
    }

    private fun feedItem(
        likeCount: Int,
        isLiked: Boolean,
        isLikePending: Boolean,
    ): FeedItemUiModel = FeedItemUiModel(
        id = 42L,
        type = KeyUtil.TEXT,
        headline = "Headline",
        body = "Body",
        likeCount = likeCount,
        isLikedByCurrentUser = isLiked,
        replyCount = 0,
        canEdit = false,
        canDelete = false,
        isLikePending = isLikePending,
        isDeletePending = false,
        createdAt = 1L,
        userAvatarUrl = null,
        userName = "user",
        userId = 10L,
        messengerAvatarUrl = null,
        messengerName = null,
        messengerId = null,
        recipientAvatarUrl = null,
        recipientName = null,
        recipientId = null,
        mediaId = null,
        mediaType = null,
        mediaTitleEnglish = null,
        mediaTitleOriginal = null,
        mediaCoverImageUrl = null,
        hasLikes = true,
        likes = emptyList(),
        feedText = "Body",
    )

    private fun commentReply(
        likeCount: Int,
        isLiked: Boolean,
        isLikePending: Boolean,
    ): CommentReplyUiModel = CommentReplyUiModel(
        id = 77L,
        reply = "Reply",
        createdAt = 1L,
        userId = 10L,
        userName = "user",
        userAvatar = null,
        likes = emptyList(),
        likeCount = likeCount,
        isLikedByCurrentUser = isLiked,
        isLikePending = isLikePending,
        isDeletePending = false,
    )

    private data class FeedListHost(
        val adapter: FeedListAdapter,
        val recyclerView: RecyclerView,
    )

    private data class CommentListHost(
        val adapter: CommentListAdapter,
        val recyclerView: RecyclerView,
    )
}
