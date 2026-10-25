@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.widget

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.FeedItemLazyList
import com.mxt.anitrend.ui.feed.FeedRowCallbacks
import com.mxt.anitrend.ui.media.ManualGridAppendGate
import com.mxt.anitrend.util.KeyUtil
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedProgressRowComposeTest {
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun progressRowExposesActionsAndRoutesItsMediaAndProfile() {
        val openedProfiles = CopyOnWriteArrayList<Long>()
        val openedMedia = CopyOnWriteArrayList<Long>()
        val shownLikes = CopyOnWriteArrayList<Long>()
        val openedComments = CopyOnWriteArrayList<Long>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val item = progressItem(id = 51L, coverUrl = localCoverUrl(activity))
            installFeedList(
                activity,
                mutableStateOf(listOf(item)),
                callbacks =
                feedCallbacks(
                    onOpenProfile = { _, id -> openedProfiles += id },
                    onOpenMedia = { _, model -> openedMedia += model.id },
                    onShowLikes = { model -> shownLikes += model.id },
                    onOpenComments = { openedComments += it },
                ),
            )
            waitForIdle()

            val profileDescription =
                activity.getString(R.string.description_comment_open_profile, requireNotNull(item.userName))
            val mediaDescription = requireNotNull(item.mediaTitleEnglish)
            val profileNode = onNodeWithContentDescription(profileDescription)
            profileNode.assertIsDisplayed().assertHasClickAction()
            waitUntil("missing avatar displays the shared fallback", 5_000L) {
                val image = profileNode.captureToImage().asAndroidBitmap()
                val isFallbackVisible = image.centerColorMatches(Color.rgb(255, 224, 74))
                image.recycle()
                isFallbackVisible
            }
            onNodeWithContentDescription(mediaDescription)
                .assertIsDisplayed()
                .assertHasClickAction()
                .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))
            onNodeWithContentDescription(activity.getString(R.string.title_view_likes))
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            onNodeWithContentDescription(activity.getString(R.string.description_feed_open_comments))
                .assertIsDisplayed()
                .assertHasClickAction()
                .performClick()
            onNodeWithContentDescription(activity.getString(R.string.description_favourite_action_remove))
                .assertIsDisplayed()
                .assertIsSelected()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity))
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithText(item.headline.toString()).assertIsDisplayed()
            onNodeWithText(requireNotNull(item.mediaTitleEnglish)).assertIsDisplayed()
            onNodeWithText(requireNotNull(item.mediaTitleOriginal)).assertIsDisplayed()
            onNodeWithText(" 3 ").assertIsDisplayed()
            onNodeWithText("12", useUnmergedTree = true, substring = true).assertIsDisplayed()

            onNodeWithContentDescription(profileDescription).performClick()
            onNodeWithContentDescription(mediaDescription).performClick()
            runOnIdle {
                assertEquals(listOf(10L), openedProfiles.toList())
                assertEquals(listOf(51L), openedMedia.toList())
                assertEquals(listOf(51L), shownLikes.toList())
                assertEquals(listOf(51L), openedComments.toList())
            }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun listVariantHidesFavoriteControlsAndDoesNotRouteMedia() {
        val openedMedia = CopyOnWriteArrayList<Long>()
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val item =
                progressItem(
                    id = 52L,
                    type = KeyUtil.MEDIA_LIST,
                    headline = "plans to read: Sample Manga",
                    mediaType = KeyUtil.MANGA,
                    mediaTitleEnglish = "Sample Manga",
                    mediaTitleOriginal = "サンプル漫画",
                    coverUrl = localCoverUrl(activity),
                    likeCount = 0,
                    isLiked = false,
                )
            installFeedList(
                activity,
                mutableStateOf(listOf(item)),
                callbacks = feedCallbacks(onOpenMedia = { _, model -> openedMedia += model.id }),
            )
            waitForIdle()

            onNodeWithContentDescription(activity.getString(R.string.title_view_likes)).assertDoesNotExist()
            onNodeWithContentDescription(activity.getString(R.string.description_favourite_action_add)).assertDoesNotExist()
            onNodeWithContentDescription(activity.getString(R.string.description_favourite_action_remove)).assertDoesNotExist()
            onNodeWithContentDescription(activity.getString(R.string.description_feed_open_comments))
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity))
                .assertIsDisplayed()
                .assertHasClickAction()
            onNodeWithText(item.headline.toString()).assertIsDisplayed()
            onNodeWithText(requireNotNull(item.mediaTitleEnglish)).assertIsDisplayed()

            val cover = onNodeWithContentDescription(requireNotNull(item.mediaTitleEnglish))
            cover.assertIsDisplayed()
            val coverSemantics = cover.fetchSemanticsNode().config
            assertFalse("list cover has no click action", coverSemantics.contains(SemanticsActions.OnClick))
            assertFalse("list cover has no long-click action", coverSemantics.contains(SemanticsActions.OnLongClick))
            runOnIdle { assertTrue("media-list cover does not navigate", openedMedia.isEmpty()) }
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun pendingProgressSnapshotDisablesFavoriteAndDeleteActions() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val item =
                progressItem(
                    id = 53L,
                    coverUrl = null,
                    isLiked = false,
                    isLikePending = true,
                    isDeletePending = true,
                )
            installFeedList(activity, mutableStateOf(listOf(item)))
            waitForIdle()

            onNodeWithContentDescription(activity.getString(R.string.description_favourite_action_add)).assertIsNotEnabled()
            onNodeWithContentDescription(activity.getString(R.string.dialog_title_delete_activity)).assertIsNotEnabled()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun updatedFeedSnapshotReplacesTheOldProgressMedia() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val activity = requireNotNull(activity)
            val initialItem = progressItem(id = 54L, coverUrl = localCoverUrl(activity))
            val items = mutableStateOf(listOf(initialItem))
            installFeedList(activity, items)
            waitForIdle()
            onNodeWithContentDescription(requireNotNull(initialItem.mediaTitleEnglish)).assertIsDisplayed()

            val replacement =
                initialItem.copy(
                    headline = "Progress without media title",
                    mediaTitleEnglish = null,
                    mediaTitleOriginal = null,
                    mediaCoverImageUrl = null,
                )
            runOnUiThread { items.value = listOf(replacement) }
            waitForIdle()

            onNodeWithText("Progress without media title").assertIsDisplayed()
            onNodeWithContentDescription(requireNotNull(initialItem.mediaTitleEnglish)).assertDoesNotExist()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun shortNearEndAppendStartsANewLoadMoreObservation() {
        val items = mutableStateOf(listOf(progressItem(id = 61L, coverUrl = null)))
        val loadMoreRevision = mutableIntStateOf(0)
        val appendGate = ManualGridAppendGate()
        val requestedTailIds = CopyOnWriteArrayList<Long>()

        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            hostActivity.installComposeContent {
                AniTrendTheme {
                    FeedItemLazyList(
                        items = items.value,
                        listState = rememberLazyListState(),
                        callbacks = feedCallbacks(),
                        onLoadMore = {
                            if (appendGate.canRequestNextPage) {
                                appendGate.onRequestStarted()
                                requestedTailIds += items.value.last().id
                            }
                        },
                        loadMoreRevision = loadMoreRevision.intValue,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            waitForIdle()
            runOnIdle { assertEquals(listOf(61L), requestedTailIds.toList()) }

            runOnUiThread {
                appendGate.onRequestSucceeded(itemsInPage = 1)
                items.value = listOf(
                    progressItem(id = 61L, coverUrl = null),
                    progressItem(id = 62L, coverUrl = null),
                )
                loadMoreRevision.intValue++
            }
            waitForIdle()
            runOnIdle { assertEquals(listOf(61L, 62L), requestedTailIds.toList()) }
        }
    }

    private fun installFeedList(
        activity: ProgressLayoutTestActivity,
        items: MutableState<List<FeedItemUiModel>>,
        callbacks: FeedRowCallbacks = feedCallbacks(),
    ) {
        activity.installComposeContent {
            AniTrendTheme {
                FeedItemLazyList(
                    items = items.value,
                    listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                    callbacks = callbacks,
                    onLoadMore = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    private fun feedCallbacks(
        onToggleLike: (Long) -> Unit = {},
        onDelete: (Long) -> Unit = {},
        onOpenMedia: (android.view.View, FeedItemUiModel) -> Unit = { _, _ -> },
        onOpenComments: (Long) -> Unit = {},
        onEdit: (FeedItemUiModel) -> Unit = {},
        onShowLikes: (FeedItemUiModel) -> Unit = {},
        onOpenProfile: (android.view.View, Long) -> Unit = { _, _ -> },
        onLongPressMedia: (android.view.View, FeedItemUiModel) -> Boolean = { _, _ -> false },
    ) = FeedRowCallbacks(
        onToggleLike = onToggleLike,
        onDelete = onDelete,
        onOpenMedia = onOpenMedia,
        onOpenComments = onOpenComments,
        onEdit = onEdit,
        onShowLikes = onShowLikes,
        onOpenProfile = onOpenProfile,
        onLongPressMedia = onLongPressMedia,
    )

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
}

private fun android.graphics.Bitmap.centerColorMatches(expectedColor: Int): Boolean {
    val actualColor = getPixel(width / 2, height / 2)
    val tolerance = 24
    return abs(Color.red(actualColor) - Color.red(expectedColor)) <= tolerance &&
        abs(Color.green(actualColor) - Color.green(expectedColor)) <= tolerance &&
        abs(Color.blue(actualColor) - Color.blue(expectedColor)) <= tolerance
}
