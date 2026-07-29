package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.feed.FeedStoreState
import com.mxt.anitrend.data.store.medialist.MediaListStoreState
import com.mxt.anitrend.graphql.generated.MediaListStatus

object StoreInvariantValidator {
    fun validateFeedState(state: FeedStoreState) {
        state.queries.forEach { (queryKey, snapshot) ->
            check(snapshot.orderedFeedIds.size == snapshot.orderedFeedIds.distinct().size) {
                "Feed store query contains duplicate feed ids for $queryKey"
            }
            snapshot.orderedFeedIds.forEach { feedId ->
                check(state.feedsById.containsKey(feedId)) {
                    "Feed store query references missing feed id=$feedId for $queryKey"
                }
            }
        }

        state.replyIdsByFeedId.forEach { (feedId, replyIds) ->
            check(replyIds.size == replyIds.distinct().size) {
                "Feed store reply mapping contains duplicate reply ids for feedId=$feedId"
            }
            replyIds.forEach { replyId ->
                check(state.repliesById.containsKey(replyId)) {
                    "Feed store reply mapping references missing reply id=$replyId for feedId=$feedId"
                }
            }
            state.feedsById[feedId]?.let { parentFeed ->
                check(parentFeed.replyCount >= replyIds.size) {
                    "Feed store reply count is smaller than known replies for feedId=$feedId"
                }
            }
        }
    }

    fun validateMediaListState(state: MediaListStoreState) {
        state.entryIdByMediaId.forEach { (mediaId, entryId) ->
            check(state.entriesById.containsKey(entryId)) {
                "Media list store media mapping references missing entryId=$entryId for mediaId=$mediaId"
            }
        }

        state.queries.forEach { (queryKey, snapshot) ->
            check(snapshot.orderedEntryIds.size == snapshot.orderedEntryIds.distinct().size) {
                "Media list store query contains duplicate entry ids for $queryKey"
            }
            snapshot.orderedEntryIds.forEach { entryId ->
                val entry = state.entriesById[entryId]
                check(entry != null) {
                    "Media list store query references missing entry id=$entryId for $queryKey"
                }
                check(queryKey.mediaType == null || entry.media?.type == queryKey.mediaType.name) {
                    "Media list store query contains mismatched media type entry id=$entryId for $queryKey"
                }
                if (queryKey.statuses.isNotEmpty()) {
                    val status = entry.status?.let { runCatching { MediaListStatus.valueOf(it) }.getOrNull() }
                    check(status in queryKey.statuses) {
                        "Media list store query contains mismatched status entry id=$entryId for $queryKey"
                    }
                }
            }
        }
    }
}
