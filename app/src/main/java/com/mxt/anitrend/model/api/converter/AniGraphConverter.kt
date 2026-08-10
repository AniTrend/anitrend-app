package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.converter.GraphConverter
import co.anitrend.retrofit.graphql.converter.GraphQLConverterFactory
import co.anitrend.retrofit.graphql.model.GraphQLDocumentRegistry
import co.anitrend.retrofit.graphql.model.GraphQLJson
import co.anitrend.retrofit.graphql.model.GraphQLResponse
import co.anitrend.retrofit.graphql.model.request.GraphQLOperationRequest
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLTransportCodec
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
     * Backend-neutral delegate for generated [GraphQLOperationRequest] request bodies and
     * [GraphQLResponse] response envelopes. All other request and response types keep flowing
     * through the legacy compat [delegate] and Gson [AniGraphResponseConverter] respectively.
     */
    private val neutralDelegate: GraphQLConverterFactory = GraphQLConverterFactory.create(
        codec = KotlinxGraphQLTransportCodec(),
        registry = registry,
    )

    /**
     * Response body conversion routes generated [GraphQLResponse] envelopes through the
     * retrofit-graphql transport codec while preserving Gson deserialization for legacy
     * AniList DTO containers.
     *
     * @param annotations All the annotation applied to the requesting method
     * @param retrofit The retrofit object representing the response
     * @param type The generic type declared on the method
     *
     * @see GraphQLConverterFactory
     */
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val rawType = getRawType(type)
        return if (rawType == GraphQLResponse::class.java) {
            neutralDelegate.responseBodyConverter(type, annotations, retrofit)
                ?: error("No response converter for GraphQLResponse type $type")
        } else {
            AniGraphResponseConverter<Any>(type, gson)
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? = if (getRawType(type) == GraphQLOperationRequest::class.java) {
        neutralDelegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    } else {
        delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }
}
