package com.mxt.anitrend.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.review.ReviewItem
import com.mxt.anitrend.data.review.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed class ReviewUiState {
    data object Loading : ReviewUiState()
    data class Success(val items: List<ReviewItem>) : ReviewUiState()
    data class Error(val message: String) : ReviewUiState()
}

class ReviewViewModel(
    private val repository: ReviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            repository.observeReviews()
                .onStart { _uiState.value = ReviewUiState.Loading }
                .catch { e -> _uiState.value = ReviewUiState.Error(e.message ?: "Unknown error") }
                .collect { items -> _uiState.value = ReviewUiState.Success(items) }
        }
    }
}
