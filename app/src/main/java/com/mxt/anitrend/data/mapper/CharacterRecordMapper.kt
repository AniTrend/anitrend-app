package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.graphql.generated.CharacterBaseData

/**
 * Maps the generated [CharacterBaseData.Character] GraphQL type to the immutable
 * [CharacterRecord] consumed by the character detail pipeline.
 *
 * Preserves the current generated-to-domain values, derives [CharacterRecord.name]
 * from the first and last name exactly like the legacy `TitleBase.fullName`
 * (see [com.mxt.anitrend.model.entity.anilist.meta.TitleBase]), and converts the
 * generated `id` Int to the domain Long. The legacy mutable
 * [com.mxt.anitrend.model.entity.base.CharacterBase] lane
 * ([com.mxt.anitrend.repository.mapper.toCharacterEntity]) is unchanged for the
 * non-detail CharacterBase consumers.
 */
fun CharacterBaseData.Character.toCharacterRecord(): CharacterRecord = CharacterRecord(
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
private fun CharacterBaseData.CharacterName.toFullName(): String? {
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
