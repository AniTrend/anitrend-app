package com.mxt.anitrend.ui.feed

enum class FeedTab { Activity, Anime, Manga, Trending }

data class FeedItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val timestamp: String,
)

data class TrendingMedia(
    val id: Long,
    val title: String,
    val coverMedium: String?,
    val coverLarge: String?,
    val meanScore: Int?,
    val type: String?,
    val format: String?,
    val episodes: Int?,
)

sealed class FeedTabState {
    data object Loading : FeedTabState()
    data class Success(val items: List<FeedItem>) : FeedTabState()
    data class Error(val message: String) : FeedTabState()
}

sealed class TrendingTabState {
    data object Loading : TrendingTabState()
    data class Success(val items: List<TrendingMedia>) : TrendingTabState()
    data class Error(val message: String) : TrendingTabState()
}

sealed class FeedUiState {
    data object Loading : FeedUiState()
    data class Success(val items: List<FeedItem>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}
