package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.data.store.mutation.OperationKey
import com.mxt.anitrend.data.store.mutation.OperationStatus
import com.mxt.anitrend.domain.medialist.interactor.DeleteMediaListEntryInteractor
import com.mxt.anitrend.domain.medialist.interactor.IncrementMediaProgressInteractor
import com.mxt.anitrend.domain.medialist.interactor.SaveMediaListEntryInteractor
import com.mxt.anitrend.domain.model.IncrementMediaProgressCommand
import com.mxt.anitrend.domain.model.SaveMediaListEntryCommand
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaListMutationViewModel(
    private val saveMediaListEntryInteractor: SaveMediaListEntryInteractor,
    private val deleteMediaListEntryInteractor: DeleteMediaListEntryInteractor,
    private val incrementMediaProgressInteractor: IncrementMediaProgressInteractor,
    private val mutationRegistry: MutationRegistry,
    private val userRepository: UserRepository,
) : ViewModel() {

    /** Read-only access to the current user's media list options, if a user is cached. */
    val currentUserMediaListOptions: MediaListOptions?
        get() = userRepository.cachedCurrentUser?.mediaListOptions

    enum class CompletedAction {
        SAVED,
        DELETED,
    }

    data class TargetState(
        val isSaveRunning: Boolean = false,
        val isDeleteRunning: Boolean = false,
        val errorMessage: String? = null,
        val completedAction: CompletedAction? = null,
        val outcomeVersion: Int = 0,
    )

    private data class LocalTargetState(
        val errorMessage: String? = null,
        val completedAction: CompletedAction? = null,
        val outcomeVersion: Int = 0,
    )

    private val localState = MutableStateFlow<Map<Long, LocalTargetState>>(emptyMap())

    fun observeTarget(
        mediaId: Long,
        entryId: Long?,
    ): Flow<TargetState> = combine(
        mutationRegistry.state,
        localState,
    ) { operations, outcomes ->
        val targetOutcomes = outcomes[mediaId] ?: LocalTargetState()
        TargetState(
            isSaveRunning = saveKeys(mediaId, entryId).any { operations[it].isRunning() },
            isDeleteRunning = deleteKeys(mediaId, entryId).any { operations[it].isRunning() },
            errorMessage = targetOutcomes.errorMessage,
            completedAction = targetOutcomes.completedAction,
            outcomeVersion = targetOutcomes.outcomeVersion,
        )
    }

    fun reset(mediaId: Long) {
        localState.update { current ->
            current - mediaId
        }
    }

    fun save(command: SaveMediaListEntryCommand) {
        val mediaId = command.mediaId ?: return
        val entryId = command.id?.toLong()
        if (saveKeys(mediaId, entryId).any { mutationRegistry.state.value[it].isRunning() }) {
            return
        }
        reset(mediaId)
        viewModelScope.launch {
            when (val result = saveMediaListEntryInteractor(command)) {
                MutationResult.Success -> markCompleted(mediaId, CompletedAction.SAVED)
                is MutationResult.Failure -> markFailure(mediaId, result.message)
            }
        }
    }

    fun delete(
        entryId: Long,
        mediaId: Long,
    ) {
        if (deleteKeys(mediaId, entryId).any { mutationRegistry.state.value[it].isRunning() }) {
            return
        }
        reset(mediaId)
        viewModelScope.launch {
            when (val result = deleteMediaListEntryInteractor(entryId = entryId, mediaId = mediaId)) {
                MutationResult.Success -> markCompleted(mediaId, CompletedAction.DELETED)
                is MutationResult.Failure -> markFailure(mediaId, result.message)
            }
        }
    }

    fun increment(command: IncrementMediaProgressCommand) {
        val mediaId = command.mediaId ?: return
        if (mutationRegistry.state.value[OperationKey.mediaListIncrementProgress(mediaId)].isRunning()) {
            return
        }
        reset(mediaId)
        viewModelScope.launch {
            when (val result = incrementMediaProgressInteractor(command)) {
                MutationResult.Success -> markCompleted(mediaId, CompletedAction.SAVED)
                is MutationResult.Failure -> markFailure(mediaId, result.message)
            }
        }
    }

    private fun markCompleted(
        mediaId: Long,
        action: CompletedAction,
    ) {
        localState.update { current ->
            val previous = current[mediaId] ?: LocalTargetState()
            current + (
                mediaId to previous.copy(
                    errorMessage = null,
                    completedAction = action,
                    outcomeVersion = previous.outcomeVersion + 1,
                )
                )
        }
    }

    private fun markFailure(
        mediaId: Long,
        message: String,
    ) {
        localState.update { current ->
            val previous = current[mediaId] ?: LocalTargetState()
            current + (
                mediaId to previous.copy(
                    errorMessage = message,
                    completedAction = null,
                    outcomeVersion = previous.outcomeVersion + 1,
                )
                )
        }
    }

    private fun saveKeys(
        mediaId: Long,
        entryId: Long?,
    ): List<OperationKey> = buildList {
        add(OperationKey.mediaListSave(mediaId))
        add(OperationKey.mediaListIncrementProgress(mediaId))
        entryId?.takeIf { it > 0 }?.let(OperationKey::mediaListSaveById)?.let(::add)
    }

    private fun deleteKeys(
        mediaId: Long,
        entryId: Long?,
    ): List<OperationKey> = buildList {
        add(OperationKey.mediaListDeleteByMedia(mediaId))
        entryId?.takeIf { it > 0 }?.let(OperationKey::mediaListDelete)?.let(::add)
    }

    private fun OperationStatus?.isRunning(): Boolean = this is OperationStatus.Running
}
