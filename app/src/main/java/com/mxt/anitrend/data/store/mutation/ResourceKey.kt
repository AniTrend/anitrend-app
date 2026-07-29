package com.mxt.anitrend.data.store.mutation

sealed interface ResourceKey {
    data class Feed(val feedId: Long) : ResourceKey

    data class Reply(val replyId: Long) : ResourceKey

    data class MediaListByMedia(val mediaId: Long) : ResourceKey

    data class MediaListById(val entryId: Long) : ResourceKey

    data class Review(val reviewId: Long) : ResourceKey
}
