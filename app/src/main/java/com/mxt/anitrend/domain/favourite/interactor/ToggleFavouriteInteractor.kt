package com.mxt.anitrend.domain.favourite.interactor

import com.mxt.anitrend.data.store.favourite.FavouriteStore
import com.mxt.anitrend.data.store.favourite.FavouriteStoreChange
import com.mxt.anitrend.data.store.mutation.MutationExecutor
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.data.store.mutation.ResourceKey
import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import com.mxt.anitrend.domain.interactor.executeMutation
import com.mxt.anitrend.domain.model.ToggleFavouriteCommand
import com.mxt.anitrend.repository.BaseRepository

/**
 * Toggles the favourite flag of a typed favourite target through
 * [BaseRepository.toggleFavourite] and commits the result into [FavouriteStore] as a
 * revisioned [com.mxt.anitrend.data.store.favourite.FavouriteFlag].
 *
 * API concession (approved): the ToggleFavourite mutation returns the caller's full
 * favourites container rather than the toggled entity's own `isFavourite`, so the flipped
 * boolean cannot be read from the server response. Instead the new flag is inferred by
 * flipping the last committed store value for the key, and only after a successful server
 * response. A failed request never touches the store, same-resource requests are serialised
 * via [ResourceKey.FavouriteStudio] (and the sibling favourite resource keys), and stale
 * responses are rejected by the store revision rules.
 *
 * Session invalidation is preserved via
 * [com.mxt.anitrend.data.store.mutation.MutationContext.ensureSessionActive] before the
 * commit, so a logout during the request can never commit state.
 */
class ToggleFavouriteInteractor(
    private val baseRepository: BaseRepository,
    private val mutationExecutor: MutationExecutor,
    private val favouriteStore: FavouriteStore,
    private val requestSequence: RequestSequence,
) {
    suspend operator fun invoke(command: ToggleFavouriteCommand): MutationResult {
        val resourceKey =
            when (val key = command.key) {
                is FavouriteKey.Anime -> ResourceKey.FavouriteAnime(key.id)
                is FavouriteKey.Manga -> ResourceKey.FavouriteManga(key.id)
                is FavouriteKey.Character -> ResourceKey.FavouriteCharacter(key.id)
                is FavouriteKey.Staff -> ResourceKey.FavouriteStaff(key.id)
                is FavouriteKey.Studio -> ResourceKey.FavouriteStudio(key.id)
            }
        val operationKey =
            when (val key = command.key) {
                is FavouriteKey.Anime -> OperationKey.favouriteAnime(key.id)
                is FavouriteKey.Manga -> OperationKey.favouriteManga(key.id)
                is FavouriteKey.Character -> OperationKey.favouriteCharacter(key.id)
                is FavouriteKey.Staff -> OperationKey.favouriteStaff(key.id)
                is FavouriteKey.Studio -> OperationKey.favouriteStudio(key.id)
            }

        return executeMutation(
            mutationExecutor = mutationExecutor,
            requestSequence = requestSequence,
            resourceKey = resourceKey,
            operationKey = operationKey,
            failureMessage = "Unable to toggle favourite",
        ) { revision, context ->
            baseRepository.toggleFavourite(
                animeId = (command.key as? FavouriteKey.Anime)?.id?.toInt(),
                mangaId = (command.key as? FavouriteKey.Manga)?.id?.toInt(),
                characterId = (command.key as? FavouriteKey.Character)?.id?.toInt(),
                staffId = (command.key as? FavouriteKey.Staff)?.id?.toInt(),
                studioId = (command.key as? FavouriteKey.Studio)?.id?.toInt(),
                page = null,
                perPage = null,
            ).fold(
                onSuccess = {
                    context.ensureSessionActive()
                    val currentIsFavourite =
                        favouriteStore.state.value.flagsByKey[command.key]?.isFavourite ?: false
                    favouriteStore.apply(
                        FavouriteStoreChange.FavouriteFlagReplaced(
                            key = command.key,
                            isFavourite = !currentIsFavourite,
                            revision = revision,
                        ),
                    )
                    MutationResult.Success
                },
                onFailure = { throwable ->
                    MutationResult.Failure(
                        message = throwable.message ?: "Unable to toggle favourite",
                        cause = throwable,
                    )
                },
            )
        }
    }
}
