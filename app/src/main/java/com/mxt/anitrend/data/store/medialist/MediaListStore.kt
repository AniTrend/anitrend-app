package com.mxt.anitrend.data.store.medialist

import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaListStore {
    val state: StateFlow<MediaListStoreState>

    suspend fun apply(change: MediaListStoreChange)

    fun observeEntryByMediaId(mediaId: Long): Flow<MediaListRecord?>

    fun observeQuery(key: MediaListQueryKey): Flow<MediaListQueryResult>
}
