package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.FeedMutation
import com.mxt.anitrend.repository.FeedRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import com.mxt.anitrend.model.entity.anilist.FeedList as FeedListEntity

class FeedListViewModel(
    private val feedRepository: FeedRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<com.mxt.anitrend.model.entity.anilist.FeedList>,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            feedRepository.mutationEvents.collect { event ->
                when (event) {
                    is FeedMutation.FeedSaved -> {
                        val current = _state.value
                        if (current is UiState.Success) {
                            val items = current.content.pageData.toMutableList()
                            val index = items.indexOfFirst { it.id == event.feed.id }
                            if (index >= 0) items[index] = event.feed else items.add(0, event.feed)
                            _state.value = current.copy(
                                content = PageContainer<FeedListEntity>().apply {
                                    if (current.content.hasPageInfo()) pageInfo = current.content.pageInfo
                                    pageData = items
                                },
                            )
                        }
                    }
                    is FeedMutation.FeedDeleted -> {
                        val current = _state.value
                        if (current is UiState.Success) {
                            val items = current.content.pageData.filter { it.id != event.id }
                            _state.value = current.copy(
                                content = PageContainer<FeedListEntity>().apply {
                                    if (current.content.hasPageInfo()) pageInfo = current.content.pageInfo
                                    pageData = items
                                },
                            )
                        }
                    }
                    else -> { /* ignore reply events - not relevant to feed list */ }
                }
            }
        }
    }

    /**
     * Loads the global/home feed. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(
        page: Int,
        pageLimit: Int,
        isFollowing: Boolean?,
        type: ActivityType?,
        isMixed: Boolean?,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            feedRepository.getFeedList(
                page = page,
                perPage = pageLimit,
                isFollowing = isFollowing,
                type = type,
                isMixed = isMixed,
            ).onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "FeedListViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load feed",
                )
            }
        }
    }
}
