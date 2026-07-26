package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.model.entity.anilist.edge.MediaEdge
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.EdgeContainer
import com.mxt.anitrend.repository.StaffRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaStaffRoleViewModel(
    private val staffRepository: StaffRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: ConnectionContainer<EdgeContainer<MediaEdge>>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads staff media roles. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(id: Long, onList: Boolean?, page: Int) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                staffRepository.getStaffRoles(
                    id = id,
                    onList = onList,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "MediaStaffRoleViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load staff roles",
                )
            }
        }
    }
}
