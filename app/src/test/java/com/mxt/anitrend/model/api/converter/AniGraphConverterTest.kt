package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.model.EmptyGraphQLVariables
import co.anitrend.retrofit.graphql.model.GraphQLRequest
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLJson
import com.google.gson.GsonBuilder
import com.mxt.anitrend.graphql.generated.GeneratedGraphQLRegistry
import com.mxt.anitrend.graphql.generated.GenreCollection
import com.mxt.anitrend.graphql.generated.GenreCollectionData
import com.mxt.anitrend.graphql.generated.LikeableType
import com.mxt.anitrend.graphql.generated.MediaTagCollectionData
import com.mxt.anitrend.graphql.generated.ToggleLike
import com.mxt.anitrend.graphql.generated.ToggleLikeVariables
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class AniGraphConverterTest {

    private val converter = AniGraphConverter(
        gson = GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient()
            .create(),
        json = KotlinxGraphQLJson(),
        registry = GeneratedGraphQLRegistry,
    )

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/")
        .build()

    @Test
    fun `routes legacy AniListContainer responses through Gson`() {
        val responseConverter = converter.responseBodyConverter(
            type = parameterizedType(AniListContainer::class.java, String::class.java),
            annotations = emptyArray(),
            retrofit = retrofit,
        )

        val response = responseConverter.convert(
            """{"data":{"Page":"Legacy"}}"""
                .toResponseBody("application/json".toMediaType()),
        ) as AniListContainer<*>

        assertEquals("Legacy", response.data?.result)
    }

    @Test
    fun `routes generated GraphContainer responses through Kotlinx`() {
        val responseConverter = converter.responseBodyConverter(
            type = parameterizedType(GraphContainer::class.java, GenreCollectionData::class.java),
            annotations = emptyArray(),
            retrofit = retrofit,
        )

        val response = responseConverter.convert(
            """{"data":{"GenreCollection":["Action","Comedy"]}}"""
                .toResponseBody("application/json".toMediaType()),
        ) as GraphContainer<*>

        val data = response.data as GenreCollectionData
        assertEquals(listOf("Action", "Comedy"), data.genreCollection)
    }

    @Test
    fun `routes generated media tag GraphContainer responses through Kotlinx`() {
        val responseConverter = converter.responseBodyConverter(
            type = parameterizedType(GraphContainer::class.java, MediaTagCollectionData::class.java),
            annotations = emptyArray(),
            retrofit = retrofit,
        )

        val response = responseConverter.convert(
            """{"data":{"MediaTagCollection":[{"id":1,"name":"Cyberpunk","description":null,"category":"Theme","rank":null,"isGeneralSpoiler":null,"isAdult":false}]}}"""
                .toResponseBody("application/json".toMediaType()),
        ) as GraphContainer<*>

        val data = response.data as MediaTagCollectionData
        val tag = data.mediaTagCollection?.single()
        assertEquals(1, tag?.id)
        assertEquals("Cyberpunk", tag?.name)
        assertEquals("Theme", tag?.category)
        assertEquals(false, tag?.isAdult)
    }

    @Test
    fun `serializes generated GraphQLOperationRequest bodies through toggleLike`() {
        val requestBody = serializedBody(
            request = ToggleLike.request(id = 1, type = LikeableType.ACTIVITY),
            type = parameterizedType(GraphQLOperationRequest::class.java, ToggleLikeVariables::class.java),
        )

        val body = jsonObject(requestBody)

        assertEquals("ToggleLike", body["operationName"]?.jsonPrimitive?.content)
        assertTrue(body["query"]?.jsonPrimitive?.content?.contains("ToggleLike") == true)
        val variables = body["variables"]?.jsonObject
        assertEquals("1", variables?.get("id")?.jsonPrimitive?.content)
        assertEquals("ACTIVITY", variables?.get("type")?.jsonPrimitive?.content)
    }

    @Test
    fun `preserves manual compat GraphQLRequest serialization through getGenres`() {
        val requestBody = serializedBody(
            request = GraphQLRequest<EmptyGraphQLVariables>(
                query = GenreCollection.document,
                operationName = GenreCollection.name,
            ),
            type = parameterizedType(GraphQLRequest::class.java, EmptyGraphQLVariables::class.java),
        )

        val body = jsonObject(requestBody)

        assertEquals("GenreCollection", body["operationName"]?.jsonPrimitive?.content)
        assertTrue(body["query"]?.jsonPrimitive?.content?.contains("query GenreCollection") == true)
    }

    private fun serializedBody(request: Any, type: Type): RequestBody {
        val converter = converter.requestBodyConverter(
            type = type,
            parameterAnnotations = emptyArray(),
            methodAnnotations = emptyArray(),
            retrofit = retrofit,
        ) as Converter<Any, RequestBody>
        return converter.convert(request) ?: error("Expected a serialized request body")
    }

    private fun jsonObject(body: RequestBody): JsonObject {
        val buffer = Buffer()
        body.writeTo(buffer)
        return Json.parseToJsonElement(buffer.readUtf8()).jsonObject
    }

    private fun parameterizedType(
        rawType: Type,
        vararg typeArguments: Type,
    ): ParameterizedType = object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(*typeArguments)

        override fun getRawType(): Type = rawType

        override fun getOwnerType(): Type? = null
    }
}
