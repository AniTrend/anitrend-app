@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UnstableApiUsage")

package com.mxt.anitrend.widget

import android.view.View
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.text.Spanned
import android.text.style.ClickableSpan
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.FeedItemLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedStatusRowComposeTest {
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
    fun otherUserStatusKeepsAccessibleActionsAndMarkdownInTheComposeList() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val localAvatarUrl = avatarUrl(activity, Color.BLUE)
            val expectedImageCenterColor = Color.BLUE
            val items = mutableStateOf(listOf(statusItem()))
            installStatusList(activity, items)
            waitForIdle()

            val profileDescription = activity.getString(R.string.description_comment_open_profile, STATUS_USER_NAME)
            val profileNode = onNodeWithContentDescription(profileDescription)
            profileNode.assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.title_view_likes)).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.description_feed_open_comments)).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.description_favourite_action_add))
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.edit_status_title)).assertDoesNotExist()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity)).assertDoesNotExist()

            waitUntil("missing status avatar displays its fallback", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFallbackVisible = image.centerColorMatches(Color.rgb(255, 224, 74))
                image.recycle()
                isFallbackVisible
            }

            runOnUiThread { items.value = listOf(statusItem(avatarUrl = localAvatarUrl)) }
            waitUntil("local status avatar renders", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFixtureVisible = image.centerColorMatches(expectedImageCenterColor)
                image.recycle()
                isFixtureVisible
            }

            var markdownView: RichMarkdownTextView? = null
            waitUntil("status Markdown view is composed", 5_000L) {
                runOnUiThread { markdownView = activity.findViewById(R.id.widget_status_text) }
                markdownView != null
            }
            val markdown = requireNotNull(markdownView)
            assertEquals("A status with bold text and AniList.", markdown.text.toString())
            assertTrue(markdown.isTextSelectable)
            assertNotNull(markdown.movementMethod)
            val clickableSpans =
                (markdown.text as Spanned).getSpans(0, markdown.text.length, ClickableSpan::class.java)
            assertTrue("Markdown links remain interactive", clickableSpans.isNotEmpty())
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun ownerStatusShowsEditDeleteAndCanonicalBusyStates() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val item = statusItem(body = null, canEdit = true, canDelete = true)
            val items = mutableStateOf(listOf(item))
            installStatusList(activity, items)
            waitForIdle()

            onNodeWithContentDescription(activity.getString(R.string.edit_status_title))
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity))
                .assertIsDisplayed()
                .assertIsEnabled()
            var emptyBody: RichMarkdownTextView? = null
            runOnUiThread { emptyBody = activity.findViewById(R.id.widget_status_text) }
            assertEquals("No content available", requireNotNull(emptyBody).text.toString())

            val favouriteDescription = activity.getString(R.string.description_favourite_action_add)
            val busyDescription = activity.getString(R.string.busy_please_wait)
            runOnUiThread { items.value = listOf(item.copy(isLikePending = true)) }
            waitForIdle()
            onNodeWithContentDescription(favouriteDescription).assertIsNotEnabled()
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()

            runOnUiThread { items.value = listOf(item.copy(isDeletePending = true)) }
            waitForIdle()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity)).assertIsNotEnabled()
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun deleteConfirmationCannotActAfterItsFeedLeavesTheComposeSnapshot() {
        val deletedFeedIds = CopyOnWriteArrayList<Long>()
        val confirmation = slot<DialogInterface.OnClickListener>()
        mockkStatic(DialogUtil::class)
        try {
            every { DialogUtil.createMessage(any(), any(), any(), capture(confirmation)) } returns Unit

            runAndroidComposeUiTest<ProgressLayoutTestActivity> {
                val activity = requireNotNull(activity)
                val items = mutableStateOf(listOf(statusItem(id = 42L, canDelete = true)))
                installStatusList(activity, items, onDeleteFeed = deletedFeedIds::add)
                waitForIdle()

                val deleteDescription = activity.getString(R.string.dialog_title_delete_activity)
                onNodeWithContentDescription(deleteDescription).performClick()
                val oldConfirmation = confirmation.captured

                runOnUiThread { items.value = listOf(statusItem(id = 43L, canDelete = true)) }
                waitForIdle()
                runOnUiThread {
                    oldConfirmation.onClick(mockk(relaxed = true), DialogInterface.BUTTON_POSITIVE)
                }
                runOnIdle { assertTrue("stale confirmation cannot delete the removed status", deletedFeedIds.isEmpty()) }

                onNodeWithContentDescription(deleteDescription).performClick()
                runOnUiThread {
                    confirmation.captured.onClick(mockk(relaxed = true), DialogInterface.BUTTON_POSITIVE)
                }
                runOnIdle { assertEquals(listOf(43L), deletedFeedIds.toList()) }
            }
        } finally {
            unmockkStatic(DialogUtil::class)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun messageListKeepsMarkdownIdentityLikesCommentsAndEditActions() {
        val openedProfiles = CopyOnWriteArrayList<Pair<Int, Long>>()
        val likesFeedIds = CopyOnWriteArrayList<Long>()
        val commentedFeedIds = CopyOnWriteArrayList<Long>()
        val editedFeedIds = CopyOnWriteArrayList<Long>()
        val message =
            statusItem(id = 44L, body = "A **message** [link](https://anilist.co)", canEdit = true).copy(
                type = KeyUtil.MESSAGE,
                messengerName = "Sender",
                messengerId = 11L,
                recipientName = "Recipient",
                recipientId = 12L,
            )

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            installStatusList(
                activity = activity,
                items = mutableStateOf(listOf(message)),
                callbacks =
                FeedRowCallbacks(
                    onToggleLike = {},
                    onDelete = {},
                    onOpenMedia = { _, _ -> },
                    onOpenComments = { commentedFeedIds.add(it) },
                    onEdit = { editedFeedIds.add(it.id) },
                    onShowLikes = { likesFeedIds.add(it.id) },
                    onOpenProfile = { target, userId -> openedProfiles.add(target.id to userId) },
                    onLongPressMedia = { _, _ -> false },
                ),
            )
            waitForIdle()

            val markdown = activity.findViewById<RichMarkdownTextView>(R.id.widget_status_text)
            assertEquals("A message link", markdown.text.toString())
            assertTrue((markdown.text as Spanned).getSpans(0, markdown.text.length, ClickableSpan::class.java).isNotEmpty())
            assertEquals("Recipient", activity.findViewById<android.widget.TextView>(R.id.recipient_user_name).text.toString())
            assertEquals("Sender", activity.findViewById<android.widget.TextView>(R.id.messenger_user_name).text.toString())
            assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.messenger_user_name).visibility)

            runOnUiThread {
                activity.findViewById<View>(R.id.messenger_avatar).performClick()
                activity.findViewById<View>(R.id.recipient_avatar).performClick()
                activity.findViewById<View>(R.id.widget_users).performClick()
                activity.findViewById<View>(R.id.widget_comment).performClick()
                activity.findViewById<View>(R.id.widget_edit).performClick()
            }
            runOnIdle {
                assertEquals(
                    listOf(R.id.messenger_avatar to 11L, R.id.recipient_avatar to 12L),
                    openedProfiles.toList(),
                )
                assertEquals(listOf(44L), likesFeedIds.toList())
                assertEquals(listOf(44L), commentedFeedIds.toList())
                assertEquals(listOf(44L), editedFeedIds.toList())
            }
        }
    }

    private fun installStatusList(
        activity: ProgressLayoutTestActivity,
        items: MutableState<List<FeedItemUiModel>>,
        onDeleteFeed: (Long) -> Unit = {},
        callbacks: FeedRowCallbacks? = null,
    ) {
        activity.installComposeContent {
            AniTrendTheme {
                FeedItemLazyList(
                    items = items.value,
                    listState = rememberLazyListState(),
                    callbacks = callbacks ?: FeedRowCallbacks(
                        onToggleLike = {},
                        onDelete = onDeleteFeed,
                        onOpenMedia = { _, _ -> },
                        onOpenComments = {},
                        onEdit = {},
                        onShowLikes = {},
                        onOpenProfile = { _, _ -> },
                        onLongPressMedia = { _, _ -> false },
                    ),
                    onLoadMore = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    private fun statusItem(
        id: Long = 42L,
        body: String? = "A status with **bold text** and [AniList](https://anilist.co).",
        canEdit: Boolean = false,
        canDelete: Boolean = false,
        isLikePending: Boolean = false,
        isDeletePending: Boolean = false,
        avatarUrl: String? = null,
    ) = FeedItemUiModel(
        id = id,
        type = KeyUtil.TEXT,
        headline = STATUS_USER_NAME,
        body = body,
        likeCount = 3,
        isLikedByCurrentUser = false,
        replyCount = 2,
        canEdit = canEdit,
        canDelete = canDelete,
        isLikePending = isLikePending,
        isDeletePending = isDeletePending,
        createdAt = System.currentTimeMillis() / 1000L - 86_400L,
        userAvatarUrl = avatarUrl,
        userName = STATUS_USER_NAME,
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
        feedText = body,
    )

    private companion object {
        const val STATUS_USER_NAME = "AniTrend user"
    }
}

private fun Bitmap.centerColorMatches(expectedColor: Int): Boolean {
    val actualColor = getPixel(width / 2, height / 2)
    val tolerance = 24
    return abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance
}
