package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStore
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.domain.review.interactor.RateReviewInteractor
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.graphql.generated.ReviewSort
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class BrowseReviewViewModel(
    private val browseRepository: BrowseRepository,
    private val reviewStore: ReviewStore,
    private val requestSequence: RequestSequence,
    private val rateReviewInteractor: RateReviewInteractor,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<Review>,
            val replaceExisting: Boolean = false,
        ) : UiState
        data class Error(val message: String) : UiState
    }

    private data class ScreenState(
        val queryKey: ReviewQueryKey? = null,
        val requestToken: Long = 0L,
        val lastRequestedPage: Int = 1,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val state: StateFlow<UiState> =
        screenState
            .flatMapLatest { screen ->
                val queryKey = screen.queryKey ?: return@flatMapLatest flowOf(
                    if (screen.errorMessage != null) {
                        UiState.Error(screen.errorMessage)
                    } else {
                        UiState.Loading
                    },
                )

                reviewStore.observeQuery(queryKey).map { query ->
                    when {
                        screen.errorMessage != null -> UiState.Error(screen.errorMessage)
                        screen.isLoading && query.reviews.isEmpty() -> UiState.Loading
                        else -> UiState.Success(
                            content = PageContainer<Review>().apply {
                                query.pageInfo?.toPageInfo()?.let { pageInfo = it }
                                pageData = query.reviews
                            },
                            replaceExisting = screen.lastRequestedPage <= 1,
                        )
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UiState.Loading,
            )

    /**
     * Loads a browsable list of reviews. Repeatable for pagination; no loadedOnce guard.
     *
     * @param sort Combined sort string (e.g. "ID_DESC"); parsed to [ReviewSort] enum.
     */
    fun load(type: MediaType?, page: Int, sort: String?) {
        val reviewSort = sort?.let {
            runCatching { ReviewSort.valueOf(it) }.getOrNull()?.let(::listOf)
        } ?: listOf(ReviewSort.CREATED_AT_DESC)
        val queryKey = ReviewQueryKey(
            mediaId = null,
            mediaType = type,
            sort = reviewSort.firstOrNull(),
        )
        val token = if (page > 1) screenState.value.requestToken else requestSequence.next()
        screenState.update {
            it.copy(
                queryKey = queryKey,
                requestToken = token,
                lastRequestedPage = page,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                browseRepository.getReviewBrowse(
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
                    sort = reviewSort,
                    asHtml = false,
                    queryKey = queryKey,
                    readToken = token,
                ).getOrThrow()
            }.onSuccess {
                if (screenState.value.requestToken != token) {
                    return@onSuccess
                }
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                if (screenState.value.requestToken != token) {
                    return@onFailure
                }
                Timber.e(throwable, "BrowseReviewViewModel load failed")
                screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load reviews",
                    )
                }
            }
        }
    }

    fun rateReview(reviewId: Long, rating: ReviewRating?) {
        viewModelScope.launch {
            rateReviewInteractor(reviewId, rating)
        }
    }
}
