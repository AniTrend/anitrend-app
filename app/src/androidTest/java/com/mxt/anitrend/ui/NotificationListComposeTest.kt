package com.mxt.anitrend.ui

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.NotificationItemUiModel
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.notification.NOTIFICATION_AVATAR_TAG
import com.mxt.anitrend.ui.notification.NOTIFICATION_ROW_TAG
import com.mxt.anitrend.ui.notification.NotificationList
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.widget.ProgressLayoutTestActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migrated contracts for the retired NotificationListAdapter: per-type text
 * projection, unread chrome and description suffix, and the row/avatar/long-click
 * routing split the fragment dispatches on.
 */
@RunWith(AndroidJUnit4::class)
class NotificationListComposeTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun rowsProjectPerTypeAndReadState() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installList(
                    activity = hostActivity,
                    items = listOf(mediaItem(id = 1L, isRead = false), personItem(id = 2L, isRead = true)),
                )
            }
            waitForIdle()

            // AIRING media row: series subject, media title header, episode content.
            onNodeWithText(context.getString(R.string.notification_series)).assertIsDisplayed()
            onAllNodesWithText("Gintama").assertCountEquals(1)
            onNodeWithText(context.getString(R.string.notification_episode, "12", "Gintama"))
                .assertIsDisplayed()

            // FOLLOWING person row: follow subject, user name header, context content.
            onNodeWithText(context.getString(R.string.notification_user_follow_activity)).assertIsDisplayed()
            onNodeWithText("follower").assertIsDisplayed()
            onNodeWithText("followed you").assertIsDisplayed()

            // Unread rows carry the unread state suffix in their description; read rows do not.
            val unreadSuffix = context.getString(R.string.layout_unread_state)
            onNode(
                SemanticsMatcher("description carries the unread suffix") {
                    it.config.getOrNull(SemanticsProperties.ContentDescription)
                        .orEmpty().any { line -> line.contains(unreadSuffix) }
                },
            ).assertIsDisplayed()
            onNode(
                SemanticsMatcher("description without the unread suffix") {
                    val description = it.config.getOrNull(SemanticsProperties.ContentDescription)
                    description != null && description.none { line -> line.contains(unreadSuffix) }
                },
            ).assertIsDisplayed()
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun rowAvatarAndLongClicksRouteSeparately() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            var rowClicked = -1L
            var avatarClicked = -1L
            var longClicked = -1L
            runOnUiThread {
                installList(
                    activity = hostActivity,
                    items = listOf(personItem(id = 7L, isRead = true)),
                    onRowClick = { rowClicked = it.id },
                    onAvatarClick = { avatarClicked = it.id },
                    onRowLongClick = { longClicked = it.id },
                )
            }
            waitForIdle()

            onNodeWithTag(NOTIFICATION_ROW_TAG).performClick()
            assertEquals(7L, rowClicked)
            assertEquals(-1L, avatarClicked)

            onNodeWithTag(NOTIFICATION_AVATAR_TAG, useUnmergedTree = true).performClick()
            assertEquals(7L, avatarClicked)

            onNodeWithTag(NOTIFICATION_ROW_TAG).performTouchInput { longClick() }
            assertEquals(7L, longClicked)
        }
    }

    @Test
    @OptIn(ExperimentalTestApi::class)
    fun listKeysRowsByStableNotificationId() {
        runAndroidComposeUiTest<ProgressLayoutTestActivity> {
            val hostActivity = requireNotNull(activity)
            runOnUiThread {
                installList(
                    activity = hostActivity,
                    items = listOf(mediaItem(id = 11L, isRead = true), personItem(id = 12L, isRead = true)),
                )
            }
            waitForIdle()
            onAllNodesWithTag(NOTIFICATION_ROW_TAG).assertCountEquals(2)
        }
    }

    private fun installList(
        activity: ProgressLayoutTestActivity,
        items: List<NotificationItemUiModel>,
        onRowClick: (NotificationItemUiModel) -> Unit = {},
        onAvatarClick: (NotificationItemUiModel) -> Unit = {},
        onRowLongClick: (NotificationItemUiModel) -> Unit = {},
    ) {
        activity.setContentView(
            ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    AniTrendTheme {
                        NotificationList(
                            items = items,
                            onRowClick = onRowClick,
                            onAvatarClick = onAvatarClick,
                            onRowLongClick = onRowLongClick,
                            onLoadMore = {},
                        )
                    }
                }
            },
        )
    }

    private fun mediaItem(id: Long, isRead: Boolean): NotificationItemUiModel = NotificationItemUiModel(
        id = id,
        record =
        NotificationRecord(
            id = id,
            type = KeyUtil.AIRING,
            createdAt = 1_700_000_000L,
            context = null,
            episode = 12,
            media =
            MediaSummaryRecord(
                id = 5L,
                titleUserPreferred = "Gintama",
                titleRomaji = null,
                titleEnglish = null,
                titleOriginal = null,
                coverImage = null,
                type = "ANIME",
                episodes = 0,
                chapters = 0,
                volumes = 0,
                status = null,
                siteUrl = null,
            ),
        ),
        isRead = isRead,
    )

    private fun personItem(id: Long, isRead: Boolean): NotificationItemUiModel = NotificationItemUiModel(
        id = id,
        record =
        NotificationRecord(
            id = id,
            type = KeyUtil.FOLLOWING,
            createdAt = 1_700_000_000L,
            context = "followed you",
            user = UserSummaryRecord(id = 7L, name = "follower", avatar = null, siteUrl = null),
        ),
        isRead = isRead,
    )
}
