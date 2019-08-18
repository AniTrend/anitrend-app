package com.mxt.anitrend.util.graphql

import io.github.wax911.library.util.getError
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/06/15.
 * ResponseError utility class
 */

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
fun Response<*>?.apiError(): String {
    try {
        if (this != null) {
            val headers = headers()
            val errors = getError()
            return if (code() != HTTP_LIMIT_REACHED) {
                errors?.firstOrNull()?.message ?: "Unable to provide information regarding error!"
            } else
                "${headers.get(RateLimit_Remaining)} of ${headers.get(RateLimit_Limit)} requests remaining, please retry after ${headers.get(Retry_After)} seconds"
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        Timber.tag(TAG).e(ex)
        return "Unexpected error encountered"
    }

    return "Unable to provide information regarding error!"
}

