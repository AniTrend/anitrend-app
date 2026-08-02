package com.mxt.anitrend.domain.model

/**
 * Immutable canonical representation of a character in the detail pipeline.
 *
 * Pure Kotlin value type, intentionally not Parcelable and not ObjectBox-backed.
 * Mapped from the generated GraphQL `CharacterBaseData.Character` type by
 * `com.mxt.anitrend.data.mapper.toCharacterRecord`. `name` is the character full
 * name derived from first and last name, preserving the legacy
 * `com.mxt.anitrend.model.entity.anilist.meta.TitleBase.fullName` display
 * semantics. The legacy mutable
 * `com.mxt.anitrend.model.entity.base.CharacterBase` remains for the non-detail
 * CharacterBase consumers (search, favourites, overview, group pages).
 */
data class CharacterRecord(
    val id: Long,
    val name: String?,
    val siteUrl: String?,
    val isFavourite: Boolean,
)
