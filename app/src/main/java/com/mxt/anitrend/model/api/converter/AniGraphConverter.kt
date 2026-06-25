package com.mxt.anitrend.model.api.converter

import co.anitrend.retrofit.graphql.converter.GraphConverter
import co.anitrend.retrofit.graphql.model.GraphQLDocumentRegistry
import com.google.gson.Gson
import com.mxt.anitrend.model.api.converter.response.AniGraphResponseConverter
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class AniGraphConverter(
    private val gson: Gson,
    registry: GraphQLDocumentRegistry,
) : Converter.Factory() {
    private val delegate: GraphConverter = GraphConverter.create(
        gson = gson,
        registry = registry,
    )

    /**
     * Response body converter delegates logic processing to a child class that handles
     * wrapping and deserialization of the json response data.
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
    ): Converter<ResponseBody, *> = AniGraphResponseConverter<Any>(type, gson)

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? = delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
}
