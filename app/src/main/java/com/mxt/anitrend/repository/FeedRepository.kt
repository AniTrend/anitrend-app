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
import com.mxt.anitrend.data.mapper.toFeedReplyRecord
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.feed.FeedStoreChange
import com.mxt.anitrend.model.api.retro.anilist.FeedService
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.anilist.FeedList as FeedListEntity

sealed class FeedMutation {
    data class FeedSaved(val feed: FeedListEntity) : FeedMutation()
    data class FeedDeleted(val id: Long) : FeedMutation()
    data class ReplySaved(
        val reply: FeedReply,
        val activityId: Long,
    ) : FeedMutation()
    data class ReplyDeleted(val id: Long) : FeedMutation()
}

class FeedRepository(
    private val feedService: FeedService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val feedStore: FeedStore? = null,
) : AbstractRepository<FeedMutation>(ioDispatcher) {

    suspend fun getFeedList(
        page: Int? = null,
        perPage: Int? = null,
        id: Long? = null,
        isFollowing: Boolean? = null,
        userId: Long? = null,
        type: ActivityType? = null,
        isMixed: Boolean? = null,
        asHtml: Boolean = false,
    ): Result<PageContainer<FeedListEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedList.request(page = page, perPage = perPage, id = id?.toInt(), isFollowing = isFollowing, userId = userId?.toInt(), type = type, isMixed = isMixed, asHtml = asHtml)
            val response = feedService.getFeedList(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFeedListReply(id: Long, asHtml: Boolean = false): Result<FeedListEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedListReply.request(id = id.toInt(), asHtml = asHtml)
            val response = feedService.getFeedListReply(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
    ): Result<PageContainer<FeedListEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = FeedMessage.request(page = page, perPage = perPage, messengerId = messengerId?.toInt(), userId = userId?.toInt(), asHtml = asHtml)
            val response = feedService.getFeedMessage(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
            val response = feedService.saveTextActivity(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.FeedUpserted(
                            feed = result.toFeedRecord(revision = revision),
                        ),
                    )
                }
                _mutationEvents.emit(FeedMutation.FeedSaved(result))
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
            val response = feedService.saveMessageActivity(request).execute()
            if (response.isSuccessful) {
                val result = handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
                if (commitToStore) {
                    feedStore?.apply(
                        FeedStoreChange.FeedUpserted(
                            feed = result.toFeedRecord(revision = revision),
                        ),
                    )
                }
                _mutationEvents.emit(FeedMutation.FeedSaved(result))
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
            val response = feedService.saveActivityReply(request).execute()
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
                _mutationEvents.emit(
                    FeedMutation.ReplySaved(
                        reply = result,
                        activityId = activityId,
                    ),
                )
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
            val response = feedService.deleteActivity(request).execute()
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
                _mutationEvents.emit(FeedMutation.FeedDeleted(id))
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
            val response = feedService.deleteActivityReply(request).execute()
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
                _mutationEvents.emit(FeedMutation.ReplyDeleted(id))
                result
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
