@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.CommentReplyRowCallbacks
import com.mxt.anitrend.ui.feed.CommentThreadLazyList
import com.mxt.anitrend.ui.feed.FeedItemLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.util.KeyUtil
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedCommentFavouriteComposeTest {

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun feedFavoriteForwardsActionAndRendersPendingAndCanonicalState() {
        val toggledIds = CopyOnWriteArrayList<Long>()
        val items = mutableStateOf(listOf(feedItem(likeCount = 5, isLiked = false, isLikePending = false)))

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            hostActivity.installComposeContent {
                AniTrendTheme {
                    FeedItemLazyList(
                        items = items.value,
                        listState = rememberLazyListState(),
                        callbacks = feedCallbacks(onToggleLike = toggledIds::add),
                        onLoadMore = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            val busyDescription = hostActivity.getString(R.string.busy_please_wait)
            onNodeWithContentDescription(addDescription).assertIsDisplayed().performClick()
            runOnIdle { assertEquals(listOf(42L), toggledIds.toList()) }

            runOnUiThread {
                items.value = listOf(feedItem(likeCount = 5, isLiked = false, isLikePending = true))
            }
            waitForIdle()
            onNodeWithContentDescription(addDescription).assertIsNotEnabled()
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread {
                items.value = listOf(feedItem(likeCount = 6, isLiked = true, isLikePending = false))
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
        val replies = mutableStateOf(listOf(commentReply(likeCount = 2, isLiked = false, isLikePending = false)))

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            hostActivity.installComposeContent {
                AniTrendTheme {
                    CommentThreadLazyList(
                        feed = null,
                        replies = replies.value,
                        currentUserId = null,
                        listState = rememberLazyListState(),
                        feedCallbacks = feedCallbacks(),
                        replyCallbacks = replyCallbacks(onToggleLike = toggledIds::add),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            val removeDescription = hostActivity.getString(R.string.description_favourite_action_remove)
            onNodeWithContentDescription(addDescription).assertIsDisplayed().performClick()
            runOnIdle { assertEquals(listOf(77L), toggledIds.toList()) }

            runOnUiThread {
                replies.value = listOf(commentReply(likeCount = 3, isLiked = true, isLikePending = false))
            }
            waitForIdle()
            onNodeWithContentDescription(removeDescription).assertIsSelected()
            onNodeWithText(" 3 ").assertIsDisplayed()
        }
    }

    private fun feedCallbacks(onToggleLike: (Long) -> Unit = {}) = FeedRowCallbacks(
        onToggleLike = onToggleLike,
        onDelete = {},
        onOpenMedia = { _, _ -> },
        onOpenComments = {},
        onEdit = {},
        onShowLikes = {},
        onOpenProfile = { _, _ -> },
        onLongPressMedia = { _, _ -> false },
    )

    private fun replyCallbacks(onToggleLike: (Long) -> Unit = {}) = CommentReplyRowCallbacks(
        onToggleLike = onToggleLike,
        onDelete = {},
        onEdit = {},
        onMention = {},
        onShowLikes = {},
        onOpenProfile = { _, _ -> },
    )

    private fun feedItem(
        likeCount: Int,
        isLiked: Boolean,
        isLikePending: Boolean,
    ) = FeedItemUiModel(
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
    ) = CommentReplyUiModel(
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
}
