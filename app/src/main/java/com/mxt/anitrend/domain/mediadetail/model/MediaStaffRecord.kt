package com.mxt.anitrend.domain.mediadetail.model

import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.domain.model.StaffRecord

/**
 * Immutable canonical representation of the media staff query
 * (`MediaStaff.graphql`) in the media detail pipeline.
 *
 * Page-level result of a media staff request. Preserves the server-returned edge
 * ordering ([edges]) together with the paging metadata ([pageInfo]) needed to
 * render the staff lane. Pure Kotlin value type, intentionally not Parcelable
 * and not ObjectBox-backed. Mapped from the generated GraphQL
 * `MediaStaffData.Media` type by
 * `com.mxt.anitrend.data.mapper.toMediaStaffRecord`.
 *
 * The nullable `staff` and `pageInfo` blocks and the nullable `edges` list from
 * the generated transport are preserved: a null block yields a null list or null
 * page metadata, keeping the nullable semantics of the generated shape. Null
 * list elements within the edges list are dropped by the mapper, following the
 * established node-list mapping convention.
 *
 * The legacy mutable
 * [com.mxt.anitrend.model.entity.container.body.ConnectionContainer] /
 * [com.mxt.anitrend.model.entity.container.body.EdgeContainer] /
 * [com.mxt.anitrend.model.entity.anilist.edge.StaffEdge] lane remains unchanged
 * for its remaining consumers.
 */
data class MediaStaffRecord(
    val edges: List<MediaStaffEdgeRecord>?,
    val pageInfo: PageInfoRecord?,
)

/**
 * Staff-edge projection as requested by `MediaStaff.graphql` (`role`, `node`).
 * The generated role is transported as its serialized String and passes through
 * unchanged via [role], matching the legacy String-backed `StaffEdge.role` lane.
 * The staff [node] is a nullable projection reusing the existing immutable
 * [com.mxt.anitrend.domain.model.StaffRecord]: a null node block from the
 * generated transport is preserved as null.
 */
data class MediaStaffEdgeRecord(
    val role: String?,
    val node: StaffRecord?,
)
