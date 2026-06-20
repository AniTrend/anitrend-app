package com.mxt.anitrend.data.profile

import com.apollographql.apollo.ApolloClient
import com.mxt.anitrend.data.graphql.CurrentUserQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class UserProfile(
    val id: Long,
    val name: String,
    val about: String?,
    val avatarLarge: String?,
    val avatarMedium: String?,
    val bannerImage: String?,
    val donatorTier: Int?,
    val unreadNotificationCount: Int?,
    val titleLanguage: String?,
    val displayAdultContent: Boolean?,
    val airingNotifications: Boolean?,
    val profileColor: String?,
    val watchedTime: Int?,
    val chaptersRead: Int?,
    val animeStatusDistribution: List<StatusCount>,
    val mangaStatusDistribution: List<StatusCount>,
    val favouredGenres: List<GenreStat>,
    val favouredYears: List<YearStat>,
) {
    data class StatusCount(val status: String?, val amount: Int?)
    data class GenreStat(val genre: String?, val amount: Int?, val meanScore: Int?)
    data class YearStat(val year: Int?, val amount: Int?, val meanScore: Int?)
}

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
}

class ApolloProfileRepository(
    private val apolloClient: ApolloClient,
) : ProfileRepository {

    override fun observeProfile(): Flow<UserProfile?> = flow {
        val response = apolloClient.query(CurrentUserQuery()).execute()
        val viewer = response.data?.Viewer
        emit(viewer?.toUserProfile())
    }

    private fun CurrentUserQuery.Viewer.toUserProfile(): UserProfile {
        return UserProfile(
            id = id.toLong(),
            name = name,
            about = about,
            avatarLarge = avatar?.large,
            avatarMedium = avatar?.medium,
            bannerImage = bannerImage,
            donatorTier = donatorTier,
            unreadNotificationCount = unreadNotificationCount,
            titleLanguage = options?.titleLanguage?.name,
            displayAdultContent = options?.displayAdultContent,
            airingNotifications = options?.airingNotifications,
            profileColor = options?.profileColor,
            watchedTime = stats?.watchedTime,
            chaptersRead = stats?.chaptersRead,
            animeStatusDistribution = stats?.animeStatusDistribution?.filterNotNull()?.map {
                UserProfile.StatusCount(it.status?.name, it.amount)
            }.orEmpty(),
            mangaStatusDistribution = stats?.mangaStatusDistribution?.filterNotNull()?.map {
                UserProfile.StatusCount(it.status?.name, it.amount)
            }.orEmpty(),
            favouredGenres = stats?.favouredGenres?.filterNotNull()?.map {
                UserProfile.GenreStat(it.genre, it.amount, it.meanScore)
            }.orEmpty(),
            favouredYears = stats?.favouredYears?.filterNotNull()?.map {
                UserProfile.YearStat(it.year, it.amount, it.meanScore)
            }.orEmpty(),
        )
    }
}
