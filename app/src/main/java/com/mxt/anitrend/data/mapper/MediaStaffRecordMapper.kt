package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.mediadetail.model.MediaStaffEdgeRecord
import com.mxt.anitrend.domain.mediadetail.model.MediaStaffRecord
import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.StaffRecord
import com.mxt.anitrend.graphql.generated.MediaStaffData

/**
 * Maps the generated `MediaStaffData.Media` GraphQL types to the immutable
 * [MediaStaffRecord] and its edge projection consumed by the media staff
 * pipeline.
 *
 * Covers the exact field set requested by `MediaStaff.graphql`,
 * `PageInfoFragment`, and `StaffCoreFragment`. Converts the generated Int staff
 * `id` to a domain Long. The generated edge role is already transported as its
 * serialized String and passes through unchanged via
 * [MediaStaffEdgeRecord.role], matching the legacy String-backed
 * `StaffEdge.role` lane. Nullable semantics of the optional staff/pageInfo
 * blocks, the nullable edges list, and the nullable node blocks are preserved;
 * null list elements within the edges list are dropped via `mapNotNull`,
 * following the established node-list mapping convention.
 *
 * The `StaffCoreFragment` node projection is mapped to the existing
 * [StaffRecord] with the exact name/site/favourite semantics of
 * [StaffRecordMapper]: [StaffRecord.name] is derived from first and last exactly
 * like the legacy `TitleBase.fullName`, `siteUrl` and `isFavourite` pass through
 * unchanged, and the `image` block is not carried by the canonical record (it is
 * intentionally omitted the same way the existing `StaffBase` mapping omits
 * it). The legacy mutable
 * [com.mxt.anitrend.model.entity.anilist.edge.StaffEdge] lane is unchanged for
 * its remaining consumers.
 */
fun MediaStaffData.Media.toMediaStaffRecord(): MediaStaffRecord = MediaStaffRecord(
    edges = staff?.edges?.mapNotNull { it?.toMediaStaffEdgeRecord() },
    pageInfo = staff?.pageInfo?.toPageInfoRecord(),
)

private fun MediaStaffData.MediaStaffEdges.toMediaStaffEdgeRecord(): MediaStaffEdgeRecord =
    MediaStaffEdgeRecord(
        role = role,
        node = node?.toStaffRecord(),
    )

private fun MediaStaffData.MediaStaffEdgesNode.toStaffRecord(): StaffRecord = StaffRecord(
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
private fun MediaStaffData.MediaStaffEdgesNodeName.toFullName(): String? {
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

private fun MediaStaffData.MediaStaffPageInfo.toPageInfoRecord(): PageInfoRecord = PageInfoRecord(
    currentPage = currentPage,
    lastPage = lastPage,
    perPage = perPage,
    total = total,
    hasNextPage = hasNextPage ?: false,
    hasPreviousPage = (currentPage ?: 0) > 1,
)
