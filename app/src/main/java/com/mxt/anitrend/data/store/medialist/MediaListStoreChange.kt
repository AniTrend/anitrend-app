package com.mxt.anitrend.data.store.medialist

import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.PageInfoRecord

sealed interface MediaListStoreChange {
    data class CollectionLoaded(
        val queryKey: MediaListQueryKey,
        val entries: List<MediaListRecord>,
        val pageInfo: PageInfoRecord?,
    ) : MediaListStoreChange

    data class EntryUpserted(
        val entry: MediaListRecord,
    ) : MediaListStoreChange

    data class EntryDeleted(
        val entryId: Long,
        val mediaId: Long?,
        val revision: Long,
    ) : MediaListStoreChange
}
