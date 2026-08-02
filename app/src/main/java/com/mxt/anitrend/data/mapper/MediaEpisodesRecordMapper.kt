package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.mediadetail.model.MediaEpisodesExternalLinkRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaEpisodesRecord
import com.mxt.anitrend.graphql.generated.MediaEpisodesData

/**
 * Maps the generated `MediaEpisodesData.Media` GraphQL types to the immutable
 * [MediaEpisodesRecord] and its feature-local projections consumed by the media
 * episodes pipeline.
 *
 * Covers the exact field set requested by `MediaEpisodes.graphql` (external
 * links `id`, `url`, `site`). Converts generated Int ids to domain Longs.
 * Nullable semantics of the optional external-links block are preserved; null
 * list elements within the external-link nodes are dropped via `mapNotNull`,
 * following the established node-list mapping convention. The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.ExternalLink] lane is unchanged for
 * its remaining consumers.
 */
fun MediaEpisodesData.Media.toMediaEpisodesRecord(): MediaEpisodesRecord = MediaEpisodesRecord(
    externalLinks = externalLinks?.mapNotNull { it?.toMediaEpisodesExternalLinkRecord() },
)

private fun MediaEpisodesData.MediaExternalLinks.toMediaEpisodesExternalLinkRecord(): MediaEpisodesExternalLinkRecord =
    MediaEpisodesExternalLinkRecord(
        id = id.toLong(),
        url = url,
        site = site,
    )
