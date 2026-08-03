package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.mediadetail.model.MediaCharactersEdgeRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaCharactersRecord
import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.graphql.generated.MediaCharactersData

/**
 * Maps the generated `MediaCharactersData.Media` GraphQL types to the immutable
 * [MediaCharactersRecord] and its edge projection consumed by the media
 * characters pipeline.
 *
 * Covers the exact field set requested by `MediaCharacters.graphql`,
 * `PageInfoFragment`, and `CharacterCoreFragment`. Converts the generated Int
 * character `id` to a domain Long and exposes the generated
 * `com.mxt.anitrend.graphql.generated.CharacterRole` enum as its serialized
 * `name` via the edge [MediaCharactersEdgeRecord.role], matching the legacy
 * String-backed `CharacterEdge.role` lane. Nullable semantics of the optional
 * characters/pageInfo blocks, the nullable edges list, and the nullable node
 * blocks are preserved; null list elements within the edges list are dropped via
 * `mapNotNull`, following the established node-list mapping convention.
 *
 * The `CharacterCoreFragment` node projection is mapped to the existing
 * [CharacterRecord] with the exact name/image/site/favourite semantics of
 * [CharacterRecordMapper]: [CharacterRecord.name] is derived from first and last
 * exactly like the legacy `TitleBase.fullName`, `siteUrl` and `isFavourite` pass
 * through unchanged, and the `image` block is not carried by the canonical
 * record (it is dropped the same way the existing `CharacterBase` mapping drops
 * it). The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge] lane is unchanged
 * for its remaining consumers.
 */
fun MediaCharactersData.Media.toMediaCharactersRecord(): MediaCharactersRecord = MediaCharactersRecord(
    edges = characters?.edges?.mapNotNull { it?.toMediaCharactersEdgeRecord() },
    pageInfo = characters?.pageInfo?.toPageInfoRecord(),
)

private fun MediaCharactersData.MediaCharactersEdges.toMediaCharactersEdgeRecord(): MediaCharactersEdgeRecord = MediaCharactersEdgeRecord(
    role = role?.name,
    node = node?.toCharacterRecord(),
)

private fun MediaCharactersData.MediaCharactersEdgesNode.toCharacterRecord(): CharacterRecord = CharacterRecord(
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
private fun MediaCharactersData.MediaCharactersEdgesNodeName.toFullName(): String? {
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

private fun MediaCharactersData.MediaCharactersPageInfo.toPageInfoRecord(): PageInfoRecord = PageInfoRecord(
    currentPage = currentPage,
    lastPage = lastPage,
    perPage = perPage,
    total = total,
    hasNextPage = hasNextPage ?: false,
    hasPreviousPage = (currentPage ?: 0) > 1,
)
