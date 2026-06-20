package com.mxt.anitrend.data.staff

import com.apollographql.apollo.ApolloClient
import com.mxt.anitrend.data.graphql.StaffDetailQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class StaffDetail(
    val id: Long,
    val name: String,
    val imageLarge: String?,
    val imageMedium: String?,
    val description: String?,
    val language: String?,
    val isFavourite: Boolean,
    val siteUrl: String?,
    val media: List<MediaAppearance>,
) {
    data class MediaAppearance(
        val id: Long,
        val title: String,
        val type: String?,
        val format: String?,
        val coverMedium: String?,
        val meanScore: Int?,
    )
}

interface StaffRepository {
    fun observeStaff(id: Int): Flow<StaffDetail?>
}

class ApolloStaffRepository(
    private val apolloClient: ApolloClient,
) : StaffRepository {
    override fun observeStaff(id: Int): Flow<StaffDetail?> = flow {
        val response = apolloClient.query(StaffDetailQuery(id = id)).execute()
        val staff = response.data?.Staff
        emit(staff?.toDetail())
    }

    private fun StaffDetailQuery.Staff.toDetail() = StaffDetail(
        id = id.toLong(),
        name = name?.full ?: "Unknown",
        imageLarge = image?.large,
        imageMedium = image?.medium,
        description = description,
        language = language?.name,
        isFavourite = isFavourite ?: false,
        siteUrl = siteUrl,
        media = staffMedia?.nodes?.filterNotNull()?.map {
            StaffDetail.MediaAppearance(
                id = it.id.toLong(),
                title = it.title?.userPreferred ?: "Unknown",
                type = it.type?.name,
                format = it.format?.name,
                coverMedium = it.coverImage?.medium,
                meanScore = it.meanScore,
            )
        }.orEmpty(),
    )
}
