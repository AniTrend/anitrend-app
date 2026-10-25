package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.store.user.UserStore
import com.mxt.anitrend.domain.model.ToggleUserFollowCommand
import com.mxt.anitrend.domain.user.interactor.ToggleUserFollowInteractor
import com.mxt.anitrend.graphql.generated.UserSort
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.util.KeyUtil
import java.util.TreeMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class UserSearchViewModel(
    private val searchRepository: SearchRepository,
    private val toggleUserFollowInteractor: ToggleUserFollowInteractor,
    private val userStore: UserStore,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        class Success(
            val content: PageContainer<UserBase>,
            val search: String?,
            val page: Int,
            val itemsInPage: Int,
        ) : UiState
        data class Error(val message: String, val search: String?, val page: Int) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val pages = TreeMap<Int, List<UserBase>>()
    private var loadedSearch: String? = null
    private var requestSequence = 0L

    /**
     * Committed follow states keyed by userId, derived from the canonical [UserStore].
     * Screens observe this instead of the legacy result callback, so a failed mutation
     * never mutates submitted items and committed state converges after success.
     */
    private val _followStates = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val followStates: StateFlow<Map<Long, Boolean>> = _followStates.asStateFlow()

    init {
        viewModelScope.launch {
            userStore.state
                .map { storeState -> storeState.usersById.mapValues { (_, record) -> record.isFollowing } }
                .distinctUntilChanged()
                .collect { states -> _followStates.value = states }
        }
    }

    /** Publishes complete, id-keyed search snapshots so lifecycle replay cannot append twice. */
    fun load(search: String?, page: Int) {
        val request = ++requestSequence
        if (page <= 1 || loadedSearch != search) {
            pages.clear()
            loadedSearch = search
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                searchRepository.searchUser(
                    search = search,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    sort = listOf(UserSort.SEARCH_MATCH),
                ).getOrThrow()
            }.onSuccess { content ->
                if (request != requestSequence) return@onSuccess
                pages[page] = content.pageData.toList()
                val snapshot = linkedMapOf<Long, UserBase>()
                var itemsInPage = 0
                pages.forEach { (loadedPage, items) ->
                    items.forEach { item ->
                        if (loadedPage == page && item.id !in snapshot) itemsInPage++
                        snapshot[item.id] = item
                    }
                }
                _state.value = UiState.Success(
                    content = PageContainer<UserBase>().apply {
                        pageData = snapshot.values.toList()
                        pageInfo = content.pageInfo
                    },
                    search = search,
                    page = page,
                    itemsInPage = itemsInPage,
                )
            }.onFailure { throwable ->
                if (request != requestSequence) return@onFailure
                Timber.e(throwable, "UserSearchViewModel load failed")
                _state.value = UiState.Error(
                    message = throwable.message ?: "Failed to load user search",
                    search = search,
                    page = page,
                )
            }
        }
    }

    /**
     * Fire-and-forget toggle routed through [ToggleUserFollowInteractor]. The authoritative
     * committed state is delivered via [followStates] observation.
     */
    fun toggleFollow(userId: Long) {
        viewModelScope.launch {
            toggleUserFollowInteractor(ToggleUserFollowCommand(userId = userId))
        }
    }
}
