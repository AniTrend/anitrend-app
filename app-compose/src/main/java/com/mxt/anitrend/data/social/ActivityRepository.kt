package com.mxt.anitrend.data.social

import com.apollographql.apollo.ApolloClient
import com.mxt.anitrend.data.graphql.ActivityDetailQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class ActivityDetail(
    val id: Long,
    val type: String?,
    val createdAt: Int,
    val replyCount: Int,
    val isLocked: Boolean?,
    val siteUrl: String?,
    val status: String?,
    val progress: String?,
    val text: String?,
    val mediaId: Long?,
    val mediaTitle: String?,
    val mediaCoverMedium: String?,
    val mediaType: String?,
    val userName: String?,
    val userAvatarMedium: String?,
    val replies: List<Reply>,
) {
    data class Reply(
        val id: Long,
        val text: String?,
        val createdAt: Int,
        val likeCount: Int,
        val userName: String?,
        val userAvatarMedium: String?,
    )
}

interface ActivityRepository {
    fun observeActivity(id: Int): Flow<ActivityDetail?>
}

class ApolloActivityRepository(
    private val apolloClient: ApolloClient,
) : ActivityRepository {

    override fun observeActivity(id: Int): Flow<ActivityDetail?> = flow {
        val response = apolloClient.query(ActivityDetailQuery(id = id)).execute()
        val activity = response.data?.Activity
        emit(activity?.toActivityDetail())
    }

    private fun ActivityDetailQuery.Activity.toActivityDetail(): ActivityDetail {
        val list = onListActivity
        val text = onTextActivity
        return ActivityDetail(
            id = (list?.id ?: text?.id ?: 0).toLong(),
            type = list?.type?.name ?: text?.type?.name,
            createdAt = list?.createdAt ?: text?.createdAt ?: 0,
            replyCount = list?.replyCount ?: text?.replyCount ?: 0,
            isLocked = list?.isLocked ?: text?.isLocked,
            siteUrl = list?.siteUrl ?: text?.siteUrl,
            status = list?.status,
            progress = list?.progress,
            text = text?.text,
            mediaId = list?.media?.id?.toLong(),
            mediaTitle = list?.media?.title?.userPreferred,
            mediaCoverMedium = list?.media?.coverImage?.medium,
            mediaType = list?.media?.type?.name,
            userName = list?.user?.name ?: text?.user?.name,
            userAvatarMedium = list?.user?.avatar?.medium ?: text?.user?.avatar?.medium,
            replies = buildReplyList(list, text),
        )
    }

    private fun buildReplyList(
        listActivity: ActivityDetailQuery.OnListActivity?,
        textActivity: ActivityDetailQuery.OnTextActivity?,
    ): List<ActivityDetail.Reply> {
        val fromList = listActivity?.replies?.filterNotNull()?.map { it.toReply() }.orEmpty()
        val fromText = textActivity?.replies?.filterNotNull()?.map { it.toReply() }.orEmpty()
        return fromList + fromText
    }

    private fun ActivityDetailQuery.Reply.toReply() = ActivityDetail.Reply(
        id = id.toLong(),
        text = text,
        createdAt = createdAt,
        likeCount = likeCount,
        userName = user?.name,
        userAvatarMedium = user?.avatar?.medium,
    )

    private fun ActivityDetailQuery.Reply1.toReply() = ActivityDetail.Reply(
        id = id.toLong(),
        text = text,
        createdAt = createdAt,
        likeCount = likeCount,
        userName = user?.name,
        userAvatarMedium = user?.avatar?.medium,
    )
}
