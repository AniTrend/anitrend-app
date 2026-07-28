package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLJson
import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MediaTagCollectionDecodeTest {

    private val type = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(MediaTagCollectionData::class.java)

        override fun getRawType(): Type = GraphContainer::class.java

        override fun getOwnerType(): Type? = null
    }

    private val json = KotlinxGraphQLJson()

    @Test
    fun `decodes media tag collection data`() {
        val container = json.decode<GraphContainer<MediaTagCollectionData>>(
            """{"data":{"MediaTagCollection":[{"id":1,"name":"Cyberpunk","description":"Future tech","category":"Theme","rank":80,"isGeneralSpoiler":true,"isAdult":false}]}}""",
            type,
        )

        val tag = container.data?.mediaTagCollection?.firstOrNull()
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
        val container = json.decode<GraphContainer<MediaTagCollectionData>>(
            """{"data":{"MediaTagCollection":[null,{"id":2,"name":"Robots","description":null,"category":null,"rank":null,"isGeneralSpoiler":null,"isAdult":null}]}}""",
            type,
        )

        val tags = container.data?.mediaTagCollection
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
        val container = json.decode<GraphContainer<MediaTagCollectionData>>(
            """{"errors":[{"message":"Boom"}]}""",
            type,
        )

        assertNull(container.data)
        assertEquals("Boom", container.errors?.firstOrNull()?.message)
    }
}
