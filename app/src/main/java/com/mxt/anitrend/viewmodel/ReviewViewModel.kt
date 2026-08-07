package com.mxt.anitrend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.data.store.review.ReviewQueryKey
import com.mxt.anitrend.data.store.review.ReviewStore
import com.mxt.anitrend.data.store.mutation.RequestSequence
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.review.interactor.RateReviewInteractor
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.KeyUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ReviewViewModel(
    private val browseRepository: BrowseRepository,
    private val reviewStore: ReviewStore,
    private val requestSequence: RequestSequence,
    private val rateReviewInteractor: RateReviewInteractor,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(
            val content: PageContainer<ReviewRecord>,
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

    private val _rateReviewEvents = Channel<ReviewRateOutcome>(Channel.BUFFERED)

    /**
     * One-shot outcomes of [rateReview] calls. The canonical committed state is delivered
     * through the [state] flow (store rebinding); these events only drive widget
     * convergence (reset the vote loading state, surface a failure message).
     *
     * A buffered single-consumer channel is used so an outcome emitted while the fragment
     * collector is inactive (for example when the view left STARTED mid-mutation) is
     * retained and delivered when the next collector subscribes, instead of being dropped.
     */
    val rateReviewEvents: Flow<ReviewRateOutcome> = _rateReviewEvents.receiveAsFlow()

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
                            content = PageContainer<ReviewRecord>().apply {
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
     * Loads reviews for a given media entry. Repeatable for pagination; no loadedOnce guard.
     */
    fun load(mediaId: Long, type: MediaType?, page: Int) {
        val queryKey = ReviewQueryKey(
            mediaId = mediaId,
            mediaType = type,
            sort = null,
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
                    mediaId = mediaId,
                    page = page,
                    perPage = KeyUtil.PAGING_LIMIT,
                    type = type,
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
                Timber.e(throwable, "ReviewViewModel load failed")
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
            val outcome = ReviewRateOutcome(
                reviewId = reviewId,
                result = rateReviewInteractor(reviewId, rating),
            )
            _rateReviewEvents.send(outcome)
        }
    }

    override fun onCleared() {
        _rateReviewEvents.close()
        super.onCleared()
    }
}
