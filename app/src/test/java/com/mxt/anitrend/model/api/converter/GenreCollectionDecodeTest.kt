package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLJson
import com.mxt.anitrend.graphql.generated.GenreCollectionData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class GenreCollectionDecodeTest {

    private val type = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(GenreCollectionData::class.java)

        override fun getRawType(): Type = GraphContainer::class.java

        override fun getOwnerType(): Type? = null
    }

    private val json = KotlinxGraphQLJson()

    @Test
    fun `decodes genre collection data`() {
        val container = json.decode<GraphContainer<GenreCollectionData>>(
            """{"data":{"GenreCollection":["Action","Comedy"]}}""",
            type,
        )

        assertEquals(listOf("Action", "Comedy"), container.data?.genreCollection)
    }

    @Test
    fun `decodes genre collection errors`() {
        val container = json.decode<GraphContainer<GenreCollectionData>>(
            """{"errors":[{"message":"Boom"}]}""",
            type,
        )

        assertNull(container.data)
        assertEquals("Boom", container.errors?.firstOrNull()?.message)
    }
}
