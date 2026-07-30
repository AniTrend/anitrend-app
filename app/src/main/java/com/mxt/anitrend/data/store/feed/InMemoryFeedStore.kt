package com.mxt.anitrend.data.store.feed

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.data.store.StoreInvariantValidator
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryFeedStore : FeedStore {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(FeedStoreState())
    private val feedDeletionRevisions = mutableMapOf<Long, Long>()
    private val replyDeletionRevisions = mutableMapOf<Long, Long>()

    override val state: StateFlow<FeedStoreState> = mutableState.asStateFlow()

    override suspend fun apply(change: FeedStoreChange) {
        mutex.withLock {
            val updatedState =
                when (change) {
                    is FeedStoreChange.PageLoaded -> reducePageLoaded(change)
                    is FeedStoreChange.FeedDetailLoaded -> reduceFeedDetailLoaded(change)
                    is FeedStoreChange.FeedUpserted -> reduceFeedUpserted(change.feed)
                    is FeedStoreChange.FeedDeleted -> reduceFeedDeleted(change.feedId, change.revision)
                    is FeedStoreChange.ReplyUpserted -> reduceReplyUpserted(change.feedId, change.reply)
                    is FeedStoreChange.ReplyDeleted -> reduceReplyDeleted(change.feedId, change.replyId, change.revision)
                    is FeedStoreChange.FeedLikesReplaced -> reduceFeedLikesReplaced(change)
                    is FeedStoreChange.ReplyLikesReplaced -> reduceReplyLikesReplaced(change)
                }

            if (BuildConfig.DEBUG) {
                StoreInvariantValidator.validateFeedState(updatedState)
            }

            mutableState.value = updatedState
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            feedDeletionRevisions.clear()
            replyDeletionRevisions.clear()
            mutableState.value = FeedStoreState()
        }
    }

    override fun observeFeed(feedId: Long): Flow<FeedRecord?> = state.map { it.feedsById[feedId] }.distinctUntilChanged()

    override fun observeReplies(feedId: Long): Flow<List<FeedReplyRecord>> = state.map { currentState ->
        currentState.replyIdsByFeedId[feedId]
            .orEmpty()
            .mapNotNull(currentState.repliesById::get)
    }.distinctUntilChanged()

    override fun observeQuery(key: FeedQueryKey): Flow<FeedQueryResult> = state.map { currentState ->
        val snapshot = currentState.queries[key]
        FeedQueryResult(
            feeds = snapshot?.orderedFeedIds.orEmpty().mapNotNull(currentState.feedsById::get),
            pageInfo = snapshot?.pageInfo,
            loadedPages = snapshot?.loadedPages.orEmpty(),
        )
    }.distinctUntilChanged()

    private fun reducePageLoaded(change: FeedStoreChange.PageLoaded): FeedStoreState {
        val currentState = mutableState.value
        val existingSnapshot = currentState.queries[change.queryKey]
        if (existingSnapshot != null && change.generation < existingSnapshot.generation) {
            return currentState
        }

        val feedsById = currentState.feedsById.toMutableMap()
        val acceptedIds = mutableListOf<Long>()

        change.feeds.forEach { feed ->
            val currentRevision = maxOf(
                feedsById[feed.id]?.revision ?: Long.MIN_VALUE,
                feedDeletionRevisions[feed.id] ?: Long.MIN_VALUE,
            )
            if (feed.revision >= currentRevision) {
                feedDeletionRevisions.remove(feed.id)
                feedsById[feed.id] = feed
            }
            if (feedsById.containsKey(feed.id)) {
                acceptedIds += feed.id
            }
        }

        val orderedFeedIds =
            if (change.page <= 1) {
                acceptedIds.distinct()
            } else {
                (existingSnapshot?.orderedFeedIds.orEmpty() + acceptedIds).distinct()
            }
        val loadedPages =
            if (change.page <= 1) {
                setOf(change.page)
            } else {
                existingSnapshot?.loadedPages.orEmpty() + change.page
            }
        val queries = currentState.queries.toMutableMap().apply {
            put(
                change.queryKey,
                FeedQuerySnapshot(
                    orderedFeedIds = orderedFeedIds,
                    pageInfo = change.pageInfo,
                    loadedPages = loadedPages,
                    generation = change.generation,
                    lastUpdatedAtMillis = System.currentTimeMillis(),
                ),
            )
        }

        return currentState.copy(
            feedsById = feedsById,
            queries = queries,
        )
    }

    private fun reduceFeedDetailLoaded(change: FeedStoreChange.FeedDetailLoaded): FeedStoreState {
        val currentState = mutableState.value
        val currentRevision = maxOf(
            currentState.feedsById[change.feed.id]?.revision ?: Long.MIN_VALUE,
            feedDeletionRevisions[change.feed.id] ?: Long.MIN_VALUE,
        )
        if (change.feed.revision < currentRevision) {
            return currentState
        }

        feedDeletionRevisions.remove(change.feed.id)

        val feedsById = currentState.feedsById.toMutableMap().apply {
            put(change.feed.id, change.feed.copy(replyCount = change.feed.replyCount))
        }

        val previousReplyIds = currentState.replyIdsByFeedId[change.feed.id].orEmpty()
        val repliesById = currentState.repliesById.toMutableMap().apply {
            previousReplyIds.forEach(::remove)
        }
        val replyIds = mutableListOf<Long>()
        change.replies.forEach { reply ->
            val replyCurrentRevision = maxOf(
                currentState.repliesById[reply.id]?.revision ?: Long.MIN_VALUE,
                replyDeletionRevisions[reply.id] ?: Long.MIN_VALUE,
            )
            if (reply.revision >= replyCurrentRevision) {
                replyDeletionRevisions.remove(reply.id)
                repliesById[reply.id] = reply.copy(activityId = change.feed.id)
                replyIds += reply.id
            }
        }

        val replyIdsByFeedId = currentState.replyIdsByFeedId.toMutableMap().apply {
            if (replyIds.isEmpty()) {
                remove(change.feed.id)
            } else {
                put(change.feed.id, replyIds.distinct())
            }
        }

        return currentState.copy(
            feedsById = feedsById,
            repliesById = repliesById,
            replyIdsByFeedId = replyIdsByFeedId,
        )
    }

    private fun reduceFeedUpserted(feed: FeedRecord): FeedStoreState {
        val currentState = mutableState.value
        val currentRevision = maxOf(
            currentState.feedsById[feed.id]?.revision ?: Long.MIN_VALUE,
            feedDeletionRevisions[feed.id] ?: Long.MIN_VALUE,
        )
        if (feed.revision < currentRevision) {
            return currentState
        }

        feedDeletionRevisions.remove(feed.id)
        val feedsById = currentState.feedsById.toMutableMap().apply {
            put(feed.id, feed)
        }
        return currentState.copy(feedsById = feedsById)
    }

    private fun reduceFeedDeleted(
        feedId: Long,
        revision: Long,
    ): FeedStoreState {
        val currentState = mutableState.value
        val currentRevision = maxOf(
            currentState.feedsById[feedId]?.revision ?: Long.MIN_VALUE,
            feedDeletionRevisions[feedId] ?: Long.MIN_VALUE,
        )
        if (revision < currentRevision) {
            return currentState
        }

        val replyIdsForFeed = currentState.replyIdsByFeedId[feedId].orEmpty()
        val relatedReplyIds =
            (replyIdsForFeed + currentState.repliesById.values.filter { it.activityId == feedId }.map { it.id })
                .distinct()

        feedDeletionRevisions[feedId] = revision
        relatedReplyIds.forEach { replyId ->
            replyDeletionRevisions[replyId] = revision
        }

        val feedsById = currentState.feedsById.toMutableMap().apply {
            remove(feedId)
        }
        val repliesById = currentState.repliesById.toMutableMap().apply {
            relatedReplyIds.forEach(::remove)
        }
        val replyIdsByFeedId = currentState.replyIdsByFeedId.toMutableMap().apply {
            remove(feedId)
        }
        val queries = currentState.queries.mapValues { (_, snapshot) ->
            snapshot.copy(
                orderedFeedIds = snapshot.orderedFeedIds.filterNot { it == feedId },
            )
        }

        return currentState.copy(
            feedsById = feedsById,
            repliesById = repliesById,
            replyIdsByFeedId = replyIdsByFeedId,
            queries = queries,
        )
    }

    private fun reduceReplyUpserted(
        feedId: Long,
        reply: FeedReplyRecord,
    ): FeedStoreState {
        val currentState = mutableState.value
        val parentFeed = currentState.feedsById[feedId] ?: return currentState
        val currentRevision = maxOf(
            currentState.repliesById[reply.id]?.revision ?: Long.MIN_VALUE,
            replyDeletionRevisions[reply.id] ?: Long.MIN_VALUE,
        )
        if (reply.revision < currentRevision) {
            return currentState
        }

        replyDeletionRevisions.remove(reply.id)
        val repliesById = currentState.repliesById.toMutableMap().apply {
            put(reply.id, reply.copy(activityId = feedId))
        }
        val previousReplyIds = currentState.replyIdsByFeedId[feedId].orEmpty()
        val replyAlreadyPresent = previousReplyIds.contains(reply.id)
        val updatedReplyIds =
            if (replyAlreadyPresent) previousReplyIds.toList() else previousReplyIds + reply.id
        val replyIdsByFeedId = currentState.replyIdsByFeedId.toMutableMap().apply {
            put(feedId, updatedReplyIds)
        }
        val updatedReplyCount =
            if (replyAlreadyPresent) {
                maxOf(parentFeed.replyCount, updatedReplyIds.size)
            } else {
                maxOf(parentFeed.replyCount + 1, updatedReplyIds.size)
            }
        val feedsById = currentState.feedsById.toMutableMap().apply {
            put(feedId, parentFeed.copy(replyCount = updatedReplyCount))
        }

        return currentState.copy(
            feedsById = feedsById,
            repliesById = repliesById,
            replyIdsByFeedId = replyIdsByFeedId,
        )
    }

    private fun reduceReplyDeleted(
        feedId: Long,
        replyId: Long,
        revision: Long,
    ): FeedStoreState {
        val currentState = mutableState.value
        val currentRevision = maxOf(
            currentState.repliesById[replyId]?.revision ?: Long.MIN_VALUE,
            replyDeletionRevisions[replyId] ?: Long.MIN_VALUE,
        )
        if (revision < currentRevision) {
            return currentState
        }

        replyDeletionRevisions[replyId] = revision

        val repliesById = currentState.repliesById.toMutableMap().apply {
            remove(replyId)
        }
        val previousReplyIds = currentState.replyIdsByFeedId[feedId].orEmpty()
        val replyWasPresent = previousReplyIds.contains(replyId)
        val updatedReplyIds = previousReplyIds.filterNot { it == replyId }
        val replyIdsByFeedId = currentState.replyIdsByFeedId.toMutableMap().apply {
            if (updatedReplyIds.isEmpty()) {
                remove(feedId)
            } else {
                put(feedId, updatedReplyIds)
            }
        }
        val feedsById = currentState.feedsById.toMutableMap().apply {
            currentState.feedsById[feedId]?.let { parentFeed ->
                val updatedReplyCount =
                    if (replyWasPresent) {
                        maxOf(updatedReplyIds.size, parentFeed.replyCount - 1, 0)
                    } else {
                        maxOf(updatedReplyIds.size, parentFeed.replyCount)
                    }
                put(feedId, parentFeed.copy(replyCount = updatedReplyCount))
            }
        }

        return currentState.copy(
            feedsById = feedsById,
            repliesById = repliesById,
            replyIdsByFeedId = replyIdsByFeedId,
        )
    }

    private fun reduceFeedLikesReplaced(change: FeedStoreChange.FeedLikesReplaced): FeedStoreState {
        val currentState = mutableState.value
        val currentFeed = currentState.feedsById[change.feedId] ?: return currentState
        val currentRevision = maxOf(
            currentFeed.revision,
            feedDeletionRevisions[change.feedId] ?: Long.MIN_VALUE,
        )
        if (change.revision < currentRevision) {
            return currentState
        }

        val feedsById = currentState.feedsById.toMutableMap().apply {
            put(
                change.feedId,
                currentFeed.copy(
                    likes = change.likes.toList(),
                    revision = change.revision,
                ),
            )
        }
        return currentState.copy(feedsById = feedsById)
    }

    private fun reduceReplyLikesReplaced(change: FeedStoreChange.ReplyLikesReplaced): FeedStoreState {
        val currentState = mutableState.value
        val currentReply = currentState.repliesById[change.replyId] ?: return currentState
        val currentRevision = maxOf(
            currentReply.revision,
            replyDeletionRevisions[change.replyId] ?: Long.MIN_VALUE,
        )
        if (change.revision < currentRevision) {
            return currentState
        }

        val repliesById = currentState.repliesById.toMutableMap().apply {
            put(
                change.replyId,
                currentReply.copy(
                    activityId = change.feedId,
                    likes = change.likes.toList(),
                    revision = change.revision,
                ),
            )
        }
        return currentState.copy(repliesById = repliesById)
    }
}
