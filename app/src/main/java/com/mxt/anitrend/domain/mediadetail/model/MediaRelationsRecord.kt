package com.mxt.anitrend.domain.mediadetail.model

import com.mxt.anitrend.domain.model.AiringScheduleRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord
import com.mxt.anitrend.domain.model.PageInfoRecord

/**
 * Immutable canonical representation of the media relations query
 * (`MediaRelations.graphql`) in the media detail pipeline.
 *
 * Page-level result of a media relations request. Preserves the server-returned
 * edge ordering ([edges]) together with the paging metadata ([pageInfo]) needed
 * to render the relations lane. Pure Kotlin value type, intentionally not
 * Parcelable and not ObjectBox-backed. Mapped from the generated GraphQL
 * `MediaRelationsData.Media` type by
 * `com.mxt.anitrend.data.mapper.toMediaRelationsRecord`.
 *
 * The nullable `relations` and `pageInfo` blocks and the nullable `edges` list
 * from the generated transport are preserved: a null block yields a null list or
 * null page metadata, keeping the nullable semantics of the generated shape. Null
 * list elements within the edges list are dropped by the mapper, following the
 * established node-list mapping convention.
 *
 * The legacy mutable
 * [com.mxt.anitrend.model.entity.container.body.ConnectionContainer] /
 * [com.mxt.anitrend.model.entity.container.body.EdgeContainer] /
 * [com.mxt.anitrend.model.entity.anilist.edge.MediaEdge] lane remains unchanged
 * for its remaining consumers.
 */
data class MediaRelationsRecord(
    val edges: List<MediaRelationsEdgeRecord>?,
    val pageInfo: PageInfoRecord?,
)

/**
 * Relation-edge projection as requested by `MediaRelations.graphql`
 * (`relationType(version: 2)`, `node`). The generated [MediaRelation] enum is
 * exposed as its serialized `name` via [relationType], matching the legacy
 * String-backed entity lane. The media [node] is a nullable projection: a null
 * node block from the generated transport is preserved as null.
 */
data class MediaRelationsEdgeRecord(
    val relationType: String?,
    val node: MediaRelationsNodeRecord?,
)

/**
 * Media-node projection as requested by the `MediaCoreFragment` fragment within
 * the media relations query. Carries the exact field set of `MediaCoreFragment`:
 * title variants, banner/cover images, media enums (type, format, season,
 * status), scores, dates, episode/chapter/volume counts, adult/favourite flags,
 * airing schedule, media-list entry projection, site URL, and update timestamp.
 *
 * Generated Int ids and timestamps are converted to domain Longs and the
 * generated enums (type, format, season, status, mediaListEntry status) are
 * exposed as their serialized `name`. Existing canonical records are reused where
 * their field set matches the requested fragments: [FuzzyDateRecord] for the
 * start/end dates, [AiringScheduleRecord] for the next-airing projection (the
 * schedule id/mediaId are redundant within the parent media context, matching
 * [com.mxt.anitrend.domain.mediadetail.model.MediaOverviewRecord] usage), and
 * [MediaListEntryRecord] for the minimal `MediaListFragmentMini` projection. The
 * cover-image projection is feature-local ([MediaRelationsCoverImageRecord])
 * because no shared record carries the exact `MediaImageFragment` field set.
 */
data class MediaRelationsNodeRecord(
    val id: Long,
    val titleUserPreferred: String?,
    val titleRomaji: String?,
    val titleEnglish: String?,
    val titleOriginal: String?,
    val bannerImage: String?,
    val coverImage: MediaRelationsCoverImageRecord?,
    val type: String?,
    val format: String?,
    val season: String?,
    val status: String?,
    val meanScore: Int?,
    val averageScore: Int?,
    val startDate: FuzzyDateRecord?,
    val endDate: FuzzyDateRecord?,
    val episodes: Int?,
    val chapters: Int?,
    val volumes: Int?,
    val isAdult: Boolean?,
    val isFavourite: Boolean,
    val nextAiringEpisode: AiringScheduleRecord?,
    val mediaListEntry: MediaListEntryRecord?,
    val siteUrl: String?,
    val updatedAt: Long?,
)

/**
 * Cover-image projection as requested by the `MediaImageFragment` fragment within
 * the media relations node.
 */
data class MediaRelationsCoverImageRecord(
    val color: String?,
    val extraLarge: String?,
    val large: String?,
    val medium: String?,
)
