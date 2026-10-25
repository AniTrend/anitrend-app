@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UnstableApiUsage")

package com.mxt.anitrend.widget

import android.content.DialogInterface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.CommentReplyRowCallbacks
import com.mxt.anitrend.ui.feed.CommentThreadLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommentThreadLazyListComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun otherUserReplyExposesMentionProfileAndLikesAndUpdatesWithSnapshot() {
        val mentionedIds = CopyOnWriteArrayList<Long>()
        val openedProfiles = CopyOnWriteArrayList<Long>()
        val shownLikes = CopyOnWriteArrayList<Long>()
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val replies = mutableStateOf(listOf(commentReply(userId = 20L)))
            installCommentThread(
                activity = activity,
                currentUserId = 21L,
                replies = replies,
                callbacks =
                replyCallbacks(
                    onMention = mentionedIds::add,
                    onOpenProfile = { _, id -> openedProfiles += id },
                    onShowLikes = shownLikes::add,
                ),
            )
            waitForIdle()

            val mentionDescription = activity.getString(R.string.description_comment_mention)
            val profileDescription = activity.getString(R.string.description_comment_open_profile, "AniTrend user")
            val profileNode = onNodeWithContentDescription(profileDescription)
            onNodeWithContentDescription(mentionDescription).assertIsDisplayed().assertHasClickAction().performClick()
            profileNode.assertIsDisplayed().assertHasClickAction().performClick()
            onNodeWithContentDescription(activity.getString(R.string.title_view_likes))
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            runOnIdle {
                assertEquals(listOf(77L), mentionedIds.toList())
                assertEquals(listOf(20L), openedProfiles.toList())
                assertEquals(listOf(77L), shownLikes.toList())
            }

            runOnUiThread {
                replies.value = listOf(commentReply(id = 88L, userId = 11L))
            }
            waitForIdle()
            onNodeWithContentDescription(mentionDescription).assertIsDisplayed().assertHasClickAction().performClick()
            profileNode.assertIsDisplayed().assertHasClickAction().performClick()
            onNodeWithContentDescription(activity.getString(R.string.title_view_likes)).performClick()
            runOnIdle {
                assertEquals(listOf(77L, 88L), mentionedIds.toList())
                assertEquals(listOf(20L, 11L), openedProfiles.toList())
                assertEquals(listOf(77L, 88L), shownLikes.toList())
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun ownerReplyShowsEditInsteadOfMention() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            installCommentThread(
                activity = activity,
                currentUserId = 10L,
                replies = mutableStateOf(listOf(commentReply())),
            )
            waitForIdle()

            onNodeWithContentDescription(activity.getString(R.string.description_comment_edit_reply))
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.description_comment_mention)).assertDoesNotExist()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingLikeStateDisablesReplyFavorite() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            installCommentThread(
                activity = activity,
                currentUserId = 20L,
                replies = mutableStateOf(listOf(commentReply(isLikePending = true))),
            )
            waitForIdle()

            onNodeWithContentDescription(activity.getString(R.string.description_favourite_action_add)).assertIsNotEnabled()
            onNodeWithContentDescription(activity.getString(R.string.busy_please_wait), useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingDeleteStateDisablesOwnerDeleteAction() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            installCommentThread(
                activity = activity,
                currentUserId = 10L,
                replies = mutableStateOf(listOf(commentReply(isDeletePending = true))),
            )
            waitForIdle()

            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity)).assertIsNotEnabled()
            onNodeWithContentDescription(activity.getString(R.string.busy_please_wait)).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun confirmedDeleteRoutesTheRenderedReplyId() {
        val deletedIds = CopyOnWriteArrayList<Long>()
        val confirmation = slot<DialogInterface.OnClickListener>()
        mockkStatic(DialogUtil::class)
        try {
            every { DialogUtil.createMessage(any(), any(), any(), capture(confirmation)) } returns Unit

            runAndroidComposeUiTest<ProgressLayoutTestActivity> {
                val activity = requireNotNull(activity)
                installCommentThread(
                    activity = activity,
                    currentUserId = 10L,
                    replies = mutableStateOf(listOf(commentReply())),
                    callbacks = replyCallbacks(onDelete = deletedIds::add),
                )
                waitForIdle()

                onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity))
                    .assertIsDisplayed()
                    .assertIsEnabled()
                    .performClick()
                runOnUiThread {
                    confirmation.captured.onClick(mockk(relaxed = true), DialogInterface.BUTTON_POSITIVE)
                }
                runOnIdle { assertEquals(listOf(77L), deletedIds.toList()) }
            }
        } finally {
            unmockkStatic(DialogUtil::class)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun commentOriginPrecedesRepliesInTheSingleLazyList() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val origin = originFeed()
            val reply = commentReply()
            installCommentThread(
                activity = activity,
                currentUserId = null,
                feed = origin,
                replies = mutableStateOf(listOf(reply)),
            )
            waitForIdle()

            val originNode = onNodeWithTag("comment-origin-${origin.id}")
            val replyNode = onNodeWithTag("comment-reply-${reply.id}")
            originNode.assertIsDisplayed()
            replyNode.assertIsDisplayed()
            assertTrue(
                "the origin is above replies in the same vertical list",
                originNode.fetchSemanticsNode().boundsInRoot.top < replyNode.fetchSemanticsNode().boundsInRoot.top,
            )
        }
    }

    private fun installCommentThread(
        activity: ProgressLayoutTestActivity,
        currentUserId: Long?,
        replies: MutableState<List<CommentReplyUiModel>>,
        feed: FeedItemUiModel? = null,
        callbacks: CommentReplyRowCallbacks = replyCallbacks(),
    ) {
        activity.installComposeContent {
            AniTrendTheme {
                CommentThreadLazyList(
                    feed = feed,
                    replies = replies.value,
                    currentUserId = currentUserId,
                    listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                    feedCallbacks = feedCallbacks(),
                    replyCallbacks = callbacks,
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                )
            }
        }
    }

    private fun feedCallbacks() = FeedRowCallbacks(
        onToggleLike = {},
        onDelete = {},
        onOpenMedia = { _, _ -> },
        onOpenComments = {},
        onEdit = {},
        onShowLikes = {},
        onOpenProfile = { _, _ -> },
        onLongPressMedia = { _, _ -> false },
    )

    private fun replyCallbacks(
        onToggleLike: (Long) -> Unit = {},
        onDelete: (Long) -> Unit = {},
        onEdit: (Long) -> Unit = {},
        onMention: (Long) -> Unit = {},
        onShowLikes: (Long) -> Unit = {},
        onOpenProfile: (android.view.View, Long) -> Unit = { _, _ -> },
    ) = CommentReplyRowCallbacks(
        onToggleLike = onToggleLike,
        onDelete = onDelete,
        onEdit = onEdit,
        onMention = onMention,
        onShowLikes = onShowLikes,
        onOpenProfile = onOpenProfile,
    )

    private fun originFeed() = FeedItemUiModel(
        id = 91L,
        type = KeyUtil.TEXT,
        headline = "Origin author",
        body = "Origin status",
        likeCount = 0,
        isLikedByCurrentUser = false,
        replyCount = 1,
        canEdit = false,
        canDelete = false,
        isLikePending = false,
        isDeletePending = false,
        createdAt = 1L,
        userAvatarUrl = null,
        userName = "Origin author",
        userId = 11L,
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
        hasLikes = false,
        likes = emptyList(),
        feedText = "Origin status",
    )

    private fun commentReply(
        id: Long = 77L,
        userId: Long? = 10L,
        isLiked: Boolean = false,
        isLikePending: Boolean = false,
        isDeletePending: Boolean = false,
    ): CommentReplyUiModel = CommentReplyUiModel(
        id = id,
        reply = "Reply body with a [link](https://example.test).",
        createdAt = 1L,
        userId = userId,
        userName = "AniTrend user",
        userAvatar = null,
        likes = emptyList(),
        likeCount = 3,
        isLikedByCurrentUser = isLiked,
        isLikePending = isLikePending,
        isDeletePending = isDeletePending,
    )
}
