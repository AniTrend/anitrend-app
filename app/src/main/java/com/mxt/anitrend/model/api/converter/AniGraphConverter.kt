package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.converter.GraphConverter
import co.anitrend.retrofit.graphql.converter.response.GraphResponseConverter
import co.anitrend.retrofit.graphql.model.GraphQLDocumentRegistry
import co.anitrend.retrofit.graphql.model.GraphQLJson
import co.anitrend.retrofit.graphql.model.body.GraphContainer
import com.google.gson.Gson
import com.mxt.anitrend.model.api.converter.response.AniGraphResponseConverter
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class AniGraphConverter(
    private val gson: Gson,
    private val json: GraphQLJson,
    registry: GraphQLDocumentRegistry,
) : Converter.Factory() {
    private val delegate: GraphConverter = GraphConverter.create(
        json = json,
        registry = registry,
    )

    /**
     * Response body conversion routes generated GraphQL containers through the retrofit-graphql
     * JSON adapter while preserving Gson deserialization for legacy AniList DTO containers.
     *
     * @param annotations All the annotation applied to the requesting method
     * @param retrofit The retrofit object representing the response
     * @param type The generic type declared on the method
     *
     * @see GraphResponseConverter
     */
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val rawType = getRawType(type)
        return if (rawType == GraphContainer::class.java) {
            GraphResponseConverter<Any>(type, json)
        } else {
            AniGraphResponseConverter<Any>(type, gson)
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? = delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
}
