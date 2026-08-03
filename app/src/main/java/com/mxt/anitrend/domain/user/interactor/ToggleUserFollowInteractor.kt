package com.mxt.anitrend.domain.user.interactor

import com.mxt.anitrend.data.mapper.toUserRecord
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.data.store.user.UserStore
import com.mxt.anitrend.data.store.user.UserStoreChange
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.repository.UserRepository

/**
 * Toggles the follow state of a user through [UserRepository.toggleFollow] and commits the
 * authoritative response into [UserStore] as a revisioned [com.mxt.anitrend.domain.model.UserRecord].
 *
 * The mutation is serialised per [ResourceKey.User] and any stale response is rejected by the
 * store revision rules. Session invalidation is preserved via [com.mxt.anitrend.data.store.mutation.MutationContext.ensureSessionActive]
 * before the commit, so a logout during the request can never commit state.
 */
class ToggleUserFollowInteractor(
    private val userRepository: UserRepository,
    private val mutationExecutor: MutationExecutor,
    private val userStore: UserStore,
    private val requestSequence: RequestSequence,
) {
    suspend operator fun invoke(command: ToggleUserFollowCommand): MutationResult = executeMutation(
        mutationExecutor = mutationExecutor,
        requestSequence = requestSequence,
        resourceKey = ResourceKey.User(command.userId),
        operationKey = OperationKey.userFollow(command.userId),
        failureMessage = "Unable to toggle follow",
    ) { revision, context ->
        userRepository.toggleFollow(command.userId).fold(
            onSuccess = { user ->
                context.ensureSessionActive()
                userStore.apply(
                    UserStoreChange.UserUpserted(
                        user = user.toUserRecord(revision = revision),
                    ),
                )
                MutationResult.Success
            },
            onFailure = { throwable ->
                MutationResult.Failure(
                    message = throwable.message ?: "Unable to toggle follow",
                    cause = throwable,
                )
            },
        )
    }
}
