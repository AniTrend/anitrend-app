package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.DeleteActivity
import com.mxt.anitrend.graphql.generated.DeleteActivityData
import com.mxt.anitrend.graphql.generated.DeleteActivityReply
import com.mxt.anitrend.graphql.generated.DeleteActivityReplyData
import com.mxt.anitrend.graphql.generated.FeedList
import com.mxt.anitrend.graphql.generated.FeedListData
import com.mxt.anitrend.graphql.generated.FeedListReply
import com.mxt.anitrend.graphql.generated.FeedListReplyData
import com.mxt.anitrend.graphql.generated.FeedMessage
import com.mxt.anitrend.graphql.generated.FeedMessageData
import com.mxt.anitrend.graphql.generated.SaveActivityReply
import com.mxt.anitrend.graphql.generated.SaveActivityReplyData
import com.mxt.anitrend.graphql.generated.SaveMessageActivity
import com.mxt.anitrend.graphql.generated.SaveMessageActivityData
import com.mxt.anitrend.graphql.generated.SaveTextActivity
import com.mxt.anitrend.graphql.generated.SaveTextActivityData
import com.mxt.anitrend.data.mapper.toFeedRecord
import com.mxt.anitrend.data.mapper.toPageInfoRecord
import com.mxt.anitrend.data.mapper.toFeedReplyRecord
import com.mxt.anitrend.data.store.feed.FeedQueryKey
import com.mxt.anitrend.data.store.feed.FeedScope
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.model.api.retro.anilist.FeedService
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import com.mxt.anitrend.model.entity.base.MediaBase as MediaEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.anilist.FeedList as FeedListEntity

/**
 * Record-typed page surface for the feed migration (Lane C).
 *
 * Additive only: legacy entity-typed repository methods remain unchanged until
 * the final hub/repository phase switches callers onto these record surfaces.
 */
data class FeedRecordPage(
    val feeds: List<FeedRecord>,
    val pageInfo: PageInfoRecord?,
)

/**
 * Record-typed detail surface for a single feed activity plus its replies.
 */
data class FeedDetailResult(
    val feed: FeedRecord,
    val replies: List<FeedReplyRecord>,
)

internal fun PageContainer<FeedListEntity>.toFeedRecords(revision: Long): List<FeedRecord> = pageData.map { it.toFeedRecord(revision = revision) }

internal fun PageContainer<FeedListEntity>.toRecordPageInfo(): PageInfoRecord? = takeIf { it.hasPageInfo() }?.pageInfo?.toPageInfoRecord()

class FeedRepository(
    private val feedService: FeedService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val feedStore: FeedStore? = null,
) : AbstractRepository(ioDispatcher) {

    suspend fun getFeedList(
        page: Int? = null,
        perPage: Int? = null,
        id: Long? = null,
        isFollowing: Boolean? = null,
        userId: Long? = null,
        type: ActivityType? = null,
        isMixed: Boolean? = null,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        queryKey: FeedQueryKey? = null,
        readToken: Long = 0L,
    ): Result<PageContainer<FeedListEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedList.request(page = page, perPage = perPage, id = id?.toInt(), isFollowing = isFollowing, userId = userId?.toInt(), type = type, isMixed = isMixed, asHtml = asHtml)
            val response = feedService.getFeedList(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedPageContainer()
                val pageInfo = result.takeIf { it.hasPageInfo() }?.pageInfo?.toPageInfoRecord()
                val resolvedQueryKey = queryKey ?: FeedQueryKey(
                    scope = when {
                        id != null -> FeedScope.MEDIA
                        userId != null -> FeedScope.USER
                        else -> FeedScope.GLOBAL
                    },
                    userId = userId,
                    mediaId = id,
                    activityType = type,
                    isFollowing = isFollowing,
                    isMixed = isMixed,
                )

                if (commitToStore && queryKey != null && feedStore != null) {
                    feedStore.apply(
                        FeedStoreChange.PageLoaded(
                            queryKey = resolvedQueryKey,
                            page = pageInfo?.currentPage ?: page ?: 1,
                            token = readToken,
                            feeds = result.pageData.map { it.toFeedRecord(revision = readToken) },
                            pageInfo = pageInfo,
                        ),
                    )
                }

                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFeedListReply(
        id: Long,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
        readToken: Long = 0L,
    ): Result<FeedListEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedListReply.request(id = id.toInt(), asHtml = asHtml)
            val response = feedService.getFeedListReply(request)
            if (response.isSuccessful) {
                handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedDetailEntity().also { result ->
                    if (commitToStore) {
                        feedStore?.apply(
                            FeedStoreChange.FeedDetailLoaded(
                                feed = result.toFeedRecord(revision = readToken),
                                replies = result.replies.orEmpty().map { reply ->
                                    reply.toFeedReplyRecord(
                                        activityId = result.id,
                                        revision = readToken,
                                    )
                                },
                            ),
                        )
                    }
                }
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFeedMessage(
        page: Int? = null,
        perPage: Int? = null,
        messengerId: Long? = null,
        userId: Long? = null,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        queryKey: FeedQueryKey? = null,
        readToken: Long = 0L,
    ): Result<PageContainer<FeedListEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedMessage.request(page = page, perPage = perPage, messengerId = messengerId?.toInt(), userId = userId?.toInt(), asHtml = asHtml)
            val response = feedService.getFeedMessage(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedPageContainer()
                val pageInfo = result.takeIf { it.hasPageInfo() }?.pageInfo?.toPageInfoRecord()
                val resolvedQueryKey = queryKey ?: FeedQueryKey(
                    scope = if (userId != null) FeedScope.MESSAGE_INBOX else FeedScope.MESSAGE_OUTBOX,
                    userId = userId ?: messengerId,
                    mediaId = null,
                    activityType = null,
                    isFollowing = null,
                    isMixed = null,
                )

                if (commitToStore && queryKey != null && feedStore != null) {
                    feedStore.apply(
                        FeedStoreChange.PageLoaded(
                            queryKey = resolvedQueryKey,
                            page = pageInfo?.currentPage ?: page ?: 1,
                            token = readToken,
                            feeds = result.pageData.map { it.toFeedRecord(revision = readToken) },
                            pageInfo = pageInfo,
                        ),
                    )
                }

                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Mutation operations

    suspend fun saveTextActivity(
        id: Long? = null,
        text: String?,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<FeedListEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveTextActivity.request(id = id?.toInt(), text = text, asHtml = asHtml)
            val response = feedService.saveTextActivity(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedListEntity()
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.FeedUpserted(
                            feed = result.toFeedRecord(revision = revision),
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun saveMessageActivity(
        id: Long? = null,
        message: String?,
        recipientId: Long,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<FeedListEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveMessageActivity.request(id = id?.toInt(), message = message, recipientId = recipientId.toInt(), asHtml = asHtml)
            val response = feedService.saveMessageActivity(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedListEntity()
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.FeedUpserted(
                            feed = result.toFeedRecord(revision = revision),
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun saveActivityReply(
        id: Long? = null,
        activityId: Long,
        text: String?,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<FeedReply> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveActivityReply.request(id = id?.toInt(), activityId = activityId.toInt(), text = text, asHtml = asHtml)
            val response = feedService.saveActivityReply(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedReplyEntity()
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.ReplyUpserted(
                            feedId = activityId,
                            reply = result.toFeedReplyRecord(activityId = activityId, revision = revision),
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun deleteActivity(
        id: Long,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<DeleteState> = withContext(ioDispatcher) {
        runCatching {
            val request = DeleteActivity.request(id = id.toInt())
            val response = feedService.deleteActivity(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toDeleteState()
                if (commitToStore && result.isDeleted) {
                    feedStore?.apply(
                        FeedStoreChange.FeedDeleted(
                            feedId = id,
                            revision = revision,
                        ),
                    )
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun deleteActivityReply(
        id: Long,
        feedId: Long? = null,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<DeleteState> = withContext(ioDispatcher) {
        runCatching {
            val request = DeleteActivityReply.request(id = id.toInt())
            val response = feedService.deleteActivityReply(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toDeleteState()
                if (commitToStore && result.isDeleted) {
                    val parentFeedId = feedId ?: feedStore?.state?.value?.repliesById?.get(id)?.activityId
                    if (parentFeedId != null) {
                        feedStore?.apply(
                            FeedStoreChange.ReplyDeleted(
                                feedId = parentFeedId,
                                replyId = id,
                                revision = revision,
                            ),
                        )
                    }
                }
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Record-typed additive surface (Lane C).
    //
    // These methods map responses at the data boundary into FeedRecord /
    // FeedReplyRecord and may commit to the FeedStore. They preserve the exact
    // transport, request parameters, page-info, null handling, revision-token,
    // and failure semantics of the legacy entity-typed methods above, so the
    // final hub/repository phase can switch callers without behaviour changes.

    suspend fun getFeedListRecords(
        page: Int? = null,
        perPage: Int? = null,
        id: Long? = null,
        isFollowing: Boolean? = null,
        userId: Long? = null,
        type: ActivityType? = null,
        isMixed: Boolean? = null,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        queryKey: FeedQueryKey? = null,
        readToken: Long = 0L,
    ): Result<FeedRecordPage> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedList.request(page = page, perPage = perPage, id = id?.toInt(), isFollowing = isFollowing, userId = userId?.toInt(), type = type, isMixed = isMixed, asHtml = asHtml)
            val response = feedService.getFeedList(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedPageContainer()
                val pageInfo = result.toRecordPageInfo()
                val feeds = result.toFeedRecords(revision = readToken)
                val resolvedQueryKey = queryKey ?: FeedQueryKey(
                    scope = when {
                        id != null -> FeedScope.MEDIA
                        userId != null -> FeedScope.USER
                        else -> FeedScope.GLOBAL
                    },
                    userId = userId,
                    mediaId = id,
                    activityType = type,
                    isFollowing = isFollowing,
                    isMixed = isMixed,
                )

                if (commitToStore && queryKey != null && feedStore != null) {
                    feedStore.apply(
                        FeedStoreChange.PageLoaded(
                            queryKey = resolvedQueryKey,
                            page = pageInfo?.currentPage ?: page ?: 1,
                            token = readToken,
                            feeds = feeds,
                            pageInfo = pageInfo,
                        ),
                    )
                }

                FeedRecordPage(feeds = feeds, pageInfo = pageInfo)
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFeedListReplyRecords(
        id: Long,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
        readToken: Long = 0L,
    ): Result<FeedDetailResult> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedListReply.request(id = id.toInt(), asHtml = asHtml)
            val response = feedService.getFeedListReply(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedDetailEntity()
                val feed = result.toFeedRecord(revision = readToken)
                val replies = result.replies.orEmpty().map { reply ->
                    reply.toFeedReplyRecord(
                        activityId = result.id,
                        revision = readToken,
                    )
                }
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.FeedDetailLoaded(
                            feed = feed,
                            replies = replies,
                        ),
                    )
                }
                FeedDetailResult(feed = feed, replies = replies)
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFeedMessageRecords(
        page: Int? = null,
        perPage: Int? = null,
        messengerId: Long? = null,
        userId: Long? = null,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        queryKey: FeedQueryKey? = null,
        readToken: Long = 0L,
    ): Result<FeedRecordPage> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedMessage.request(page = page, perPage = perPage, messengerId = messengerId?.toInt(), userId = userId?.toInt(), asHtml = asHtml)
            val response = feedService.getFeedMessage(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedPageContainer()
                val pageInfo = result.toRecordPageInfo()
                val feeds = result.toFeedRecords(revision = readToken)
                val resolvedQueryKey = queryKey ?: FeedQueryKey(
                    scope = if (userId != null) FeedScope.MESSAGE_INBOX else FeedScope.MESSAGE_OUTBOX,
                    userId = userId ?: messengerId,
                    mediaId = null,
                    activityType = null,
                    isFollowing = null,
                    isMixed = null,
                )

                if (commitToStore && queryKey != null && feedStore != null) {
                    feedStore.apply(
                        FeedStoreChange.PageLoaded(
                            queryKey = resolvedQueryKey,
                            page = pageInfo?.currentPage ?: page ?: 1,
                            token = readToken,
                            feeds = feeds,
                            pageInfo = pageInfo,
                        ),
                    )
                }

                FeedRecordPage(feeds = feeds, pageInfo = pageInfo)
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun saveTextActivityRecord(
        id: Long? = null,
        text: String?,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<FeedRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveTextActivity.request(id = id?.toInt(), text = text, asHtml = asHtml)
            val response = feedService.saveTextActivity(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedListEntity()
                val feed = result.toFeedRecord(revision = revision)
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.FeedUpserted(
                            feed = feed,
                        ),
                    )
                }
                feed
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun saveMessageActivityRecord(
        id: Long? = null,
        message: String?,
        recipientId: Long,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<FeedRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveMessageActivity.request(id = id?.toInt(), message = message, recipientId = recipientId.toInt(), asHtml = asHtml)
            val response = feedService.saveMessageActivity(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedListEntity()
                val feed = result.toFeedRecord(revision = revision)
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.FeedUpserted(
                            feed = feed,
                        ),
                    )
                }
                feed
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun saveActivityReplyRecord(
        id: Long? = null,
        activityId: Long,
        text: String?,
        asHtml: Boolean = false,
        commitToStore: Boolean = true,
        revision: Long = 0L,
    ): Result<FeedReplyRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = SaveActivityReply.request(id = id?.toInt(), activityId = activityId.toInt(), text = text, asHtml = asHtml)
            val response = feedService.saveActivityReply(request)
            if (response.isSuccessful) {
                val result = handleGraphQLResponse(response.body() ?: throw IllegalStateException("Empty response body")).toFeedReplyEntity()
                val reply = result.toFeedReplyRecord(activityId = activityId, revision = revision)
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.ReplyUpserted(
                            feedId = activityId,
                            reply = reply,
                        ),
                    )
                }
                reply
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}

// Generated response mapping to the legacy entity surface (Phase 2).
//
// Maps operation data at the repository boundary back into the entity shapes
// the repository has always exposed, preserving page-info, reply, like, media,
// and null handling of the previous AniListContainer decode path.

internal fun FeedListData.toFeedPageContainer(): PageContainer<FeedListEntity> = page?.toFeedPage() ?: throw IllegalStateException("Empty response body")

internal fun FeedMessageData.toFeedPageContainer(): PageContainer<FeedListEntity> = page?.toFeedPage() ?: throw IllegalStateException("Empty response body")

internal fun FeedListData.Page.toFeedPage(): PageContainer<FeedListEntity> = PageContainer<FeedListEntity>().also { container ->
    container.pageData = activities.orEmpty().mapNotNull { activity -> activity?.toFeedListEntity() }
    pageInfo?.let { container.pageInfo = it.toPageInfo() }
}

internal fun FeedMessageData.Page.toFeedPage(): PageContainer<FeedListEntity> = PageContainer<FeedListEntity>().also { container ->
    container.pageData = activities.orEmpty().mapNotNull { activity -> activity?.toFeedListEntity() }
    pageInfo?.let { container.pageInfo = it.toPageInfo() }
}

internal fun FeedListReplyData.toFeedDetailEntity(): FeedListEntity = activity?.toFeedListEntity() ?: throw IllegalStateException("Empty response body")

internal fun SaveTextActivityData.toFeedListEntity(): FeedListEntity = saveTextActivity?.toFeedListEntity() ?: throw IllegalStateException("Empty response body")

internal fun SaveMessageActivityData.toFeedListEntity(): FeedListEntity = saveMessageActivity?.toFeedListEntity() ?: throw IllegalStateException("Empty response body")

internal fun SaveActivityReplyData.toFeedReplyEntity(): FeedReply = saveActivityReply?.toFeedReplyEntity() ?: throw IllegalStateException("Empty response body")

internal fun DeleteActivityData.toDeleteState(): DeleteState = deleteActivity?.let { DeleteState(isDeleted = it.deleted ?: false) } ?: throw IllegalStateException("Empty response body")

internal fun DeleteActivityReplyData.toDeleteState(): DeleteState = deleteActivityReply?.let { DeleteState(isDeleted = it.deleted ?: false) } ?: throw IllegalStateException("Empty response body")

internal fun FeedListData.PageActivities.toFeedListEntity(): FeedListEntity = when (this) {
    is FeedListData.PageActivities.ListActivity -> FeedListEntity(
        id = id.toLong(),
        replyCount = replyCount,
        type = type?.name,
        status = status,
        text = progress,
        createdAt = createdAt.toLong(),
        user = user?.toUserBase(),
        media = media?.toMediaEntity(),
        likes = likes?.mapNotNull { like -> like?.toUserBase() },
        siteUrl = siteUrl,
    ).also { entity ->
        entity.replies = replies?.mapNotNull { reply -> reply?.toFeedReplyEntity() }
    }
    is FeedListData.PageActivities.TextActivity -> FeedListEntity(
        id = id.toLong(),
        replyCount = replyCount,
        type = type?.name,
        text = text,
        createdAt = createdAt.toLong(),
        user = user?.toUserBase(),
        likes = likes?.mapNotNull { like -> like?.toUserBase() },
        siteUrl = siteUrl,
    ).also { entity ->
        entity.replies = replies?.mapNotNull { reply -> reply?.toFeedReplyEntity() }
    }
    is FeedListData.PageActivities.MessageActivity -> FeedListEntity()
}

internal fun FeedListReplyData.Activity.toFeedListEntity(): FeedListEntity = when (this) {
    is FeedListReplyData.Activity.ListActivity -> FeedListEntity(
        id = id.toLong(),
        replyCount = replyCount,
        type = type?.name,
        status = status,
        text = progress,
        createdAt = createdAt.toLong(),
        user = user?.toUserBase(),
        media = media?.toMediaEntity(),
        likes = likes?.mapNotNull { like -> like?.toUserBase() },
        siteUrl = siteUrl,
    ).also { entity ->
        entity.replies = replies?.mapNotNull { reply -> reply?.toFeedReplyEntity() }
    }
    is FeedListReplyData.Activity.MessageActivity -> FeedListEntity(
        id = id.toLong(),
        replyCount = replyCount,
        type = type?.name,
        text = message,
        createdAt = createdAt.toLong(),
        messenger = messenger?.toUserBase(),
        recipient = recipient?.toUserBase(),
        likes = likes?.mapNotNull { like -> like?.toUserBase() },
        siteUrl = siteUrl,
    ).also { entity ->
        entity.replies = replies?.mapNotNull { reply -> reply?.toFeedReplyEntity() }
    }
    is FeedListReplyData.Activity.TextActivity -> FeedListEntity(
        id = id.toLong(),
        replyCount = replyCount,
        type = type?.name,
        text = text,
        createdAt = createdAt.toLong(),
        user = user?.toUserBase(),
        likes = likes?.mapNotNull { like -> like?.toUserBase() },
        siteUrl = siteUrl,
    ).also { entity ->
        entity.replies = replies?.mapNotNull { reply -> reply?.toFeedReplyEntity() }
    }
}

internal fun FeedMessageData.PageActivities.toFeedListEntity(): FeedListEntity = when (this) {
    is FeedMessageData.PageActivities.MessageActivity -> FeedListEntity(
        id = id.toLong(),
        replyCount = replyCount,
        type = type?.name,
        text = message,
        createdAt = createdAt.toLong(),
        messenger = messenger?.toUserBase(),
        recipient = recipient?.toUserBase(),
        likes = likes?.mapNotNull { like -> like?.toUserBase() },
        siteUrl = siteUrl,
    ).also { entity ->
        entity.replies = replies?.mapNotNull { reply -> reply?.toFeedReplyEntity() }
    }
    is FeedMessageData.PageActivities.ListActivity -> FeedListEntity()
    is FeedMessageData.PageActivities.TextActivity -> FeedListEntity()
}

internal fun SaveTextActivityData.SaveTextActivity.toFeedListEntity(): FeedListEntity = FeedListEntity(
    id = id.toLong(),
    replyCount = replyCount,
    type = type?.name,
    text = text,
    createdAt = createdAt.toLong(),
)

internal fun SaveMessageActivityData.SaveMessageActivity.toFeedListEntity(): FeedListEntity = FeedListEntity(
    id = id.toLong(),
    replyCount = replyCount,
    type = type?.name,
    text = message,
    createdAt = createdAt.toLong(),
)

internal fun SaveActivityReplyData.SaveActivityReply.toFeedReplyEntity(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
)

internal fun FeedListData.ListActivityPageActivitiesReplies.toFeedReplyEntity(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
)

internal fun FeedListData.TextActivityPageActivitiesReplies.toFeedReplyEntity(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
)

internal fun FeedListReplyData.ListActivityActivityReplies.toFeedReplyEntity(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
)

internal fun FeedListReplyData.MessageActivityActivityReplies.toFeedReplyEntity(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
)

internal fun FeedListReplyData.TextActivityActivityReplies.toFeedReplyEntity(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
)

internal fun FeedMessageData.MessageActivityPageActivitiesReplies.toFeedReplyEntity(): FeedReply = FeedReply(
    id = id.toLong(),
    text = text,
    createdAt = createdAt.toLong(),
    user = user?.toUserBase(),
    likes = likes?.mapNotNull { like -> like?.toUserBase() },
)

private fun FeedListData.ListActivityPageActivitiesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListData.ListActivityPageActivitiesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListData.ListActivityPageActivitiesRepliesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListData.ListActivityPageActivitiesRepliesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListData.TextActivityPageActivitiesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListData.TextActivityPageActivitiesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListData.TextActivityPageActivitiesRepliesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListData.TextActivityPageActivitiesRepliesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.ListActivityActivityUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.ListActivityActivityLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.ListActivityActivityRepliesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.ListActivityActivityRepliesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.MessageActivityActivityMessenger.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.MessageActivityActivityRecipient.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.MessageActivityActivityLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.MessageActivityActivityRepliesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.MessageActivityActivityRepliesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.TextActivityActivityUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.TextActivityActivityLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.TextActivityActivityRepliesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedListReplyData.TextActivityActivityRepliesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedMessageData.MessageActivityPageActivitiesMessenger.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedMessageData.MessageActivityPageActivitiesRecipient.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedMessageData.MessageActivityPageActivitiesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedMessageData.MessageActivityPageActivitiesRepliesUser.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun FeedMessageData.MessageActivityPageActivitiesRepliesLikes.toUserBase(): UserBase = toUserBase(id, name, avatar?.large, avatar?.medium, bannerImage, isFollowing)

private fun toUserBase(
    id: Int,
    name: String,
    avatarLarge: String?,
    avatarMedium: String?,
    bannerImage: String?,
    isFollowing: Boolean?,
): UserBase = UserBase(name = name).also { user ->
    user.id = id.toLong()
    user.avatar = ImageBase(extraLarge = null, large = avatarLarge, medium = avatarMedium)
    user.isFollowing = isFollowing ?: false
    user.bannerImage = bannerImage
}

internal fun FeedListData.ListActivityPageActivitiesMedia.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
    entity.id = id.toLong()
    entity.title = title?.toMediaTitle()
    entity.coverImage = coverImage?.toImageBase()
    entity.bannerImage = bannerImage
    entity.type = type?.name
    entity.format = format?.name
    entity.season = season?.name
    entity.status = status?.name
    entity.siteUrl = siteUrl
    entity.meanScore = meanScore ?: 0
    entity.averageScore = averageScore ?: 0
    entity.startDate = startDate?.toFuzzyDate()
    entity.endDate = endDate?.toFuzzyDate()
    entity.episodes = episodes ?: 0
    entity.chapters = chapters ?: 0
    entity.volumes = volumes ?: 0
    entity.isAdult = isAdult ?: false
    entity.isFavourite = isFavourite
    entity.nextAiringEpisode = nextAiringEpisode?.toAiringSchedule()
    entity.mediaListEntry = mediaListEntry?.toMediaList()
}

internal fun FeedListReplyData.ListActivityActivityMedia.toMediaEntity(): MediaEntity = MediaEntity().also { entity ->
    entity.id = id.toLong()
    entity.title = title?.toMediaTitle()
    entity.coverImage = coverImage?.toImageBase()
    entity.bannerImage = bannerImage
    entity.type = type?.name
    entity.format = format?.name
    entity.season = season?.name
    entity.status = status?.name
    entity.siteUrl = siteUrl
    entity.meanScore = meanScore ?: 0
    entity.averageScore = averageScore ?: 0
    entity.startDate = startDate?.toFuzzyDate()
    entity.endDate = endDate?.toFuzzyDate()
    entity.episodes = episodes ?: 0
    entity.chapters = chapters ?: 0
    entity.volumes = volumes ?: 0
    entity.isAdult = isAdult ?: false
    entity.isFavourite = isFavourite
    entity.nextAiringEpisode = nextAiringEpisode?.toAiringSchedule()
    entity.mediaListEntry = mediaListEntry?.toMediaList()
}

private fun FeedListData.ListActivityPageActivitiesMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun FeedListReplyData.ListActivityActivityMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
    romajiRaw = romaji,
    englishRaw = english,
    originalRaw = native,
    userPreferredRaw = userPreferred,
)

private fun FeedListData.ListActivityPageActivitiesMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun FeedListReplyData.ListActivityActivityMediaCoverImage.toImageBase(): ImageBase = ImageBase(
    extraLarge = extraLarge,
    large = large,
    medium = medium,
)

private fun FeedListData.ListActivityPageActivitiesMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun FeedListData.ListActivityPageActivitiesMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun FeedListReplyData.ListActivityActivityMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun FeedListReplyData.ListActivityActivityMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
    day = day ?: 0,
    month = month ?: 0,
    year = year ?: 0,
)

private fun FeedListData.ListActivityPageActivitiesMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun FeedListReplyData.ListActivityActivityMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
    airingAt = airingAt.toLong(),
    timeUntilAiring = timeUntilAiring.toLong(),
    episode = episode,
)

private fun FeedListData.ListActivityPageActivitiesMediaMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun FeedListReplyData.ListActivityActivityMediaMediaListEntry.toMediaList(): MediaList = MediaList().also { mediaList ->
    mediaList.id = id.toLong()
    mediaList.status = status?.name
}

private fun FeedListData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}

private fun FeedMessageData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
    total = total ?: 0,
    perPage = perPage ?: 0,
    currentPage = currentPage ?: 0,
).also { pageInfo ->
    pageInfo.setHasNextPage(hasNextPage ?: false)
}
