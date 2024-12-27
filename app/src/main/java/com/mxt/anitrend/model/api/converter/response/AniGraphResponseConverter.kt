package com.mxt.anitrend.model.api.converter.response

import com.google.gson.Gson
import com.mxt.anitrend.model.entity.container.body.AniListContainer
import io.github.wax911.library.converter.response.GraphResponseConverter
import okhttp3.ResponseBody
import timber.log.Timber
import java.lang.reflect.Type

class AniGraphResponseConverter<T>(
        type: Type?,
        gson: Gson
) : GraphResponseConverter<T>(type, gson) {

    /**
     * Converter contains logic on how to handle responses, since GraphQL responses follow
     * the JsonAPI spec it makes sense to wrap our base query response data and errors response
     * in here, the logic remains open to the implementation
     * <br></br>
     *
     * @param responseBody The retrofit response body received from the network
     * @return The type declared in the Call of the request
     */
    override fun convert(responseBody: ResponseBody): T? {
        var targetResult: T? = null
        var jsonResponse: String? = null
        try {
            responseBody.use {
                jsonResponse = it.string()
            }
            val container = gson.fromJson<AniListContainer<T?>>(
                    jsonResponse, type
            )
            if (container?.data != null) {
                val dataContainer = container.data
                targetResult = dataContainer.result
            } else
                container?.errors?.forEach {
                    Timber.e(it.message)
                }
        } catch (e: Exception) {
            Timber.e(e, jsonResponse?:"Json response is null")
        }
        return targetResult
    }
}