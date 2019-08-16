package com.mxt.anitrend.util

import com.google.gson.reflect.TypeToken
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.container.body.GraphContainer
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/06/15.
 * ResponseError utility class
 */

object ErrorUtil {

    private const val HTTP_LIMIT_REACHED = 429

    private const val TAG = "ErrorUtil"
    private const val Retry_After = "Retry-After"
    private const val RateLimit_Limit = "X-RateLimit-Limit"
    private const val RateLimit_Remaining = "X-RateLimit-Remaining"

    /**
     * Converts the response error response into an object.
     *
     * @return The error object, or null if an exception was encountered
     * @see Error
     */
    fun getError(response: Response<*>?): String {
        try {
            if (response != null) {
                val headers = response.headers()
                val responseBody = response.errorBody()
                val message = responseBody?.string()
                var error = getGraphQLError(message)
                if (response.code() != HTTP_LIMIT_REACHED) {
                    if (responseBody != null && !message.isNullOrBlank())
                        if (!error.isNullOrBlank())
                            return error
                } else {
                    error = String.format("%s of %s requests remaining, please retry after %s seconds",
                            headers.get(RateLimit_Remaining), headers.get(RateLimit_Limit), headers.get(Retry_After))
                    return error
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return "Unexpected error encountered"
        }

        return "Unable to provide information regarding error!"
    }

    private fun getGraphQLError(errorJson: String?): String? {
        return errorJson?.let {
            Timber.tag(TAG).e(it)
            val tokenType = object : TypeToken<GraphContainer<*>>() {}.type
            val graphContainer = WebFactory.gson.fromJson<GraphContainer<*>>(it, tokenType)
            val errors = graphContainer.errors
            if (!CompatUtil.isEmpty(errors)) {
                val builder = StringBuilder()
                for (error in errors)
                    builder.append(error.toString())
                return@let builder.toString()
            }
            null
        }
    }
}
