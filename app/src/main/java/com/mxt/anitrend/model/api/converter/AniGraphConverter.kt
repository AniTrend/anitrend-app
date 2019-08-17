package com.mxt.anitrend.model.api.converter

import android.content.Context
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.mxt.anitrend.model.api.converter.request.AniRequestConverter
import com.mxt.anitrend.model.api.converter.response.AniGraphResponseConverter
import io.github.wax911.library.converter.GraphConverter
import io.github.wax911.library.model.request.QueryContainerBuilder
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class AniGraphConverter(
        context: Context?
) : GraphConverter(context) {

    /**
     * Response body converter delegates logic processing to a child class that handles
     * wrapping and deserialization of the json response data.
     *
     * @param parameterAnnotations All the annotation applied to request parameters
     * @param methodAnnotations All the annotation applied to the requesting method
     * @param retrofit The retrofit object representing the response
     * @param type The type of the parameter of the request
     *
     * @see AniRequestConverter
     */
    override fun requestBodyConverter(
            type: Type?,
            parameterAnnotations: Array<Annotation>,
            methodAnnotations: Array<Annotation>,
            retrofit: Retrofit?
    ): Converter<QueryContainerBuilder, RequestBody>? =
            AniRequestConverter(
                    methodAnnotations = methodAnnotations,
                    graphProcessor = graphProcessor,
                    gson = gson
            )

    /**
     * Response body converter delegates logic processing to a child class that handles
     * wrapping and deserialization of the json response data.
     * @see GraphResponseConverter
     * <br></br>
     *
     *
     * @param annotations All the annotation applied to the requesting Call method
     * @see retrofit2.Call
     *
     * @param retrofit The retrofit object representing the response
     * @param type The generic type declared on the Call method
     */
    override fun responseBodyConverter(
            type: Type?,
            annotations: Array<Annotation>,
            retrofit: Retrofit
    ): Converter<ResponseBody, *>? =
            AniGraphResponseConverter<Any>(type, gson)

    companion object {

        /**
         * Allows you to provide your own Gson configuration which will be used when serialize or
         * deserialize response and request bodies.
         *
         * @param context any valid application context
         */
        fun create(context: Context?) =
                AniGraphConverter(context).apply {
                    gson = GsonBuilder()
                            .addSerializationExclusionStrategy(object : ExclusionStrategy {
                                /**
                                 * @param clazz the class object that is under test
                                 * @return true if the class should be ignored; otherwise false
                                 */
                                override fun shouldSkipClass(clazz: Class<*>?) = false

                                /**
                                 * @param f the field object that is under test
                                 * @return true if the field should be ignored; otherwise false
                                 */
                                override fun shouldSkipField(f: FieldAttributes?): Boolean {
                                    return f?.name?.equals("operationName") ?: false
                                            ||
                                            f?.name?.equals("extensions") ?: false
                                }
                            })
                            .enableComplexMapKeySerialization()
                            .setLenient()
                            .create()
                }
    }
}