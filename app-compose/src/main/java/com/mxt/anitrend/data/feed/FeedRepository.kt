package com.mxt.anitrend.data.feed

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.mxt.anitrend.data.graphql.AnimeTrendingQuery
import com.mxt.anitrend.data.graphql.FeedListQuery
import com.mxt.anitrend.data.graphql.MangaTrendingQuery
import com.mxt.anitrend.data.graphql.MediaTrendingQuery
import com.mxt.anitrend.ui.feed.FeedItem
import com.mxt.anitrend.ui.feed.TrendingMedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface FeedRepository {
    fun observeFeed(page: Int = 1): Flow<List<FeedItem>>
    fun observeTrendingAnime(page: Int = 1): Flow<List<TrendingMedia>>
    fun observeTrendingManga(page: Int = 1): Flow<List<TrendingMedia>>
    fun observeTrending(page: Int = 1): Flow<List<TrendingMedia>>
}

class ApolloFeedRepository(
    private val apolloClient: ApolloClient,
) : FeedRepository {

    override fun observeFeed(page: Int): Flow<List<FeedItem>> = flow {
        val response = apolloClient.query(
            FeedListQuery(page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.activities
            ?.mapNotNull { it?.toFeedItem() }
            .orEmpty()

        emit(items)
    }

    override fun observeTrendingAnime(page: Int): Flow<List<TrendingMedia>> = flow {
        val response = apolloClient.query(
            AnimeTrendingQuery(page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.media
            ?.filterNotNull()
            ?.map { it.toTrendingMedia() }
            .orEmpty()

        emit(items)
    }

    override fun observeTrendingManga(page: Int): Flow<List<TrendingMedia>> = flow {
        val response = apolloClient.query(
            MangaTrendingQuery(page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.media
            ?.filterNotNull()
            ?.map { it.toTrendingMedia() }
            .orEmpty()

        emit(items)
    }

    override fun observeTrending(page: Int): Flow<List<TrendingMedia>> = flow {
        val response = apolloClient.query(
            MediaTrendingQuery(page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.media
            ?.filterNotNull()
            ?.map { it.toTrendingMedia() }
            .orEmpty()

        emit(items)
    }

    private fun AnimeTrendingQuery.Medium.toTrendingMedia(): TrendingMedia {
        return TrendingMedia(
            id = id.toLong(),
            title = title?.userPreferred ?: "Unknown",
            coverMedium = coverImage?.medium,
            coverLarge = coverImage?.large,
            meanScore = meanScore,
            type = "ANIME",
            format = format?.name,
            episodes = episodes,
        )
    }

    private fun MangaTrendingQuery.Medium.toTrendingMedia(): TrendingMedia {
        return TrendingMedia(
            id = id.toLong(),
            title = title?.userPreferred ?: "Unknown",
            coverMedium = coverImage?.medium,
            coverLarge = coverImage?.large,
            meanScore = meanScore,
            type = "MANGA",
            format = format?.name,
            episodes = episodes,
        )
    }

    private fun MediaTrendingQuery.Medium.toTrendingMedia(): TrendingMedia {
        return TrendingMedia(
            id = id.toLong(),
            title = title?.userPreferred ?: "Unknown",
            coverMedium = coverImage?.medium,
            coverLarge = coverImage?.large,
            meanScore = meanScore,
            type = type?.name,
            format = format?.name,
            episodes = null,
        )
    }

    private fun FeedListQuery.Activity.toFeedItem(): FeedItem? {
        val listActivity = onListActivity
        val textActivity = onTextActivity

        return when {
            listActivity != null -> FeedItem(
                id = listActivity.id.toLong(),
                title = listActivity.media?.title?.userPreferred ?: "Unknown",
                subtitle = buildString {
                    val status = listActivity.status
                    val progress = listActivity.progress
                    if (!status.isNullOrEmpty()) append(status)
                    if (!progress.isNullOrEmpty()) {
                        if (isNotEmpty()) append(" · ")
                        append(progress)
                    }
                }.ifEmpty { listActivity.type?.name ?: "Activity" },
                imageUrl = listActivity.media?.coverImage?.medium,
                timestamp = formatTimestamp(listActivity.createdAt),
            )
            textActivity != null -> FeedItem(
                id = textActivity.id.toLong(),
                title = textActivity.user?.name ?: "User",
                subtitle = textActivity.text?.take(100) ?: "",
                imageUrl = textActivity.user?.avatar?.medium,
                timestamp = formatTimestamp(textActivity.createdAt),
            )
            else -> null
        }
    }

    private fun formatTimestamp(unixSeconds: Int): String {
        val now = System.currentTimeMillis() / 1000
        val diff = now - unixSeconds
        return when {
            diff < 60 -> "just now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            diff < 604800 -> "${diff / 86400}d ago"
            else -> "${diff / 604800}w ago"
        }
    }
}
