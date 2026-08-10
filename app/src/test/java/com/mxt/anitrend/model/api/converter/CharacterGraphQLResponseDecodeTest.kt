package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLTransportCodec
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CharacterGraphQLResponseDecodeTest {

    private val codec = KotlinxGraphQLTransportCodec()

    @Test
    fun `decodes character base data`() {
        val response = codec.decodeResponse<GraphQLResponse<CharacterBaseData>>(
            """{"data":{"Character":{"id":1,"name":{"first":"Spike","last":"Spiegel","native":"スパイク・スピーゲル","alternative":["Spikey"]},"image":{"large":"large.jpg","medium":"medium.jpg"},"isFavourite":true,"siteUrl":"https://anilist.co/character/1"}}}""",
            graphQLResponseType(CharacterBaseData::class.java),
        )

        val character = (response.data as GraphQLData.Present<*>).value as CharacterBaseData
        assertEquals(1, character.character?.id)
        assertEquals("Spike", character.character?.name?.first)
        assertEquals("Spiegel", character.character?.name?.last)
        assertEquals("large.jpg", character.character?.image?.large)
        assertEquals(true, character.character?.isFavourite)
        assertEquals("https://anilist.co/character/1", character.character?.siteUrl)
    }

    @Test
    fun `decodes character overview data`() {
        val response = codec.decodeResponse<GraphQLResponse<CharacterOverviewData>>(
            """{"data":{"Character":{"id":1,"name":{"first":"Spike","last":"Spiegel","native":"スパイク・スピーゲル","alternative":["Spikey"]},"image":{"large":"large.jpg","medium":"medium.jpg"},"isFavourite":false,"siteUrl":"https://anilist.co/character/1","description":"Space cowboy"}}}""",
            graphQLResponseType(CharacterOverviewData::class.java),
        )

        val character = (response.data as GraphQLData.Present<*>).value as CharacterOverviewData
        assertEquals(1, character.character?.id)
        assertEquals("Space cowboy", character.character?.description)
        assertEquals("Spike", character.character?.name?.first)
        assertEquals("medium.jpg", character.character?.image?.medium)
        assertEquals(false, character.character?.isFavourite)
    }

    @Test
    fun `decodes character errors`() {
        val response = codec.decodeResponse<GraphQLResponse<CharacterBaseData>>(
            """{"errors":[{"message":"Character failed"}]}""",
            graphQLResponseType(CharacterBaseData::class.java),
        )

        assertTrue(response.data is GraphQLData.Absent)
        assertEquals("Character failed", response.errors?.firstOrNull()?.message)
    }

    private fun graphQLResponseType(typeArgument: Type): ParameterizedType = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(typeArgument)

        override fun getRawType(): Type = GraphQLResponse::class.java

        override fun getOwnerType(): Type? = null
    }
}
