package com.mxt.anitrend.data.character

import com.apollographql.apollo.ApolloClient
import com.mxt.anitrend.data.graphql.CharacterDetailQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class CharacterDetail(
    val id: Long,
    val name: String,
    val imageLarge: String?,
    val imageMedium: String?,
    val description: String?,
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

interface CharacterRepository {
    fun observeCharacter(id: Int): Flow<CharacterDetail?>
}

class ApolloCharacterRepository(
    private val apolloClient: ApolloClient,
) : CharacterRepository {
    override fun observeCharacter(id: Int): Flow<CharacterDetail?> = flow {
        val response = apolloClient.query(CharacterDetailQuery(id = id)).execute()
        val char = response.data?.Character
        emit(char?.toDetail())
    }

    private fun CharacterDetailQuery.Character.toDetail() = CharacterDetail(
        id = id.toLong(),
        name = name?.full ?: "Unknown",
        imageLarge = image?.large,
        imageMedium = image?.medium,
        description = description,
        isFavourite = isFavourite ?: false,
        siteUrl = siteUrl,
        media = media?.nodes?.filterNotNull()?.map {
            CharacterDetail.MediaAppearance(
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
