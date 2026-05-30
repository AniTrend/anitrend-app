package com.mxt.anitrend.data.studio

import com.apollographql.apollo.ApolloClient
import com.mxt.anitrend.data.graphql.StudioDetailQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class StudioDetail(
    val id: Long,
    val name: String,
    val isAnimationStudio: Boolean,
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

interface StudioRepository {
    fun observeStudio(id: Int): Flow<StudioDetail?>
}

class ApolloStudioRepository(
    private val apolloClient: ApolloClient,
) : StudioRepository {
    override fun observeStudio(id: Int): Flow<StudioDetail?> = flow {
        val response = apolloClient.query(StudioDetailQuery(id = id)).execute()
        val studio = response.data?.Studio
        emit(studio?.toDetail())
    }

    private fun StudioDetailQuery.Studio.toDetail() = StudioDetail(
        id = id.toLong(),
        name = name,
        isAnimationStudio = isAnimationStudio,
        isFavourite = isFavourite,
        siteUrl = siteUrl,
        media = media?.nodes?.filterNotNull()?.map {
            StudioDetail.MediaAppearance(
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
