package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort

/**
 * Identity of a review query as executed against the AniList backend.
 *
 * [sort] is normalized at construction: a null sort means the server default
 * ([ReviewSort.CREATED_AT_DESC]), so equivalent server queries share one key.
 * Filter membership is [mediaId] and [mediaType] only; [sort] never decides
 * which reviews belong to a query.
 */
data class ReviewQueryKey private constructor(
    val mediaId: Long?,
    val mediaType: MediaType?,
    val sort: ReviewSort,
) {
    companion object {
        operator fun invoke(
            mediaId: Long?,
            mediaType: MediaType?,
            sort: ReviewSort?,
        ): ReviewQueryKey = ReviewQueryKey(
            mediaId = mediaId,
            mediaType = mediaType,
            sort = sort ?: ReviewSort.CREATED_AT_DESC,
        )
    }
}
