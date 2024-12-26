package com.mxt.anitrend.model.api.converter

import com.google.gson.Gson
import com.mxt.anitrend.model.api.converter.response.AniGraphResponseConverter
import io.github.wax911.library.annotation.processor.contract.AbstractGraphProcessor
import io.github.wax911.library.converter.GraphConverter
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class AniGraphConverter(
    graphProcessor: AbstractGraphProcessor,
    gson: Gson
) : GraphConverter(graphProcessor, gson) {
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
        retrofit: Retrofit
    ): Converter<ResponseBody, *> = AniGraphResponseConverter<Any>(type, gson)
}