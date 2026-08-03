package com.mxt.anitrend.domain.model

/**
 * Immutable canonical representation of a staff member in the detail pipeline.
 *
 * Pure Kotlin value type, intentionally not Parcelable and not ObjectBox-backed.
 * Mapped from the generated GraphQL `StaffBaseData.Staff` type by
 * `com.mxt.anitrend.data.mapper.toStaffRecord`. `name` is the staff full name
 * derived from first and last name, preserving the legacy
 * `com.mxt.anitrend.model.entity.anilist.meta.TitleBase.fullName` display
 * semantics. The legacy mutable
 * `com.mxt.anitrend.model.entity.base.StaffBase` remains for the non-detail
 * StaffBase consumers (search, favourites, overview, media, roles).
 */
data class StaffRecord(
    val id: Long,
    val name: String?,
    val siteUrl: String?,
    val isFavourite: Boolean,
)
