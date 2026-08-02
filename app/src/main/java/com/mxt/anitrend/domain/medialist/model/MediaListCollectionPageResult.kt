package com.mxt.anitrend.domain.medialist.model

import com.mxt.anitrend.domain.model.PageInfoRecord

/**
 * Immutable result of a media list collection fetch, flattened across all returned
 * lists.
 *
 * Entries use the canonical [MediaListRecord] lane consumed by the media list store.
 * [pageInfo] mirrors the store's collection-loaded shape and is always null for the
 * collection operation (the query has no paging wrapper); it is kept for symmetry
 * with the store's collection-loaded change.
 */
data class MediaListCollectionPageResult(
    val entries: List<MediaListRecord>,
    val pageInfo: PageInfoRecord? = null,
)
