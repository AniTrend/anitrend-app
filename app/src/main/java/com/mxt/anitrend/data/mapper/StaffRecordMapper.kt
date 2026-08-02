package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.graphql.generated.StaffBaseData

/**
 * Maps the generated [StaffBaseData.Staff] GraphQL type to the immutable
 * [StaffRecord] consumed by the staff detail pipeline.
 *
 * Preserves the current generated-to-domain values, derives [StaffRecord.name]
 * from the first and last name exactly like the legacy `TitleBase.fullName`
 * (see [com.mxt.anitrend.model.entity.anilist.meta.TitleBase]), and converts the
 * generated `id` Int to the domain Long. The legacy mutable
 * [com.mxt.anitrend.model.entity.base.StaffBase] lane is unchanged for the
 * non-detail StaffBase consumers.
 */
fun StaffBaseData.Staff.toStaffRecord(): StaffRecord = StaffRecord(
    id = id.toLong(),
    name = name?.toFullName(),
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)

/**
 * Mirrors the legacy `TitleBase.fullName` semantics: first and last joined with a
 * single space when both are present, falling back to whichever of first/last is
 * present, and null when both are missing.
 */
private fun StaffBaseData.StaffName.toFullName(): String? {
    var fullName = first
    if (!last.isNullOrEmpty()) {
        fullName = if (!fullName.isNullOrEmpty()) {
            "$fullName $last"
        } else {
            last
        }
    }
    return fullName
}
