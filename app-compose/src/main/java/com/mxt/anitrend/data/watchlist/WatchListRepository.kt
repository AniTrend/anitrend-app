package com.mxt.anitrend.data.watchlist

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.mxt.anitrend.data.graphql.WatchListQuery
import com.mxt.anitrend.data.graphql.type.MediaType as ApolloMediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class WatchMediaType { ANIME, MANGA }

data class WatchEntry(
    val id: Long,
    val status: String?,
    val score: Int?,
    val progress: Int?,
    val mediaId: Long,
    val mediaTitle: String,
    val mediaCoverMedium: String?,
    val mediaType: String?,
    val episodes: Int?,
    val chapters: Int?,
    val nextAiringEpisode: Int?,
    val nextAiringEpisodeNumber: Int?,
)

data class WatchListGroup(
    val name: String,
    val status: String?,
    val entries: List<WatchEntry>,
)

interface WatchListRepository {
    fun observeWatchList(
        type: WatchMediaType,
        userId: Int? = null,
        userName: String? = null,
    ): Flow<List<WatchListGroup>>
}

class ApolloWatchListRepository(
    private val apolloClient: ApolloClient,
) : WatchListRepository {

    override fun observeWatchList(
        type: WatchMediaType,
        userId: Int?,
        userName: String?,
    ): Flow<List<WatchListGroup>> = flow {
        val apolloType = when (type) {
            WatchMediaType.ANIME -> ApolloMediaType.ANIME
            WatchMediaType.MANGA -> ApolloMediaType.MANGA
        }
        val response = apolloClient.query(
            WatchListQuery(
                userId = Optional.presentIfNotNull(userId),
                userName = Optional.presentIfNotNull(userName),
                type = Optional.present(apolloType),
            )
        ).execute()

        val groups = response.data?.MediaListCollection?.lists
            ?.filterNotNull()
            ?.map { list ->
                WatchListGroup(
                    name = list.name ?: "Unknown",
                    status = list.status?.name,
                    entries = list.entries
                        ?.filterNotNull()
                        ?.map { entry ->
                            val media = entry.media
                            val nextAiring = media?.nextAiringEpisode
                            WatchEntry(
                                id = entry.id.toLong(),
                                status = entry.status?.name,
                                score = entry.score?.toInt(),
                                progress = entry.progress,
                                mediaId = media?.id?.toLong() ?: 0L,
                                mediaTitle = media?.title?.userPreferred ?: "Unknown",
                                mediaCoverMedium = media?.coverImage?.medium,
                                mediaType = media?.type?.name,
                                episodes = media?.episodes,
                                chapters = media?.chapters,
                                nextAiringEpisode = nextAiring?.airingAt,
                                nextAiringEpisodeNumber = nextAiring?.episode,
                            )
                        }
                        .orEmpty(),
                )
            }
            .orEmpty()

        emit(groups)
    }
}
