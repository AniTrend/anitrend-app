package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SuggestionListViewModel(
    private val userRepository: UserRepository,
    private val browseRepository: BrowseRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<MediaBase>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    val currentUser: User?
        get() = userRepository.cachedCurrentUser

    /**
     * Loads suggested media browse. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(
        sort: String?,
        page: Int,
        tags: List<String>?,
        genres: List<String>?,
        isAdult: Boolean?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                val sortList: List<MediaSort>? =
                    sort?.let { runCatching { MediaSort.valueOf(it) }.getOrNull()?.let { listOf(it) } }
                browseRepository.getMediaBrowse(
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = MediaType.ANIME,
                    onList = false,
                    sort = sortList,
                    tags = tags,
                    genres = genres,
                    isAdult = isAdult,
                ).getOrThrow()
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "SuggestionListViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load suggestions",
                )
            }
        }
    }
}
