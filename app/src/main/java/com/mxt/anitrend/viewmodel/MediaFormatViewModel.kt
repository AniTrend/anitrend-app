package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.repository.CharacterRepository
import com.mxt.anitrend.repository.StaffRepository
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.collection.GroupingUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaFormatViewModel(
    private val characterRepository: CharacterRepository,
    private val staffRepository: StaffRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val newItems: List<RecyclerItem>,
            val pageInfo: PageInfo?,
            val isEmpty: Boolean,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val accumulatedItems = mutableListOf<RecyclerItem>()

    /**
     * Loads media grouped by format. Repeatable for pagination; no loadedOnce guard.
     *
     * Page 1 resets accumulated items (refresh). Subsequent pages append.
     *
     * @param id The character or staff id.
     * @param onList Filter to media on the user's list (staff-only).
     * @param mediaType Raw enum name of [MediaType]; parsed internally.
     * @param page Current page number.
     * @param requestType One of [KeyUtil.CHARACTER_MEDIA_REQ] or [KeyUtil.STAFF_MEDIA_REQ].
     */
    fun load(
        id: Long,
        onList: Boolean?,
        mediaType: String?,
        page: Int,
        requestType: Int,
    ) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            if (page == 1) {
                accumulatedItems.clear()
            }
            val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
            val result = if (requestType == KeyUtil.CHARACTER_MEDIA_REQ) {
                characterRepository.getCharacterMedia(
                    id = id,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
                )
            } else {
                staffRepository.getStaffMedia(
                    id = id,
                    onList = onList,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
                )
            }
            result.fold(
                onSuccess = { container ->
                    val pageContainer = container.connection
                    if (pageContainer != null && !pageContainer.isEmpty) {
                        val newItems = GroupingUtil.groupMediaByFormat(
                            pageContainer.pageData,
                            accumulatedItems,
                        )
                        accumulatedItems.addAll(newItems)
                        _state.value = UiState.Success(
                            newItems = newItems,
                            pageInfo = if (pageContainer.hasPageInfo()) pageContainer.pageInfo else null,
                            isEmpty = false,
                        )
                    } else {
                        _state.value = UiState.Success(
                            newItems = emptyList(),
                            pageInfo = pageContainer?.let { if (it.hasPageInfo()) it.pageInfo else null },
                            isEmpty = true,
                        )
                    }
                },
                onFailure = { throwable ->
                    Timber.e(throwable, "MediaFormatViewModel load failed")
                    _state.value = UiState.Error(
                        throwable.message ?: "Failed to load media format",
                    )
                },
            )
        }
    }
}
