package com.mxt.anitrend.domain.review.interactor

import com.mxt.anitrend.data.mapper.toReviewRecord
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.review.ReviewStore
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.repository.BrowseRepository

class RateReviewInteractor(
    private val browseRepository: BrowseRepository,
    private val mutationExecutor: MutationExecutor,
    private val reviewStore: ReviewStore,
    private val requestSequence: RequestSequence,
) {
    suspend operator fun invoke(reviewId: Long, rating: ReviewRating?): MutationResult = executeMutation(
        mutationExecutor = mutationExecutor,
        requestSequence = requestSequence,
        resourceKey = ResourceKey.Review(reviewId),
        operationKey = OperationKey.reviewRate(reviewId),
        failureMessage = "Unable to rate review",
    ) { revision, context ->
        browseRepository.rateReview(
            id = reviewId,
            rating = rating,
            commitToStore = false,
            revision = revision,
        ).fold(
            onSuccess = { review ->
                context.ensureSessionActive()
                val mapped = review.toReviewRecord(revision = revision)
                val existing = reviewStore.state.value.reviewsById[reviewId]?.review
                reviewStore.apply(
                    ReviewStoreChange.ReviewRated(
                        review = convergeRatingState(
                            existing = existing,
                            response = mapped,
                            revision = revision,
                        ),
                        revision = revision,
                    ),
                )
                MutationResult.Success
            },
            onFailure = { throwable ->
                MutationResult.Failure(
                    message = throwable.message ?: "Unable to rate review",
                    cause = throwable,
                )
            },
        )
    }
}

/**
 * Rating-only convergence for successful RateReview responses.
 *
 * AniList RateReview responses are partial: the live response can carry
 * `user: null` and `media: null` (and no body/summary/mediaType), which Gson
 * writes into the legacy [com.mxt.anitrend.model.entity.anilist.Review]
 * regardless of declared nullability. Wholesale-committing that partial record
 * would erase the cached author/media/body/summary display metadata of the
 * review already committed in the store. Response fields such as mediaType,
 * summary, or body must never be used to decide whether the response is full:
 * the live response can carry a non-null mediaType while user/media stay null.
 *
 * For every existing committed record, only the mutation-authoritative rating
 * state ([ReviewRecord.rating], [ReviewRecord.ratingAmount],
 * [ReviewRecord.userRating]) is patched in. All display metadata - nested user
 * and media summaries, body, summary, mediaType, and the non-rating scalars
 * (score, isPrivate, createdAt), which a rate mutation cannot change - is
 * unconditionally retained from the existing record.
 * [ReviewRecord.revision] is refreshed to the current store revision so the
 * committed record stays self-contained, matching the store wrapper revision.
 *
 * Only when no existing record exists is the mapped response committed as-is.
 *
 * The store remains the sole state owner: this only reads the committed state
 * to derive the next change, which is applied through [ReviewStore.apply].
 */
private fun convergeRatingState(
    existing: ReviewRecord?,
    response: ReviewRecord,
    revision: Long,
): ReviewRecord {
    if (existing == null) {
        return response
    }

    return existing.copy(
        rating = response.rating,
        ratingAmount = response.ratingAmount,
        userRating = response.userRating,
        revision = revision,
    )
}
