package com.mxt.anitrend.domain.mediadetail.model

/**
 * Immutable canonical representation of the media episodes query
 * (`MediaEpisodes.graphql`) in the media detail pipeline.
 *
 * Covers the exact field set requested by `MediaEpisodes.graphql`: the nullable
 * list of external links (`id`, `url`, `site`). Pure Kotlin value type,
 * intentionally not Parcelable and not ObjectBox-backed. Mapped from the
 * generated GraphQL `MediaEpisodesData.Media` type by
 * `com.mxt.anitrend.data.mapper.toMediaEpisodesRecord`. Generated Int ids are
 * converted to domain Longs.
 *
 * The nullable `externalLinks` block from the generated transport is preserved
 * as the nullable [externalLinks] list: a null block yields a null list,
 * preserving the nullable semantics of the generated shape. Null list elements
 * are dropped by the mapper, following the established node-list mapping
 * convention. The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.ExternalLink] lane remains unchanged
 * for its remaining consumers.
 */
data class MediaEpisodesRecord(
    val externalLinks: List<MediaEpisodesExternalLinkRecord>?,
)

/**
 * External-link projection as requested by `MediaEpisodes.graphql` (`id`, `url`,
 * `site`).
 */
data class MediaEpisodesExternalLinkRecord(
    val id: Long,
    val url: String?,
    val site: String,
)
