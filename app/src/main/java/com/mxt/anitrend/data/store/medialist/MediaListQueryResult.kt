package com.mxt.anitrend.data.store.medialist

import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.PageInfoRecord

data class MediaListQueryResult(
    val entries: List<MediaListRecord>,
    val pageInfo: PageInfoRecord?,
)
