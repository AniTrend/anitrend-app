package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLTransportCodec
import com.mxt.anitrend.graphql.generated.GenreCollectionData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class GenreCollectionDecodeTest {

    private val type = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(GenreCollectionData::class.java)

        override fun getRawType(): Type = GraphQLResponse::class.java

        override fun getOwnerType(): Type? = null
    }

    private val codec = KotlinxGraphQLTransportCodec()

    @Test
    fun `decodes genre collection data`() {
        val response = codec.decodeResponse<GraphQLResponse<GenreCollectionData>>(
            """{"data":{"GenreCollection":["Action","Comedy"]}}""",
            type,
        )

        val data = (response.data as GraphQLData.Present<*>).value as GenreCollectionData
        assertEquals(listOf("Action", "Comedy"), data.genreCollection)
    }

    @Test
    fun `decodes genre collection errors`() {
        val response = codec.decodeResponse<GraphQLResponse<GenreCollectionData>>(
            """{"errors":[{"message":"Boom"}]}""",
            type,
        )

        assertTrue(response.data is GraphQLData.Absent)
        assertEquals("Boom", response.errors?.firstOrNull()?.message)
    }

    @Test
    fun `decodes explicit null data as GraphQLData Present with null value`() {
        val response = codec.decodeResponse<GraphQLResponse<GenreCollectionData>>(
            """{"data":null}""",
            type,
        )

        val data = response.data
        assertTrue(data is GraphQLData.Present)
        assertNull((data as GraphQLData.Present<*>).value)
    }
}
