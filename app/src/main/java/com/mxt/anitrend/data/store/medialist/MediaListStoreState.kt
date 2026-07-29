package com.mxt.anitrend.data.store.medialist

import com.mxt.anitrend.domain.medialist.model.MediaListRecord

data class MediaListStoreState(
    val entriesById: Map<Long, MediaListRecord> = emptyMap(),
    val entryIdByMediaId: Map<Long, Long> = emptyMap(),
    val queries: Map<MediaListQueryKey, MediaListQuerySnapshot> = emptyMap(),
)
