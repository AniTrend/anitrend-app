package com.mxt.anitrend.domain.mediadetail.model

/**
 * Immutable canonical representation of the primary media detail query
 * (`MediaBase.graphql`) in the media detail pipeline.
 *
 * Pure Kotlin value type, intentionally not Parcelable and not ObjectBox-backed.
 * Mapped from the generated GraphQL `MediaBaseData.Media` type by
 * `com.mxt.anitrend.data.mapper.toMediaDetailRecord`. Generated Int ids are
 * converted to domain Longs and the generated [type] and
 * [MediaListEntryRecord.status] enums are exposed as their serialized `name`,
 * matching the legacy String-backed entity lane.
 *
 * [mediaListEntry] is the minimal identity/status projection from the
 * `MediaListFragmentMini` fragment; it is intentionally not the canonical
 * [com.mxt.anitrend.domain.medialist.model.MediaListRecord]. The legacy mutable
 * [com.mxt.anitrend.model.entity.base.MediaBase] remains unchanged for its
 * remaining consumers.
 */
data class MediaDetailRecord(
    val id: Long,
    val idMal: Long?,
    val titleUserPreferred: String?,
    val type: String?,
    val bannerImage: String?,
    val isFavourite: Boolean,
    val siteUrl: String?,
    val mediaListEntry: MediaListEntryRecord?,
)

/**
 * Minimal identity/status projection of a media-list entry as requested by the
 * `MediaListFragmentMini` fragment within the `MediaBase` query.
 *
 * Distinct from the canonical [com.mxt.anitrend.domain.medialist.model.MediaListRecord],
 * which carries the full server entry state.
 */
data class MediaListEntryRecord(
    val id: Long,
    val status: String?,
)
