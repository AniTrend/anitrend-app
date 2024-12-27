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
                errors?.firstOrNull()?.message ?: "Unexpected HTTP/${code()} from server"
            } else {
                val waitPeriod = headers.get(Retry_After) ?: 60
                "Too many requests, please retry after $waitPeriod seconds"
            }
        }
    } catch (e: Exception) {
        Timber.e(e)
        return "Unable to recover from encountered error"
    }

    return "Unable to provide information regarding error"
}

