package com.mxt.anitrend.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mxt.anitrend.data.feed.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FeedViewModel(
    private val feedRepository: FeedRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(FeedTab.Activity)
    val selectedTab: StateFlow<FeedTab> = _selectedTab

    private val _activityState = MutableStateFlow<FeedTabState>(FeedTabState.Loading)
    val activityState: StateFlow<FeedTabState> = _activityState

    private val _animeState = MutableStateFlow<TrendingTabState>(TrendingTabState.Loading)
    val animeState: StateFlow<TrendingTabState> = _animeState

    private val _mangaState = MutableStateFlow<TrendingTabState>(TrendingTabState.Loading)
    val mangaState: StateFlow<TrendingTabState> = _mangaState

    private val _trendingState = MutableStateFlow<TrendingTabState>(TrendingTabState.Loading)
    val trendingState: StateFlow<TrendingTabState> = _trendingState

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState

    init {
        loadActivity()
        loadAnime()
        loadManga()
        loadTrending()
    }

    fun selectTab(tab: FeedTab) {
        _selectedTab.value = tab
    }

    fun loadActivity(page: Int = 1) {
        viewModelScope.launch {
            feedRepository.observeFeed(page)
                .onStart { _activityState.value = FeedTabState.Loading }
                .catch { e -> _activityState.value = FeedTabState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _activityState.value = FeedTabState.Success(items)
                    _uiState.value = FeedUiState.Success(items)
                }
        }
    }

    fun loadAnime(page: Int = 1) {
        viewModelScope.launch {
            feedRepository.observeTrendingAnime(page)
                .onStart { _animeState.value = TrendingTabState.Loading }
                .catch { e -> _animeState.value = TrendingTabState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _animeState.value = TrendingTabState.Success(items)
                }
        }
    }

    fun loadManga(page: Int = 1) {
        viewModelScope.launch {
            feedRepository.observeTrendingManga(page)
                .onStart { _mangaState.value = TrendingTabState.Loading }
                .catch { e -> _mangaState.value = TrendingTabState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _mangaState.value = TrendingTabState.Success(items)
                }
        }
    }

    fun loadTrending(page: Int = 1) {
        viewModelScope.launch {
            feedRepository.observeTrending(page)
                .onStart { _trendingState.value = TrendingTabState.Loading }
                .catch { e -> _trendingState.value = TrendingTabState.Error(e.message ?: "Unknown error") }
                .collect { items ->
                    _trendingState.value = TrendingTabState.Success(items)
                }
        }
    }

    @Deprecated("Use per-tab state instead")
    fun loadFeed(page: Int = 1) {
        loadActivity(page)
    }
}
