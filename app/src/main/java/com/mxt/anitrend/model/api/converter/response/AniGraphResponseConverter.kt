package com.mxt.anitrend.model.api.converter.response

import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import java.lang.reflect.Type

class AniGraphResponseConverter<T>(
    private val type: Type,
    private val gson: Gson,
) : Converter<ResponseBody, T> {
    override fun convert(responseBody: ResponseBody): T? =
        responseBody.use {
            gson.fromJson(it.string(), type)
        }
}
