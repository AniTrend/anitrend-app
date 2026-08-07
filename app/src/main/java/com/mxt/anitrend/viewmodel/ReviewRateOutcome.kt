package com.mxt.anitrend.viewmodel

import com.mxt.anitrend.data.store.mutation.MutationResult

/**
 * UI-effect result of a review rating mutation.
 *
 * The canonical committed state stays exclusively in
 * [com.mxt.anitrend.data.store.review.ReviewStore]; this outcome only lets the
 * screen converge widget loading/error/success behavior (reset the vote control,
 * surface a failure message) without optimistic store mutation.
 */
data class ReviewRateOutcome(
    val reviewId: Long,
    val result: MutationResult,
)
