@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UnstableApiUsage")

package com.mxt.anitrend.widget

import android.content.DialogInterface
import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.Color
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.index.FeedListAdapter
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.After
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
    fun otherUserStatusKeepsAccessibleActionsAndRendersAvatarFallbackAcrossRecycle() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val localAvatarUrl = avatarUrl(activity, Color.BLUE)
            val expectedImageCenterColor = Color.BLUE

            lateinit var host: FeedStatusHost
            runOnUiThread {
                host =
                    installStatusHost(
                        activity = activity,
                        item = statusItem(),
                    )
            }
            waitUntil("other-user status holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val profileDescription = activity.getString(R.string.description_comment_open_profile, STATUS_USER_NAME)
            val likesDescription = activity.getString(R.string.title_view_likes)
            val commentsDescription = activity.getString(R.string.description_feed_open_comments)
            val profileNode = onNodeWithContentDescription(profileDescription)
            profileNode.assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(likesDescription).assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(commentsDescription).assertIsDisplayed().assertHasClickAction()
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

            runOnUiThread { host.adapter.submitList(listOf(statusItem(avatarUrl = localAvatarUrl))) }
            waitUntil("local status avatar renders", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFixtureVisible = image.centerColorMatches(expectedImageCenterColor)
                image.recycle()
                isFixtureVisible
            }

            lateinit var recycledHolder: RecyclerView.ViewHolder
            runOnUiThread {
                recycledHolder = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0))
                host.adapter.onViewRecycled(recycledHolder)
            }
            waitUntil("recycled status avatar displays its fallback", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFallbackVisible = image.centerColorMatches(Color.rgb(255, 224, 74))
                image.recycle()
                isFallbackVisible
            }
            profileNode.assertIsDisplayed().assertHasClickAction()

            runOnUiThread { host.adapter.onBindViewHolder(recycledHolder, 0) }
            waitUntil("rebound status avatar renders", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFixtureVisible = image.centerColorMatches(expectedImageCenterColor)
                image.recycle()
                isFixtureVisible
            }
            profileNode.assertIsDisplayed().assertHasClickAction()

            var markdownView: RichMarkdownTextView? = null
            runOnUiThread {
                val row = requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0)).itemView
                markdownView = row.findViewById(R.id.widget_status_text)
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
    fun ownerStatusShowsEditDeleteAndLoadingEmptyStates() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val idleItem = statusItem(body = null, canEdit = true, canDelete = true)
            lateinit var host: FeedStatusHost
            runOnUiThread { host = installStatusHost(activity, idleItem) }
            waitUntil("owner status holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            onNodeWithContentDescription(activity.getString(R.string.edit_status_title))
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity))
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()

            var emptyBody: RichMarkdownTextView? = null
            runOnUiThread {
                emptyBody =
                    requireNotNull(host.recyclerView.findViewHolderForAdapterPosition(0))
                        .itemView
                        .findViewById(R.id.widget_status_text)
            }
            assertEquals("No content available", requireNotNull(emptyBody).text.toString())

            val favouriteDescription = activity.getString(R.string.description_favourite_action_add)
            val busyDescription = activity.getString(R.string.busy_please_wait)
            val pendingFavourite = idleItem.copy(isLikePending = true)
            runOnUiThread { host.adapter.submitList(listOf(pendingFavourite)) }
            waitUntil("favorite pending state commits", 5_000L) {
                host.adapter.currentList.singleOrNull()?.isLikePending == true
            }
            waitForIdle()
            onNodeWithContentDescription(favouriteDescription).assertIsNotEnabled()
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()

            val pendingDelete = idleItem.copy(isDeletePending = true)
            runOnUiThread { host.adapter.submitList(listOf(pendingDelete)) }
            waitUntil("delete pending state commits", 5_000L) {
                host.adapter.currentList.singleOrNull()?.isDeletePending == true
            }
            waitForIdle()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity)).assertIsNotEnabled()
            onNodeWithContentDescription(busyDescription, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun deleteConfirmationCannotActOnAnotherFeedAfterRebind() {
        val deletedFeedIds = CopyOnWriteArrayList<Long>()
        val confirmation = slot<DialogInterface.OnClickListener>()

        mockkStatic(DialogUtil::class)
        try {
            every {
                DialogUtil.createMessage(any(), any(), any(), capture(confirmation))
            } returns Unit

            runAndroidComposeUiTest<ProgressLayoutTestActivity> {
                val activity = requireNotNull(activity)
                lateinit var host: FeedStatusHost
                runOnUiThread {
                    host =
                        installStatusHost(
                            activity = activity,
                            item = statusItem(id = 42L, canDelete = true),
                            onDeleteFeed = deletedFeedIds::add,
                        )
                }
                waitUntil("first deletable status binds", 5_000L) {
                    host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
                }
                waitForIdle()

                val deleteDescription = activity.getString(R.string.dialog_title_delete_activity)
                onNodeWithContentDescription(deleteDescription).performClick()
                val oldConfirmation = confirmation.captured

                runOnUiThread {
                    host.adapter.submitList(listOf(statusItem(id = 43L, canDelete = true)))
                }
                waitUntil("rebound deletable status commits", 5_000L) {
                    host.adapter.currentList.singleOrNull()?.id == 43L &&
                        host.recyclerView.findViewHolderForAdapterPosition(0) != null
                }
                waitForIdle()
                runOnUiThread {
                    oldConfirmation.onClick(
                        mockk(relaxed = true),
                        DialogInterface.BUTTON_POSITIVE,
                    )
                }
                runOnIdle { assertTrue("stale confirmation cannot delete the rebound status", deletedFeedIds.isEmpty()) }

                onNodeWithContentDescription(deleteDescription).performClick()
                runOnUiThread {
                    confirmation.captured.onClick(
                        mockk(relaxed = true),
                        DialogInterface.BUTTON_POSITIVE,
                    )
                }
                runOnIdle { assertEquals(listOf(43L), deletedFeedIds.toList()) }
            }
        } finally {
            unmockkStatic(DialogUtil::class)
        }
    }

    private fun installStatusHost(
        activity: ProgressLayoutTestActivity,
        item: FeedItemUiModel,
        onDeleteFeed: (Long) -> Unit = {},
    ): FeedStatusHost {
        val adapter =
            FeedListAdapter(
                onToggleLikeAction = {},
                onDeleteFeedAction = onDeleteFeed,
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
        return FeedStatusHost(adapter, recyclerView)
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

    private data class FeedStatusHost(
        val adapter: FeedListAdapter,
        val recyclerView: RecyclerView,
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
