package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.anitrend.retrofit.graphql.model.attribute.GraphError
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewBrowse
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.model.api.retro.anilist.BrowseModel
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class BrowseReviewViewModel(
    private val browseService: BrowseModel,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val content: PageContainer<Review>) : UiState
        data class Error(val message: String) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    /**
     * Loads a browsable list of reviews. Repeatable for pagination; no loadedOnce guard.
     *
     * @param sort Combined sort string (e.g. "ID_DESC"); parsed to [ReviewSort] enum.
     */
    fun load(type: MediaType?, page: Int, sort: String?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            runCatching {
                withContext(ioDispatcher) {
                    val reviewSort: List<ReviewSort?>? =
                        sort?.let {
                            runCatching { ReviewSort.valueOf(it) }.getOrNull()?.let { r ->
                                listOf(r)
                            }
                        } ?: listOf(ReviewSort.CREATED_AT_DESC)
                    val request = ReviewBrowse.request(
                        type = type,
                        page = page,
                        perPage = KeyUtil.PAGING_LIMIT,
                        sort = reviewSort,
                        asHtml = false,
                    )
                    val response = browseService.getReviewBrowse(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body()
                            ?: throw IllegalStateException("Empty response body")
                        val graphErrors: List<GraphError>? = body.errors
                        if (!graphErrors.isNullOrEmpty()) {
                            throw RuntimeException(
                                graphErrors.first().message
                                    ?: "GraphQL error",
                            )
                        }
                        body.data?.result
                            ?: throw IllegalStateException("Empty response body")
                    } else {
                        throw RuntimeException(response.apiError())
                    }
                }
            }.onSuccess { content ->
                _state.value = UiState.Success(content)
            }.onFailure { throwable ->
                Timber.e(throwable, "BrowseReviewViewModel load failed")
                _state.value = UiState.Error(
                    throwable.message ?: "Failed to load reviews",
                )
            }
        }
    }
}
