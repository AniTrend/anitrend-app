package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.GraphQLData
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLTransportCodec
import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MediaTagCollectionDecodeTest {

    private val type = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(MediaTagCollectionData::class.java)

        override fun getRawType(): Type = GraphQLResponse::class.java

        override fun getOwnerType(): Type? = null
    }

    private val codec = KotlinxGraphQLTransportCodec()

    @Test
    fun `decodes media tag collection data`() {
        val response = codec.decodeResponse<GraphQLResponse<MediaTagCollectionData>>(
            """{"data":{"MediaTagCollection":[{"id":1,"name":"Cyberpunk","description":"Future tech","category":"Theme","rank":80,"isGeneralSpoiler":true,"isAdult":false}]}}""",
            type,
        )

        val data = (response.data as GraphQLData.Present<*>).value as MediaTagCollectionData
        val tag = data.mediaTagCollection?.firstOrNull()
        assertEquals(1, tag?.id)
        assertEquals("Cyberpunk", tag?.name)
        assertEquals("Future tech", tag?.description)
        assertEquals("Theme", tag?.category)
        assertEquals(80, tag?.rank)
        assertEquals(true, tag?.isGeneralSpoiler)
        assertEquals(false, tag?.isAdult)
    }

    @Test
    fun `decodes nullable fields and null list elements`() {
        val response = codec.decodeResponse<GraphQLResponse<MediaTagCollectionData>>(
            """{"data":{"MediaTagCollection":[null,{"id":2,"name":"Robots","description":null,"category":null,"rank":null,"isGeneralSpoiler":null,"isAdult":null}]}}""",
            type,
        )

        val data = (response.data as GraphQLData.Present<*>).value as MediaTagCollectionData
        val tags = data.mediaTagCollection
        assertNull(tags?.firstOrNull())
        assertEquals(2, tags?.get(1)?.id)
        assertEquals("Robots", tags?.get(1)?.name)
        assertNull(tags?.get(1)?.description)
        assertNull(tags?.get(1)?.category)
        assertNull(tags?.get(1)?.rank)
        assertNull(tags?.get(1)?.isGeneralSpoiler)
        assertNull(tags?.get(1)?.isAdult)
    }

    @Test
    fun `decodes media tag collection errors`() {
        val response = codec.decodeResponse<GraphQLResponse<MediaTagCollectionData>>(
            """{"errors":[{"message":"Boom"}]}""",
            type,
        )

        assertTrue(response.data is GraphQLData.Absent)
        assertEquals("Boom", response.errors?.firstOrNull()?.message)
    }
}
