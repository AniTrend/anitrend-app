package com.mxt.anitrend.data.store.mutation

sealed interface OperationKey {
    val resourceKey: ResourceKey
    val type: Type

    enum class Type {
        FEED_LIKE,
        FEED_SAVE,
        FEED_DELETE,
        REPLY_LIKE,
        REPLY_SAVE,
        REPLY_DELETE,
        MEDIA_LIST_SAVE,
        MEDIA_LIST_DELETE,
        MEDIA_LIST_INCREMENT_PROGRESS,
        REVIEW_SAVE,
        REVIEW_DELETE,
        REVIEW_RATE,
        USER_FOLLOW,
        FAVOURITE_ANIME,
        FAVOURITE_MANGA,
        FAVOURITE_CHARACTER,
        FAVOURITE_STAFF,
        FAVOURITE_STUDIO,
    }

    data class Key(
        override val resourceKey: ResourceKey,
        override val type: Type,
    ) : OperationKey

    companion object {
        fun of(resourceKey: ResourceKey, type: Type): OperationKey = Key(
            resourceKey = resourceKey,
            type = type,
        )

        fun feedLike(feedId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Feed(feedId),
            type = Type.FEED_LIKE,
        )

        fun feedSave(feedId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Feed(feedId),
            type = Type.FEED_SAVE,
        )

        fun feedDelete(feedId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Feed(feedId),
            type = Type.FEED_DELETE,
        )

        fun replyLike(replyId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Reply(replyId),
            type = Type.REPLY_LIKE,
        )

        fun replySave(replyId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Reply(replyId),
            type = Type.REPLY_SAVE,
        )

        fun replyDelete(replyId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Reply(replyId),
            type = Type.REPLY_DELETE,
        )

        fun mediaListSave(mediaId: Long): OperationKey = Key(
            resourceKey = ResourceKey.MediaListByMedia(mediaId),
            type = Type.MEDIA_LIST_SAVE,
        )

        fun mediaListSaveById(entryId: Long): OperationKey = Key(
            resourceKey = ResourceKey.MediaListById(entryId),
            type = Type.MEDIA_LIST_SAVE,
        )

        fun mediaListDelete(entryId: Long): OperationKey = Key(
            resourceKey = ResourceKey.MediaListById(entryId),
            type = Type.MEDIA_LIST_DELETE,
        )

        fun mediaListDeleteByMedia(mediaId: Long): OperationKey = Key(
            resourceKey = ResourceKey.MediaListByMedia(mediaId),
            type = Type.MEDIA_LIST_DELETE,
        )

        fun mediaListIncrementProgress(mediaId: Long): OperationKey = Key(
            resourceKey = ResourceKey.MediaListByMedia(mediaId),
            type = Type.MEDIA_LIST_INCREMENT_PROGRESS,
        )

        fun reviewSave(reviewId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Review(reviewId),
            type = Type.REVIEW_SAVE,
        )

        fun reviewDelete(reviewId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Review(reviewId),
            type = Type.REVIEW_DELETE,
        )

        fun reviewRate(reviewId: Long): OperationKey = Key(
            resourceKey = ResourceKey.Review(reviewId),
            type = Type.REVIEW_RATE,
        )

        fun userFollow(userId: Long): OperationKey = Key(
            resourceKey = ResourceKey.User(userId),
            type = Type.USER_FOLLOW,
        )

        fun favouriteAnime(mediaId: Long): OperationKey = Key(
            resourceKey = ResourceKey.FavouriteAnime(mediaId),
            type = Type.FAVOURITE_ANIME,
        )

        fun favouriteManga(mediaId: Long): OperationKey = Key(
            resourceKey = ResourceKey.FavouriteManga(mediaId),
            type = Type.FAVOURITE_MANGA,
        )

        fun favouriteCharacter(characterId: Long): OperationKey = Key(
            resourceKey = ResourceKey.FavouriteCharacter(characterId),
            type = Type.FAVOURITE_CHARACTER,
        )

        fun favouriteStaff(staffId: Long): OperationKey = Key(
            resourceKey = ResourceKey.FavouriteStaff(staffId),
            type = Type.FAVOURITE_STAFF,
        )

        fun favouriteStudio(studioId: Long): OperationKey = Key(
            resourceKey = ResourceKey.FavouriteStudio(studioId),
            type = Type.FAVOURITE_STUDIO,
        )
    }
}
