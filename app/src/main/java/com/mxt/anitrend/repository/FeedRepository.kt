package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.DeleteActivity
import com.mxt.anitrend.graphql.generated.DeleteActivityReply
import com.mxt.anitrend.graphql.generated.FeedList
import com.mxt.anitrend.graphql.generated.FeedListReply
import com.mxt.anitrend.graphql.generated.FeedMessage
import com.mxt.anitrend.graphql.generated.SaveActivityReply
import com.mxt.anitrend.graphql.generated.SaveMessageActivity
import com.mxt.anitrend.graphql.generated.SaveTextActivity
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
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body")).also { result ->
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
