package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLJson
import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CharacterGraphContainerDecodeTest {

    private val json = KotlinxGraphQLJson()

    @Test
    fun `decodes character base data`() {
        val container = json.decode<GraphContainer<CharacterBaseData>>(
            """{"data":{"Character":{"id":1,"name":{"first":"Spike","last":"Spiegel","native":"スパイク・スピーゲル","alternative":["Spikey"]},"image":{"large":"large.jpg","medium":"medium.jpg"},"isFavourite":true,"siteUrl":"https://anilist.co/character/1"}}}""",
            graphContainerType(CharacterBaseData::class.java),
        )

        val character = container.data?.character
        assertEquals(1, character?.id)
        assertEquals("Spike", character?.name?.first)
        assertEquals("Spiegel", character?.name?.last)
        assertEquals("large.jpg", character?.image?.large)
        assertEquals(true, character?.isFavourite)
        assertEquals("https://anilist.co/character/1", character?.siteUrl)
    }

    @Test
    fun `decodes character overview data`() {
        val container = json.decode<GraphContainer<CharacterOverviewData>>(
            """{"data":{"Character":{"id":1,"name":{"first":"Spike","last":"Spiegel","native":"スパイク・スピーゲル","alternative":["Spikey"]},"image":{"large":"large.jpg","medium":"medium.jpg"},"isFavourite":false,"siteUrl":"https://anilist.co/character/1","description":"Space cowboy"}}}""",
            graphContainerType(CharacterOverviewData::class.java),
        )

        val character = container.data?.character
        assertEquals(1, character?.id)
        assertEquals("Space cowboy", character?.description)
        assertEquals("Spike", character?.name?.first)
        assertEquals("medium.jpg", character?.image?.medium)
        assertEquals(false, character?.isFavourite)
    }

    @Test
    fun `decodes character errors`() {
        val container = json.decode<GraphContainer<CharacterBaseData>>(
            """{"errors":[{"message":"Character failed"}]}""",
            graphContainerType(CharacterBaseData::class.java),
        )

        assertNull(container.data)
        assertEquals("Character failed", container.errors?.firstOrNull()?.message)
    }

    private fun graphContainerType(typeArgument: Type): ParameterizedType = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(typeArgument)

        override fun getRawType(): Type = GraphContainer::class.java

        override fun getOwnerType(): Type? = null
    }
}
