package com.mxt.anitrend.domain.mediadetail.model

import com.mxt.anitrend.domain.model.AiringScheduleRecord
import com.mxt.anitrend.domain.model.FuzzyDateRecord

/**
 * Immutable canonical representation of the media overview query
 * (`MediaOverview.graphql`) in the media detail pipeline.
 *
 * Covers the exact field set requested by `MediaOverview.graphql`,
 * `MediaCoreFragment`, `MediaTagFragment`, and `StudioFragment`. Pure Kotlin value
 * type, intentionally not Parcelable and not ObjectBox-backed. Mapped from the
 * generated GraphQL `MediaOverviewData.Media` type by
 * `com.mxt.anitrend.data.mapper.toMediaOverviewRecord`. Generated Int ids and
 * timestamps are converted to domain Longs and the generated enums (type, format,
 * season, status, source, mediaListEntry status) are exposed as their serialized
 * `name`, matching the legacy String-backed entity lane.
 *
 * Existing canonical records are reused where their semantics match:
 * [FuzzyDateRecord] for the start/end dates and [AiringScheduleRecord] for the
 * next-airing projection (the schedule id/mediaId are redundant within the parent
 * media context, matching [MediaSummaryRecord] usage). [MediaListEntryRecord] is
 * the minimal identity/status projection from the `MediaListFragmentMini` fragment.
 * Feature-local records ([MediaOverviewCoverImageRecord], [MediaOverviewTrailerRecord],
 * [MediaOverviewTagRecord], [MediaOverviewStudioRecord]) carry the projections that the
 * shared summary/statistics records ([MediaSummaryRecord], [StudioRecord],
 * [MediaTagRecord]) do not represent with the exact requested field set.
 *
 * The legacy mutable [com.mxt.anitrend.model.entity.anilist.Media] remains
 * unchanged for its remaining consumers.
 */
data class MediaOverviewRecord(
    val id: Long,
    val titleUserPreferred: String?,
    val titleRomaji: String?,
    val titleEnglish: String?,
    val titleOriginal: String?,
    val bannerImage: String?,
    val coverImage: MediaOverviewCoverImageRecord?,
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
    val genres: List<String?>?,
    val tags: List<MediaOverviewTagRecord>?,
    val trailer: MediaOverviewTrailerRecord?,
    val duration: Int?,
    val hashtag: String?,
    val source: String?,
    val studios: List<MediaOverviewStudioRecord>?,
    val description: String?,
)

/**
 * Full cover-image projection as requested by the `MediaImageFragment` fragment.
 * Distinct from the single-URL [com.mxt.anitrend.domain.model.MediaSummaryRecord.coverImage],
 * which collapses the image variants for summary lanes.
 */
data class MediaOverviewCoverImageRecord(
    val color: String?,
    val extraLarge: String?,
    val large: String?,
    val medium: String?,
)

/**
 * Trailer projection requested by `MediaOverview.graphql` (`id`, `site`,
 * `thumbnail`). The trailer `id` is a String in the generated transport, so no
 * Int-to-Long conversion applies here.
 */
data class MediaOverviewTrailerRecord(
    val id: String?,
    val site: String?,
    val thumbnail: String?,
)

/**
 * Media-tag projection as requested by the `MediaTagFragment` fragment within the
 * media overview query. Distinct from
 * [com.mxt.anitrend.domain.user.model.MediaTagRecord], which carries the
 * statistics/UI-only `isMediaSpoiler` and `isSelected` state of the user
 * statistics lane.
 */
data class MediaOverviewTagRecord(
    val id: Long,
    val name: String,
    val description: String?,
    val category: String?,
    val rank: Int,
    val isGeneralSpoiler: Boolean,
    val isAdult: Boolean,
)

/**
 * Studio projection as requested by the `StudioFragment` fragment within the media
 * overview query. Distinct from [com.mxt.anitrend.domain.model.StudioRecord], which
 * does not carry the `isAnimationStudio` flag requested by the fragment.
 */
data class MediaOverviewStudioRecord(
    val id: Long,
    val name: String,
    val isAnimationStudio: Boolean,
    val siteUrl: String?,
    val isFavourite: Boolean,
)
