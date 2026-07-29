package com.mxt.anitrend.data.store.medialist

import com.mxt.anitrend.graphql.generated.MediaListSort
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.graphql.generated.MediaType

data class MediaListQueryKey(
    val userId: Long?,
    val userName: String?,
    val mediaType: MediaType?,
    val statuses: Set<MediaListStatus>,
    val sort: MediaListSort?,
)
