package com.mxt.anitrend.domain.mediadetail.model

import com.mxt.anitrend.domain.model.CharacterRecord
import com.mxt.anitrend.domain.model.PageInfoRecord

/**
 * Immutable canonical representation of the media characters query
 * (`MediaCharacters.graphql`) in the media detail pipeline.
 *
 * Page-level result of a media characters request. Preserves the server-returned
 * edge ordering ([edges]) together with the paging metadata ([pageInfo]) needed
 * to render the characters lane. Pure Kotlin value type, intentionally not
 * Parcelable and not ObjectBox-backed. Mapped from the generated GraphQL
 * `MediaCharactersData.Media` type by
 * `com.mxt.anitrend.data.mapper.toMediaCharactersRecord`.
 *
 * The nullable `characters` and `pageInfo` blocks and the nullable `edges` list
 * from the generated transport are preserved: a null block yields a null list or
 * null page metadata, keeping the nullable semantics of the generated shape. Null
 * list elements within the edges list are dropped by the mapper, following the
 * established node-list mapping convention.
 *
 * The legacy mutable
 * [com.mxt.anitrend.model.entity.container.body.ConnectionContainer] /
 * [com.mxt.anitrend.model.entity.container.body.EdgeContainer] /
 * [com.mxt.anitrend.model.entity.anilist.edge.CharacterEdge] lane remains unchanged
 * for its remaining consumers.
 */
data class MediaCharactersRecord(
    val edges: List<MediaCharactersEdgeRecord>?,
    val pageInfo: PageInfoRecord?,
)

/**
 * Character-edge projection as requested by `MediaCharacters.graphql` (`role`,
 * `node`). The generated [com.mxt.anitrend.graphql.generated.CharacterRole] enum
 * is exposed as its serialized `name` via [role], matching the legacy
 * String-backed `CharacterEdge.role` lane. The character [node] is a nullable
 * projection reusing the existing immutable
 * [com.mxt.anitrend.domain.model.CharacterRecord]: a null node block from the
 * generated transport is preserved as null.
 */
data class MediaCharactersEdgeRecord(
    val role: String?,
    val node: CharacterRecord?,
)
