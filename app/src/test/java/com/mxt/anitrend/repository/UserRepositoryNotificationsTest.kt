package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.attribute.GraphError
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.domain.model.NotificationRecord
import com.mxt.anitrend.graphql.generated.NotificationType
import com.mxt.anitrend.graphql.generated.UserNotifications
import com.mxt.anitrend.graphql.generated.UserNotificationsData
import com.mxt.anitrend.model.api.retro.anilist.UserService
import com.mxt.anitrend.model.entity.base.NotificationHistory
import io.objectbox.Box
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryNotificationsTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val service = mock(UserService::class.java)
    private val boxQuery = mock(BoxQuery::class.java)
    private val repository = UserRepository(
        userService = service,
        boxQuery = boxQuery,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `getUserNotifications success maps page nodes preserving order and page info`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = userNotificationsData(
                        notifications = listOf(activityMessageNode(id = 1, activityId = 10), airingNode(id = 4, episode = 12)),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getUserNotifications()

        assertTrue(result.isSuccess)
        val page: NotificationPageResult = result.getOrThrow()
        assertEquals(listOf(1L, 4L), page.notifications.map { it.id })
        val activity = page.notifications.first()
        assertEquals("ACTIVITY_MESSAGE", activity.type)
        assertEquals(10L, activity.activityId)
        assertEquals("Hello there", activity.context)
        assertEquals(1_600_000_000L, activity.createdAt)
        assertEquals("Sasuke", activity.user?.name)
        assertEquals("https://avatar", activity.user?.avatar)
        val airing = page.notifications[1]
        assertEquals("AIRING", airing.type)
        assertEquals(12, airing.episode)
        assertEquals(listOf("Episode 12 airs soon"), airing.contexts)
        assertEquals(7L, airing.media?.id)
        assertEquals("ANIME", airing.media?.type)
        assertEquals("Naruto", airing.media?.titleUserPreferred)
        assertEquals("https://cover", airing.media?.coverImage)
        assertNotNull(page.pageInfo)
        assertEquals(1, page.pageInfo?.currentPage)
        assertTrue(page.pageInfo?.hasNextPage == true)
        assertEquals(2, page.pageInfo?.total)
    }

    @Test
    fun `getUserNotifications success maps thread and deletion specific fields`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = userNotificationsData(
                        notifications = listOf(
                            threadCommentReplyNode(id = 2, commentId = 33, threadId = 55),
                            mediaMergeNode(id = 3, mediaId = 8),
                            mediaDeletionNode(id = 9),
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getUserNotifications()

        assertTrue(result.isSuccess)
        val page: NotificationPageResult = result.getOrThrow()
        val threadComment = page.notifications[0]
        assertEquals("THREAD_COMMENT_REPLY", threadComment.type)
        assertEquals(33L, threadComment.commentId)
        assertEquals(55L, threadComment.threadId)
        val merge = page.notifications[1]
        assertEquals("MEDIA_MERGE", merge.type)
        assertEquals(listOf("Old Title", "Older Title"), merge.deletedMediaTitles)
        assertEquals("duplicate merge", merge.reason)
        assertEquals(8L, merge.media?.id)
        val deletion = page.notifications[2]
        assertEquals("MEDIA_DELETION", deletion.type)
        assertEquals("Deleted Title", deletion.deletedMediaTitle)
        assertEquals("removed by user", deletion.reason)
    }

    @Test
    fun `getUserNotifications success returns empty result for empty nodes`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = userNotificationsData(notifications = emptyList()),
                    errors = null,
                ),
            ),
        )

        val result = repository.getUserNotifications()

        assertTrue(result.isSuccess)
        val page: NotificationPageResult = result.getOrThrow()
        assertTrue(page.notifications.isEmpty())
        assertNotNull(page.pageInfo)
    }

    @Test
    fun `getUserNotifications drops null nodes while preserving order`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = userNotificationsData(
                        notifications = listOf(
                            activityMessageNode(id = 1, activityId = 10),
                            null,
                            airingNode(id = 4, episode = 12),
                        ),
                    ),
                    errors = null,
                ),
            ),
        )

        val result = repository.getUserNotifications()

        assertTrue(result.isSuccess)
        val page: NotificationPageResult = result.getOrThrow()
        assertEquals(listOf(1L, 4L), page.notifications.map { it.id })
    }

    @Test
    fun `getUserNotifications GraphQL error returns failed Result with message`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(
            Response.success(
                GraphContainer<UserNotificationsData>(
                    data = null,
                    errors = listOf(GraphError(message = "Notifications failed")),
                ),
            ),
        )

        val result = repository.getUserNotifications()

        assertTrue(result.isFailure)
        assertEquals("Notifications failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserNotifications null body returns failed Result`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(Response.success(null))

        val result = repository.getUserNotifications()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserNotifications null data returns failed Result`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(
            Response.success(
                GraphContainer<UserNotificationsData>(
                    data = null,
                    errors = null,
                ),
            ),
        )

        val result = repository.getUserNotifications()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUserNotifications null page returns failed Result`() = runTest {
        val request = UserNotifications.request(page = null, perPage = null, type = null, typeIn = null, resetNotificationCount = false)
        `when`(service.getUserNotifications(request)).thenReturn(
            Response.success(
                GraphContainer(
                    data = UserNotificationsData(page = null),
                    errors = null,
                ),
            ),
        )

        val result = repository.getUserNotifications()

        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `saveNotificationHistory persists notification ids from page result`() {
        @Suppress("UNCHECKED_CAST")
        val box = mock(Box::class.java) as Box<NotificationHistory>
        `when`(boxQuery.getBoxStore(NotificationHistory::class.java)).thenReturn(box)
        val pageResult = NotificationPageResult(
            notifications = listOf(
                NotificationRecord(id = 11L),
                NotificationRecord(id = 22L),
            ),
            pageInfo = null,
        )

        repository.saveNotificationHistory(pageResult)

        verify(box).put(
            argThat<Collection<NotificationHistory>> { notifications ->
                notifications.map { it.id } == listOf(11L, 22L)
            },
        )
    }

    private fun userNotificationsData(
        notifications: List<UserNotificationsData.PageNotifications?>,
    ): UserNotificationsData = UserNotificationsData(
        page = UserNotificationsData.Page(
            notifications = notifications,
            pageInfo = UserNotificationsData.PagePageInfo(
                currentPage = 1,
                hasNextPage = true,
                lastPage = 1,
                perPage = 10,
                total = notifications.size,
            ),
        ),
    )

    private fun activityMessageNode(
        id: Int,
        activityId: Int,
    ): UserNotificationsData.PageNotifications.ActivityMessageNotification = UserNotificationsData.PageNotifications.ActivityMessageNotification(
        activityId = activityId,
        context = "Hello there",
        createdAt = 1_600_000_000,
        id = id,
        type = NotificationType.ACTIVITY_MESSAGE,
        user = UserNotificationsData.ActivityMessageNotificationPageNotificationsUser(
            avatar = UserNotificationsData.ActivityMessageNotificationPageNotificationsUserAvatar(
                large = "https://avatar",
                medium = null,
            ),
            bannerImage = null,
            id = 5,
            isFollowing = null,
            name = "Sasuke",
            updatedAt = null,
        ),
    )

    private fun airingNode(
        id: Int,
        episode: Int,
    ): UserNotificationsData.PageNotifications.AiringNotification = UserNotificationsData.PageNotifications.AiringNotification(
        contexts = listOf("Episode 12 airs soon"),
        createdAt = 1_600_000_100,
        episode = episode,
        id = id,
        type = NotificationType.AIRING,
        media = UserNotificationsData.AiringNotificationPageNotificationsMedia(
            averageScore = 80,
            bannerImage = null,
            chapters = null,
            coverImage = UserNotificationsData.AiringNotificationPageNotificationsMediaCoverImage(
                color = null,
                extraLarge = "https://cover",
                large = null,
                medium = null,
            ),
            endDate = null,
            episodes = 12,
            format = null,
            id = 7,
            isAdult = null,
            isFavourite = false,
            meanScore = null,
            mediaListEntry = null,
            nextAiringEpisode = null,
            season = null,
            siteUrl = "https://anilist.co/anime/7",
            startDate = null,
            status = null,
            title = UserNotificationsData.AiringNotificationPageNotificationsMediaTitle(
                english = "Naruto",
                native = null,
                romaji = null,
                userPreferred = "Naruto",
            ),
            type = com.mxt.anitrend.graphql.generated.MediaType.ANIME,
            updatedAt = null,
            volumes = null,
        ),
    )

    private fun threadCommentReplyNode(
        id: Int,
        commentId: Int,
        threadId: Int,
    ): UserNotificationsData.PageNotifications.ThreadCommentReplyNotification = UserNotificationsData.PageNotifications.ThreadCommentReplyNotification(
        commentId = commentId,
        context = "New reply",
        createdAt = 1_600_000_200,
        id = id,
        type = NotificationType.THREAD_COMMENT_REPLY,
        thread = UserNotificationsData.ThreadCommentReplyNotificationPageNotificationsThread(id = threadId),
        user = UserNotificationsData.ThreadCommentReplyNotificationPageNotificationsUser(
            avatar = null,
            bannerImage = null,
            id = 6,
            isFollowing = null,
            name = "Naruto",
            updatedAt = null,
        ),
    )

    private fun mediaMergeNode(
        id: Int,
        mediaId: Int,
    ): UserNotificationsData.PageNotifications.MediaMergeNotification = UserNotificationsData.PageNotifications.MediaMergeNotification(
        context = "Merged entries",
        createdAt = 1_600_000_300,
        deletedMediaTitles = listOf("Old Title", "Older Title"),
        id = id,
        type = NotificationType.MEDIA_MERGE,
        reason = "duplicate merge",
        media = UserNotificationsData.MediaMergeNotificationPageNotificationsMedia(
            averageScore = null,
            bannerImage = null,
            chapters = null,
            coverImage = null,
            endDate = null,
            episodes = null,
            format = null,
            id = mediaId,
            isAdult = null,
            isFavourite = false,
            meanScore = null,
            mediaListEntry = null,
            nextAiringEpisode = null,
            season = null,
            siteUrl = null,
            startDate = null,
            status = null,
            title = null,
            type = null,
            updatedAt = null,
            volumes = null,
        ),
    )

    private fun mediaDeletionNode(
        id: Int,
    ): UserNotificationsData.PageNotifications.MediaDeletionNotification = UserNotificationsData.PageNotifications.MediaDeletionNotification(
        context = "Media removed",
        createdAt = 1_600_000_400,
        deletedMediaTitle = "Deleted Title",
        id = id,
        reason = "removed by user",
        type = NotificationType.MEDIA_DELETION,
    )
}
