@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UnstableApiUsage")

package com.mxt.anitrend.widget

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import com.mxt.anitrend.adapter.recycler.detail.CommentListAdapter
import com.mxt.anitrend.domain.model.CommentReplyUiModel
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.DialogUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommentListAdapterComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun otherUserRowExposesActionsAndRebindsItsRenderedAvatar() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            val imageFixture = requireNotNull(BitmapFactory.decodeResource(hostActivity.resources, R.drawable.reg_bg))
            val expectedImageCenterColor = imageFixture.getPixel(imageFixture.width / 2, imageFixture.height / 2)
            imageFixture.recycle()

            lateinit var host: CommentHost
            runOnUiThread {
                host =
                    installCommentHost(
                        activity = hostActivity,
                        currentUserId = 20L,
                        item = commentReply(hostActivity),
                    )
            }
            waitUntil("other-user comment holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()
            val mentionDescription = hostActivity.getString(R.string.description_comment_mention)
            val profileDescription = hostActivity.getString(R.string.description_comment_open_profile, "AniTrend user")
            val profileNode = onNodeWithContentDescription(profileDescription)

            waitUntil("local comment avatar renders", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFixtureVisible = image.centerColorMatches(expectedImageCenterColor)
                image.recycle()
                isFixtureVisible
            }
            onNodeWithContentDescription(mentionDescription)
                .assertIsDisplayed()
                .assertHasClickAction()
            profileNode.assertIsDisplayed().assertHasClickAction()
            onNodeWithContentDescription(hostActivity.getString(R.string.title_view_likes))
                .assertIsDisplayed()
                .assertHasClickAction()

            lateinit var recycledHolder: CommentListAdapter.CommentViewHolder
            runOnUiThread {
                recycledHolder =
                    requireNotNull(
                        host.recyclerView.findViewHolderForAdapterPosition(0) as? CommentListAdapter.CommentViewHolder,
                    )
                host.adapter.onViewRecycled(recycledHolder)
            }
            waitUntil("recycled comment avatar displays its fallback", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFallbackVisible = image.centerColorMatches(Color.rgb(255, 224, 74))
                image.recycle()
                isFallbackVisible
            }
            profileNode.assertIsDisplayed().assertHasClickAction()

            runOnUiThread { host.adapter.onBindViewHolder(recycledHolder, 0) }
            waitUntil("rebound comment avatar renders", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFixtureVisible = image.centerColorMatches(expectedImageCenterColor)
                image.recycle()
                isFixtureVisible
            }
            profileNode.assertIsDisplayed().assertHasClickAction()

            runOnUiThread {
                host.adapter.submitList(
                    listOf(
                        commentReply(
                            activity = hostActivity,
                            id = 88L,
                            userId = 11L,
                        ),
                    ),
                )
            }
            waitUntil("comment action follows the rebound reply", 5_000L) {
                host.adapter.currentList.singleOrNull()?.id == 88L &&
                    host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()
            onNodeWithContentDescription(mentionDescription)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun ownerRowExposesEditInsteadOfMention() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: CommentHost
            runOnUiThread {
                host =
                    installCommentHost(
                        activity = hostActivity,
                        currentUserId = 10L,
                        item = commentReply(hostActivity),
                    )
            }
            waitUntil("owner comment holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()
            val editDescription = hostActivity.getString(R.string.description_comment_edit_reply)
            val mentionDescription = hostActivity.getString(R.string.description_comment_mention)

            onNodeWithContentDescription(editDescription)
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithContentDescription(mentionDescription).assertDoesNotExist()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingLikeStateDisablesCommentFavorite() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: CommentHost
            runOnUiThread {
                host =
                    installCommentHost(
                        activity = hostActivity,
                        currentUserId = 20L,
                        item = commentReply(hostActivity, isLikePending = true),
                    )
            }
            waitUntil("pending-like comment holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            onNodeWithContentDescription(hostActivity.getString(R.string.description_favourite_action_add))
                .assertIsNotEnabled()
            onNodeWithContentDescription(hostActivity.getString(R.string.busy_please_wait), useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingDeleteStateDisablesOwnerDeleteAction() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: CommentHost
            runOnUiThread {
                host =
                    installCommentHost(
                        activity = hostActivity,
                        currentUserId = 10L,
                        item = commentReply(hostActivity, isDeletePending = true),
                    )
            }
            waitUntil("pending-delete comment holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            onNodeWithContentDescription(hostActivity.getString(R.string.dialog_title_delete_activity))
                .assertIsNotEnabled()
            onNodeWithContentDescription(hostActivity.getString(R.string.busy_please_wait)).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun confirmedDeleteRoutesTheRenderedReplyId() {
        val deletedIds = CopyOnWriteArrayList<Long>()
        val confirmation = slot<DialogInterface.OnClickListener>()

        mockkStatic(DialogUtil::class)
        try {
            every {
                DialogUtil.createMessage(any(), any(), any(), capture(confirmation))
            } returns Unit

            runAndroidComposeUiTest<ProgressLayoutTestActivity> {
                val hostActivity = requireNotNull(activity)
                lateinit var host: CommentHost
                runOnUiThread {
                    host =
                        installCommentHost(
                            activity = hostActivity,
                            currentUserId = 10L,
                            item = commentReply(hostActivity),
                            onDeleteReply = deletedIds::add,
                        )
                }
                waitUntil("deletable comment holder binds", 5_000L) {
                    host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
                }
                waitForIdle()

                onNodeWithContentDescription(hostActivity.getString(R.string.dialog_title_delete_activity))
                    .assertIsDisplayed()
                    .assertIsEnabled()
                    .performClick()
                runOnUiThread {
                    confirmation.captured.onClick(
                        mockk(relaxed = true),
                        DialogInterface.BUTTON_POSITIVE,
                    )
                }
                runOnIdle { assertEquals(listOf(77L), deletedIds.toList()) }
            }
        } finally {
            unmockkStatic(DialogUtil::class)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun recycledCommentFavoriteDoesNotDispatchUntilRebound() {
        val toggledIds = CopyOnWriteArrayList<Long>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            lateinit var host: CommentHost
            runOnUiThread {
                host =
                    installCommentHost(
                        activity = hostActivity,
                        currentUserId = 20L,
                        item = commentReply(hostActivity),
                        onToggleLike = toggledIds::add,
                    )
            }
            waitUntil("comment favorite holder binds", 5_000L) {
                host.adapter.itemCount == 1 && host.recyclerView.findViewHolderForAdapterPosition(0) != null
            }
            waitForIdle()

            val addDescription = hostActivity.getString(R.string.description_favourite_action_add)
            onNodeWithContentDescription(addDescription).performClick()
            runOnIdle { assertEquals(listOf(77L), toggledIds.toList()) }

            lateinit var recycledHolder: CommentListAdapter.CommentViewHolder
            runOnUiThread {
                recycledHolder =
                    requireNotNull(
                        host.recyclerView.findViewHolderForAdapterPosition(0) as? CommentListAdapter.CommentViewHolder,
                    )
                host.adapter.onViewRecycled(recycledHolder)
            }
            waitForIdle()
            onNodeWithContentDescription(addDescription).assertIsEnabled().performClick()
            runOnIdle { assertEquals(listOf(77L), toggledIds.toList()) }

            runOnUiThread { host.adapter.onBindViewHolder(recycledHolder, 0) }
            waitForIdle()
            onNodeWithContentDescription(addDescription).assertIsEnabled().performClick()
            runOnIdle { assertEquals(listOf(77L, 77L), toggledIds.toList()) }
        }
    }

    private fun installCommentHost(
        activity: ProgressLayoutTestActivity,
        currentUserId: Long,
        item: CommentReplyUiModel,
        onToggleLike: (Long) -> Unit = {},
        onDeleteReply: (Long) -> Unit = {},
    ): CommentHost {
        val adapter =
            CommentListAdapter(
                currentUser = UserBase(name = "current-user").apply { id = currentUserId },
                onToggleLike = onToggleLike,
                onDeleteReply = onDeleteReply,
                onEditReply = {},
                onMentionReply = {},
                onShowLikes = {},
                onOpenProfile = { _, _ -> },
            )
        val recyclerView =
            RecyclerView(activity).apply {
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
        return CommentHost(adapter, recyclerView)
    }

    private fun commentReply(
        activity: ProgressLayoutTestActivity,
        id: Long = 77L,
        userId: Long? = 10L,
        isLiked: Boolean = false,
        isLikePending: Boolean = false,
        isDeletePending: Boolean = false,
    ): CommentReplyUiModel = CommentReplyUiModel(
        id = id,
        reply = "A **marked-up** reply with a [link](https://example.test).",
        createdAt = 1L,
        userId = userId,
        userName = "AniTrend user",
        userAvatar = "android.resource://${activity.packageName}/${R.drawable.reg_bg}",
        likes = emptyList(),
        likeCount = 3,
        isLikedByCurrentUser = isLiked,
        isLikePending = isLikePending,
        isDeletePending = isDeletePending,
    )

    private data class CommentHost(
        val adapter: CommentListAdapter,
        val recyclerView: RecyclerView,
    )
}

private fun Bitmap.centerColorMatches(expectedColor: Int): Boolean {
    val actualColor = getPixel(width / 2, height / 2)
    val tolerance = 24
    return abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance
}
