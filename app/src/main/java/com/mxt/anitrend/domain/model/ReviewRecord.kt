package com.mxt.anitrend.domain.model

/**
 * Immutable canonical representation of a media review in the review store.
 *
 * Pure Kotlin value type, intentionally not Parcelable and not ObjectBox-backed.
 * Mapped from the legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.Review] by
 * [com.mxt.anitrend.data.mapper.toReviewRecord]. Reuses the existing
 * [UserSummaryRecord] and [MediaSummaryRecord] domain values. The legacy entity
 * remains for the adapters, widgets, fragments, and navigation consumers until
 * they are migrated in later phases.
 *
 * [revision] carries the store mutation revision at commit time so the committed
 * record is self-contained, matching the [FeedRecord] and [MediaListRecord]
 * convention. The review store wrapper continues to track the authoritative
 * mutation revision for stale-response rejection.
 */
data class ReviewRecord(
    val id: Long,
    val summary: String?,
    val mediaType: String?,
    val body: String?,
    val rating: Int,
    val ratingAmount: Int,
    val userRating: String?,
    val score: Int,
    val isPrivate: Boolean,
    val createdAt: Long,
    val user: UserSummaryRecord?,
    val media: MediaSummaryRecord?,
    val revision: Long,
)
