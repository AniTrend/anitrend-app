package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.model.entity.anilist.Review

/**
 * Maps the legacy mutable [Review] entity to the immutable [ReviewRecord]
 * consumed by the review store. No legacy entity is changed by this lane;
 * repository mapping to [ReviewRecord] happens in the review migration phase
 * that follows the store contract change.
 *
 * Null/default behavior is explicit:
 * - The legacy entity always materializes non-null `user` and `media` defaults
 *   (`UserBase()` / `MediaBase()`). An empty default instance carries no business
 *   identity, so it is mapped to a null nested summary: `user` is null when both
 *   `id == 0L` and `name == null`, and `media` is null when `id == 0L`.
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
    user = if (user.id == 0L && user.name == null) null else user.toUserSummaryRecord(),
    media = if (media.id == 0L) null else media.toMediaSummaryRecord(),
    revision = revision,
)
