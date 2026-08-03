package com.mxt.anitrend.domain.model

/**
 * Immutable canonical representation of a studio in the detail pipeline.
 *
 * Pure Kotlin value type, intentionally not Parcelable and not ObjectBox-backed.
 * Mapped from the generated GraphQL `StudioBaseData.Studio` type by
 * `com.mxt.anitrend.data.mapper.toStudioRecord`. The legacy mutable
 * `com.mxt.anitrend.model.entity.base.StudioBase` remains for the non-detail
 * StudioBase consumers (search, favourites, media overview, statistics).
 */
data class StudioRecord(
    val id: Long,
    val name: String,
    val siteUrl: String?,
    val isFavourite: Boolean,
)
