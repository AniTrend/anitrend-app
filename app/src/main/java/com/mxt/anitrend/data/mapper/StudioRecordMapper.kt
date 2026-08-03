package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.StudioRecord
import com.mxt.anitrend.graphql.generated.StudioBaseData

/**
 * Maps the generated [StudioBaseData.Studio] GraphQL type to the immutable
 * [StudioRecord] consumed by the studio detail pipeline.
 *
 * Preserves the current generated-to-domain values and converts the generated
 * `id` Int to the domain Long. The legacy mutable
 * [com.mxt.anitrend.model.entity.base.StudioBase] lane
 * ([com.mxt.anitrend.repository.mapper.toStudioEntity]) is unchanged for the
 * non-detail StudioBase consumers.
 */
fun StudioBaseData.Studio.toStudioRecord(): StudioRecord = StudioRecord(
    id = id.toLong(),
    name = name,
    siteUrl = siteUrl,
    isFavourite = isFavourite,
)
