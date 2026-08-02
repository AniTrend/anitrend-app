package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.mediadetail.model.MediaDetailRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaListEntryRecord
import com.mxt.anitrend.graphql.generated.MediaBaseData

/**
 * Maps the generated `MediaBaseData.Media` GraphQL types to the immutable
 * [MediaDetailRecord] and [MediaListEntryRecord] consumed by the media detail
 * pipeline.
 *
 * Converts generated Int ids to domain Longs and preserves the media id, MAL id,
 * preferred title, type, banner image, favourite flag, site URL, and the minimal
 * `mediaListEntry` identity/status projection. Generated enums (type, status) are
 * exposed as their serialized `name`, matching the legacy String-backed entity
 * lane. Nullable semantics of the generated blocks are preserved. The legacy
 * mutable [com.mxt.anitrend.model.entity.base.MediaBase] lane is unchanged for
 * its remaining consumers.
 */
fun MediaBaseData.Media.toMediaDetailRecord(): MediaDetailRecord = MediaDetailRecord(
    id = id.toLong(),
    idMal = idMal?.toLong(),
    titleUserPreferred = title?.userPreferred,
    type = type?.name,
    bannerImage = bannerImage,
    isFavourite = isFavourite,
    siteUrl = siteUrl,
    mediaListEntry = mediaListEntry?.toMediaListEntryRecord(),
)

fun MediaBaseData.MediaMediaListEntry.toMediaListEntryRecord(): MediaListEntryRecord = MediaListEntryRecord(
    id = id.toLong(),
    status = status?.name,
)
