package com.mxt.anitrend.data.notification

import com.apollographql.apollo.ApolloClient
import com.mxt.anitrend.data.graphql.UserNotificationsQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class AppNotification(
    val id: Long,
    val type: String,
    val context: String?,
    val createdAt: Int,
    val userId: Long?,
    val userName: String?,
    val userAvatar: String?,
    val mediaId: Long?,
    val mediaTitle: String?,
    val mediaCoverMedium: String?,
    val episode: Int?,
    val activityId: Long?,
    val commentId: Long?,
    val threadId: Long?,
    val reason: String?,
    val deletedMediaTitle: String?,
)

interface NotificationRepository {
    fun observeNotifications(page: Int = 1): Flow<List<AppNotification>>
}

class ApolloNotificationRepository(
    private val apolloClient: ApolloClient,
) : NotificationRepository {

    override fun observeNotifications(page: Int): Flow<List<AppNotification>> = flow {
        val response = apolloClient.query(
            UserNotificationsQuery(page = com.apollographql.apollo.api.Optional.present(page))
        ).execute()

        val items = response.data?.Page?.notifications
            ?.mapNotNull { it?.toAppNotification() }
            .orEmpty()

        emit(items)
    }

    private fun UserNotificationsQuery.Notification.toAppNotification(): AppNotification {
        onAiringNotification?.let { a ->
            return AppNotification(
                id = a.id.toLong(),
                type = a.type?.rawValue ?: __typename,
                context = a.contexts?.filterNotNull()?.joinToString(" ") ?: __typename,
                createdAt = a.createdAt ?: 0,
                userId = null,
                userName = null,
                userAvatar = null,
                mediaId = a.media?.id?.toLong(),
                mediaTitle = a.media?.title?.userPreferred,
                mediaCoverMedium = a.media?.coverImage?.medium,
                episode = a.episode,
                activityId = null,
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onFollowingNotification?.let { f ->
            return AppNotification(
                id = f.id.toLong(),
                type = f.type?.rawValue ?: __typename,
                context = f.context,
                createdAt = f.createdAt ?: 0,
                userId = f.user?.id?.toLong(),
                userName = f.user?.name,
                userAvatar = f.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = null,
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onActivityMessageNotification?.let { a ->
            return AppNotification(
                id = a.id.toLong(),
                type = a.type?.rawValue ?: __typename,
                context = a.context,
                createdAt = a.createdAt ?: 0,
                userId = a.user?.id?.toLong(),
                userName = a.user?.name,
                userAvatar = a.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = a.activityId.toLong(),
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onActivityMentionNotification?.let { a ->
            return AppNotification(
                id = a.id.toLong(),
                type = a.type?.rawValue ?: __typename,
                context = a.context,
                createdAt = a.createdAt ?: 0,
                userId = a.user?.id?.toLong(),
                userName = a.user?.name,
                userAvatar = a.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = a.activityId.toLong(),
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onActivityReplyNotification?.let { a ->
            return AppNotification(
                id = a.id.toLong(),
                type = a.type?.rawValue ?: __typename,
                context = a.context,
                createdAt = a.createdAt ?: 0,
                userId = a.user?.id?.toLong(),
                userName = a.user?.name,
                userAvatar = a.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = a.activityId.toLong(),
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onActivityLikeNotification?.let { a ->
            return AppNotification(
                id = a.id.toLong(),
                type = a.type?.rawValue ?: __typename,
                context = a.context,
                createdAt = a.createdAt ?: 0,
                userId = a.user?.id?.toLong(),
                userName = a.user?.name,
                userAvatar = a.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = a.activityId.toLong(),
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onActivityReplySubscribedNotification?.let { a ->
            return AppNotification(
                id = a.id.toLong(),
                type = a.type?.rawValue ?: __typename,
                context = a.context,
                createdAt = a.createdAt ?: 0,
                userId = a.user?.id?.toLong(),
                userName = a.user?.name,
                userAvatar = a.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = a.activityId.toLong(),
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onActivityReplyLikeNotification?.let { a ->
            return AppNotification(
                id = a.id.toLong(),
                type = a.type?.rawValue ?: __typename,
                context = a.context,
                createdAt = a.createdAt ?: 0,
                userId = a.user?.id?.toLong(),
                userName = a.user?.name,
                userAvatar = a.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = a.activityId.toLong(),
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onThreadCommentMentionNotification?.let { t ->
            return AppNotification(
                id = t.id.toLong(),
                type = t.type?.rawValue ?: __typename,
                context = t.context,
                createdAt = t.createdAt ?: 0,
                userId = t.user?.id?.toLong(),
                userName = t.user?.name,
                userAvatar = t.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = null,
                commentId = t.commentId.toLong(),
                threadId = t.thread?.id?.toLong(),
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onThreadCommentReplyNotification?.let { t ->
            return AppNotification(
                id = t.id.toLong(),
                type = t.type?.rawValue ?: __typename,
                context = t.context,
                createdAt = t.createdAt ?: 0,
                userId = t.user?.id?.toLong(),
                userName = t.user?.name,
                userAvatar = t.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = null,
                commentId = t.commentId.toLong(),
                threadId = t.thread?.id?.toLong(),
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onThreadCommentSubscribedNotification?.let { t ->
            return AppNotification(
                id = t.id.toLong(),
                type = t.type?.rawValue ?: __typename,
                context = t.context,
                createdAt = t.createdAt ?: 0,
                userId = t.user?.id?.toLong(),
                userName = t.user?.name,
                userAvatar = t.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = null,
                commentId = t.commentId.toLong(),
                threadId = t.thread?.id?.toLong(),
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onThreadCommentLikeNotification?.let { t ->
            return AppNotification(
                id = t.id.toLong(),
                type = t.type?.rawValue ?: __typename,
                context = t.context,
                createdAt = t.createdAt ?: 0,
                userId = t.user?.id?.toLong(),
                userName = t.user?.name,
                userAvatar = t.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = null,
                commentId = t.commentId.toLong(),
                threadId = t.thread?.id?.toLong(),
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onThreadLikeNotification?.let { t ->
            return AppNotification(
                id = t.id.toLong(),
                type = t.type?.rawValue ?: __typename,
                context = t.context,
                createdAt = t.createdAt ?: 0,
                userId = t.user?.id?.toLong(),
                userName = t.user?.name,
                userAvatar = t.user?.avatar?.medium,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = null,
                commentId = null,
                threadId = t.thread?.id?.toLong(),
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onRelatedMediaAdditionNotification?.let { r ->
            return AppNotification(
                id = r.id.toLong(),
                type = r.type?.rawValue ?: __typename,
                context = r.context,
                createdAt = r.createdAt ?: 0,
                userId = null,
                userName = null,
                userAvatar = null,
                mediaId = r.media?.id?.toLong(),
                mediaTitle = r.media?.title?.userPreferred,
                mediaCoverMedium = r.media?.coverImage?.medium,
                episode = null,
                activityId = null,
                commentId = null,
                threadId = null,
                reason = null,
                deletedMediaTitle = null,
            )
        }
        onMediaDataChangeNotification?.let { m ->
            return AppNotification(
                id = m.id.toLong(),
                type = m.type?.rawValue ?: __typename,
                context = m.context,
                createdAt = m.createdAt ?: 0,
                userId = null,
                userName = null,
                userAvatar = null,
                mediaId = m.media?.id?.toLong(),
                mediaTitle = m.media?.title?.userPreferred,
                mediaCoverMedium = m.media?.coverImage?.medium,
                episode = null,
                activityId = null,
                commentId = null,
                threadId = null,
                reason = m.reason,
                deletedMediaTitle = null,
            )
        }
        onMediaMergeNotification?.let { m ->
            return AppNotification(
                id = m.id.toLong(),
                type = m.type?.rawValue ?: __typename,
                context = m.context,
                createdAt = m.createdAt ?: 0,
                userId = null,
                userName = null,
                userAvatar = null,
                mediaId = m.media?.id?.toLong(),
                mediaTitle = m.media?.title?.userPreferred,
                mediaCoverMedium = m.media?.coverImage?.medium,
                episode = null,
                activityId = null,
                commentId = null,
                threadId = null,
                reason = m.reason,
                deletedMediaTitle = m.deletedMediaTitles?.filterNotNull()?.joinToString(", "),
            )
        }
        onMediaDeletionNotification?.let { m ->
            return AppNotification(
                id = m.id.toLong(),
                type = m.type?.rawValue ?: __typename,
                context = m.context,
                createdAt = m.createdAt ?: 0,
                userId = null,
                userName = null,
                userAvatar = null,
                mediaId = null,
                mediaTitle = null,
                mediaCoverMedium = null,
                episode = null,
                activityId = null,
                commentId = null,
                threadId = null,
                reason = m.reason,
                deletedMediaTitle = m.deletedMediaTitle,
            )
        }
        return AppNotification(
            id = 0L,
            type = __typename,
            context = null,
            createdAt = 0,
            userId = null,
            userName = null,
            userAvatar = null,
            mediaId = null,
            mediaTitle = null,
            mediaCoverMedium = null,
            episode = null,
            activityId = null,
            commentId = null,
            threadId = null,
            reason = null,
            deletedMediaTitle = null,
        )
    }
}
