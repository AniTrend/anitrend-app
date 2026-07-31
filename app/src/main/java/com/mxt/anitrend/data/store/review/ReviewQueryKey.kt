package com.mxt.anitrend.data.store.review

import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewSort

data class ReviewQueryKey(
    val mediaId: Long?,
    val mediaType: MediaType?,
    val sort: ReviewSort?,
)
