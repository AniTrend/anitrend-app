package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.UserBase

/**
 * Maps the legacy mutable [Review] entity to the immutable [ReviewRecord]
 * consumed by the review store. No legacy entity is changed by this lane;
 * repository mapping to [ReviewRecord] happens in the review migration phase
 * that follows the store contract change.
 *
 * Null/default behavior is explicit:
 * - The legacy entity normally materializes non-null `user` and `media` defaults
 *   (`UserBase()` / `MediaBase()`). An empty default instance carries no business
 *   identity, so it is mapped to a null nested summary: `user` is null when both
 *   `id == 0L` and `name == null`, and `media` is null when `id == 0L`.
 * - Gson can also write a runtime null into those declared non-null properties
 *   when the response contains `"user": null` / `"media": null` (reflection
 *   bypasses the Kotlin contract, seen on RateReview responses). A runtime null
 *   carries no business identity either, so it maps to a null nested summary.
 * - Scalar fields are copied verbatim, preserving legacy defaults (e.g. `0` for
 *   `rating`, `ratingAmount`, and `score`).
 */
fun Review.toReviewRecord(revision: Long = 0L): ReviewRecord = ReviewRecord(
    id = id,
    summary = summary,
    mediaType = mediaType,
    body = body,
    rating = rating,
    ratingAmount = ratingAmount,
    userRating = userRating,
    score = score,
    isPrivate = isPrivate,
    createdAt = createdAt,
    user = user.toUserSummaryRecordOrNull(),
    media = media.toMediaSummaryRecordOrNull(),
    revision = revision,
)

private fun UserBase?.toUserSummaryRecordOrNull(): UserSummaryRecord? =
    if (this == null || (id == 0L && name == null)) null else toUserSummaryRecord()

private fun MediaBase?.toMediaSummaryRecordOrNull(): MediaSummaryRecord? =
    if (this == null || id == 0L) null else toMediaSummaryRecord()
