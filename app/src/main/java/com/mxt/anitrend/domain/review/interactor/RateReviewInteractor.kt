package com.mxt.anitrend.domain.review.interactor

import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.review.ReviewStore
import com.mxt.anitrend.data.store.review.ReviewStoreChange
import com.mxt.anitrend.domain.interactor.executeMutation
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
                reviewStore.apply(
                    ReviewStoreChange.ReviewRated(
                        review = review,
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
