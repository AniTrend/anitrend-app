package com.mxt.anitrend.domain.user.model

/**
 * Immutable canonical representation of a media tag reference inside a user
 * statistics entry, mirroring the legacy `MediaTag` fields that are relevant to
 * statistics (the generated transport omits the UI-only selection state, which
 * the generated mapper defaults to false).
 *
 * Pure Kotlin value type with no Android or ObjectBox dependencies.
 */
data class MediaTagRecord(
    val id: Long,
    val name: String?,
    val description: String?,
    val category: String?,
    val rank: Int,
    val isGeneralSpoiler: Boolean,
    val isMediaSpoiler: Boolean,
    val isAdult: Boolean,
    val isSelected: Boolean,
)
