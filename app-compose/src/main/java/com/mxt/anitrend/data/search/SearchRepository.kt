package com.mxt.anitrend.data.search

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.mxt.anitrend.data.graphql.CharacterSearchQuery
import com.mxt.anitrend.data.graphql.MediaSearchQuery
import com.mxt.anitrend.data.graphql.StaffSearchQuery
import com.mxt.anitrend.data.graphql.StudioSearchQuery
import com.mxt.anitrend.data.graphql.UserSearchQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SearchResult(
    val id: Long,
    val title: String,
    val type: String?,
    val format: String?,
    val status: String?,
    val meanScore: Int?,
    val coverMedium: String?,
    val coverLarge: String?,
    val seasonYear: Int?,
    val episodes: Int?,
    val chapters: Int?,
    val studios: List<String>,
)

data class CharacterResult(
    val id: Long,
    val name: String,
    val imageMedium: String?,
    val isFavourite: Boolean,
)

data class StaffResult(
    val id: Long,
    val name: String,
    val imageMedium: String?,
    val language: String?,
    val isFavourite: Boolean,
)

data class StudioResult(
    val id: Long,
    val name: String,
    val isAnimationStudio: Boolean,
    val isFavourite: Boolean,
)

data class UserResult(
    val id: Long,
    val name: String,
    val avatarMedium: String?,
    val about: String?,
)

interface SearchRepository {
    fun search(query: String, page: Int = 1): Flow<List<SearchResult>>
    fun searchCharacters(query: String, page: Int = 1): Flow<List<CharacterResult>>
    fun searchStaff(query: String, page: Int = 1): Flow<List<StaffResult>>
    fun searchStudios(query: String, page: Int = 1): Flow<List<StudioResult>>
    fun searchUsers(query: String, page: Int = 1): Flow<List<UserResult>>
}

class ApolloSearchRepository(
    private val apolloClient: ApolloClient,
) : SearchRepository {

    override fun search(query: String, page: Int): Flow<List<SearchResult>> = flow {
        val response = apolloClient.query(
            MediaSearchQuery(search = Optional.present(query), page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.media
            ?.filterNotNull()
            ?.map { media ->
                SearchResult(
                    id = media.id.toLong(),
                    title = media.title?.userPreferred ?: "Unknown",
                    type = media.type?.name,
                    format = media.format?.name,
                    status = media.status?.name,
                    meanScore = media.meanScore,
                    coverMedium = media.coverImage?.medium,
                    coverLarge = media.coverImage?.large,
                    seasonYear = media.seasonYear,
                    episodes = media.episodes,
                    chapters = media.chapters,
                    studios = media.studios?.nodes?.filterNotNull()?.map { it.name ?: "" }.orEmpty(),
                )
            }
            .orEmpty()

        emit(items)
    }

    override fun searchCharacters(query: String, page: Int): Flow<List<CharacterResult>> = flow {
        val response = apolloClient.query(
            CharacterSearchQuery(search = Optional.present(query), page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.characters
            ?.filterNotNull()
            ?.map { char ->
                CharacterResult(
                    id = char.id.toLong(),
                    name = char.name?.full ?: "Unknown",
                    imageMedium = char.image?.medium,
                    isFavourite = char.isFavourite ?: false,
                )
            }
            .orEmpty()

        emit(items)
    }

    override fun searchStaff(query: String, page: Int): Flow<List<StaffResult>> = flow {
        val response = apolloClient.query(
            StaffSearchQuery(search = Optional.present(query), page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.staff
            ?.filterNotNull()
            ?.map { staff ->
                StaffResult(
                    id = staff.id.toLong(),
                    name = staff.name?.full ?: "Unknown",
                    imageMedium = staff.image?.medium,
                    language = staff.language?.name,
                    isFavourite = staff.isFavourite ?: false,
                )
            }
            .orEmpty()

        emit(items)
    }

    override fun searchStudios(query: String, page: Int): Flow<List<StudioResult>> = flow {
        val response = apolloClient.query(
            StudioSearchQuery(search = Optional.present(query), page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.studios
            ?.filterNotNull()
            ?.map { studio ->
                StudioResult(
                    id = studio.id.toLong(),
                    name = studio.name,
                    isAnimationStudio = studio.isAnimationStudio,
                    isFavourite = studio.isFavourite ?: false,
                )
            }
            .orEmpty()

        emit(items)
    }

    override fun searchUsers(query: String, page: Int): Flow<List<UserResult>> = flow {
        val response = apolloClient.query(
            UserSearchQuery(search = Optional.present(query), page = Optional.present(page))
        ).execute()

        val items = response.data?.Page?.users
            ?.filterNotNull()
            ?.map { user ->
                UserResult(
                    id = user.id.toLong(),
                    name = user.name,
                    avatarMedium = user.avatar?.medium,
                    about = user.about,
                )
            }
            .orEmpty()

        emit(items)
    }
}
