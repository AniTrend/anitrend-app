package com.mxt.anitrend.data.store.medialist

import com.mxt.anitrend.domain.model.PageInfoRecord

data class MediaListQuerySnapshot(
    val orderedEntryIds: List<Long>,
    val pageInfo: PageInfoRecord?,
    val loadedPages: Set<Int>,
    val lastUpdatedAtMillis: Long,
)
