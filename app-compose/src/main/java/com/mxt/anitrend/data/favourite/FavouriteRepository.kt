package com.mxt.anitrend.data.favourite

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.mxt.anitrend.data.graphql.MediaListCollectionQuery
import com.mxt.anitrend.data.graphql.UserFavouritesQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class MediaListEntry(
    val id: Long,
    val status: String?,
    val score: Int?,
    val progress: Int?,
    val mediaId: Long,
    val mediaTitle: String,
    val mediaCoverMedium: String?,
    val mediaType: String?,
)

data class MediaListGroup(
    val name: String,
    val entries: List<MediaListEntry>,
)

data class FavMedia(
    val id: Long,
    val title: String,
    val type: String?,
    val format: String?,
    val coverMedium: String?,
    val meanScore: Int?,
)

data class FavEntity(
    val id: Long,
    val name: String,
    val imageMedium: String?,
)

data class FavStudio(
    val id: Long,
    val name: String,
    val isAnimationStudio: Boolean,
)

data class UserFavourites(
    val anime: List<FavMedia>,
    val manga: List<FavMedia>,
    val characters: List<FavEntity>,
    val staff: List<FavEntity>,
    val studios: List<FavStudio>,
)

interface FavouriteRepository {
    fun observeMediaListCollection(): Flow<List<MediaListGroup>>
    fun observeUserFavourites(userId: Int? = null, userName: String? = null): Flow<UserFavourites>
}

class ApolloFavouriteRepository(
    private val apolloClient: ApolloClient,
) : FavouriteRepository {

    override fun observeMediaListCollection(): Flow<List<MediaListGroup>> = flow {
        val response = apolloClient.query(
            MediaListCollectionQuery(type = Optional.absent())
        ).execute()

        val groups = response.data?.MediaListCollection?.lists
            ?.filterNotNull()
            ?.map { list ->
                MediaListGroup(
                    name = list.name ?: "Unknown",
                    entries = list.entries
                        ?.filterNotNull()
                        ?.map { entry ->
                            MediaListEntry(
                                id = entry.id.toLong(),
                                status = entry.status?.name,
                                score = entry.score?.toInt(),
                                progress = entry.progress,
                                mediaId = entry.media?.id?.toLong() ?: 0L,
                                mediaTitle = entry.media?.title?.userPreferred ?: "Unknown",
                                mediaCoverMedium = entry.media?.coverImage?.medium,
                                mediaType = entry.media?.type?.name,
                            )
                        }
                        .orEmpty(),
                )
            }
            .orEmpty()

        emit(groups)
    }

    override fun observeUserFavourites(userId: Int?, userName: String?): Flow<UserFavourites> = flow {
        val response = apolloClient.query(
            UserFavouritesQuery(
                userId = Optional.presentIfNotNull(userId),
                userName = Optional.presentIfNotNull(userName),
            )
        ).execute()

        val fav = response.data?.User?.favourites

        val anime = fav?.anime?.nodes
            ?.filterNotNull()
            ?.map { node ->
                FavMedia(
                    id = node.id.toLong(),
                    title = node.title?.userPreferred ?: "Unknown",
                    type = node.type?.name,
                    format = node.format?.name,
                    coverMedium = node.coverImage?.medium,
                    meanScore = node.meanScore,
                )
            }
            .orEmpty()

        val manga = fav?.manga?.nodes
            ?.filterNotNull()
            ?.map { node ->
                FavMedia(
                    id = node.id.toLong(),
                    title = node.title?.userPreferred ?: "Unknown",
                    type = node.type?.name,
                    format = node.format?.name,
                    coverMedium = node.coverImage?.medium,
                    meanScore = node.meanScore,
                )
            }
            .orEmpty()

        val characters = fav?.characters?.nodes
            ?.filterNotNull()
            ?.map { node ->
                FavEntity(
                    id = node.id.toLong(),
                    name = node.name?.full ?: "Unknown",
                    imageMedium = node.image?.medium,
                )
            }
            .orEmpty()

        val staff = fav?.staff?.nodes
            ?.filterNotNull()
            ?.map { node ->
                FavEntity(
                    id = node.id.toLong(),
                    name = node.name?.full ?: "Unknown",
                    imageMedium = node.image?.medium,
                )
            }
            .orEmpty()

        val studios = fav?.studios?.nodes
            ?.filterNotNull()
            ?.map { node ->
                FavStudio(
                    id = node.id.toLong(),
                    name = node.name,
                    isAnimationStudio = node.isAnimationStudio,
                )
            }
            .orEmpty()

        emit(UserFavourites(anime, manga, characters, staff, studios))
    }
}
