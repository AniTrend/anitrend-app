package com.mxt.anitrend.base.custom.viewmodel

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.async.RequestHandler
import com.mxt.anitrend.base.interfaces.event.ResponseCallback
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.apiError
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/10/14.
 * View model abstraction contains the generic data model
 *
 * @deprecated Use direct [androidx.lifecycle.ViewModel] subclasses with [StateFlow] instead.
 * Proven replacements in this repo:
 * - [com.mxt.anitrend.viewmodel.LoggingViewModel] for local-activity pattern
 * - [com.mxt.anitrend.viewmodel.StudioViewModel] for API-backed activity pattern
 * - [com.mxt.anitrend.viewmodel.StaffOverviewViewModel] and
 *   [com.mxt.anitrend.viewmodel.StudioMediaViewModel] for fragment-side patterns
 */
@Deprecated(
    "Use direct ViewModel with StateFlow. See LoggingViewModel, StudioViewModel, " +
        "StaffOverviewViewModel, StudioMediaViewModel for proven patterns.",
    level = DeprecationLevel.WARNING,
)
class ViewModelBase<T> :
    ViewModel(),
    RetroCallback<T> {
    val model = MutableLiveData<T?>()

    var state: ResponseCallback? = null

    private var mLoader: RequestHandler<T>? = null

    private var emptyMessage: String = "No data available"
    private var errorMessage: String = "Request failed"
    private var tokenMessage: String = "Authentication token is invalid"

    val params = Bundle()

    /**
     * @return A live data snapshot value [T]? at the time this function is invoked
     */
    fun snapshot(): T? = model.value

    fun setContext(context: Context) {
        emptyMessage = context.getString(R.string.layout_empty_response)
        errorMessage = context.getString(R.string.text_error_request)
        tokenMessage = context.getString(R.string.text_error_auth_token)
    }

    /**
     * Template to make requests for various data types from api, the
     * <br></br>
     * @param request_type the type of request to execute
     */
    fun requestData(
        @KeyUtil.RequestType request_type: Int,
        context: Context,
    ) {
        mLoader =
            RequestHandler(params, this, request_type).also {
                it.execute(context)
            }
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     *
     *
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        mLoader?.cancel()
        mLoader = null
        state = null
        super.onCleared()
    }

    /**
     * Invoked for a received HTTP response.
     *
     *
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call [Response.isSuccessful] to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    override fun onResponse(
        call: Call<T>,
        response: Response<T>,
    ) {
        val container: T? = response.body()
        if (response.isSuccessful && container != null) {
            model.setValue(container)
        } else {
            val error = response.apiError()
            // Hacky fix that I'm ashamed of
            if (response.code() == 400 && error.contains("Invalid token")) {
                state?.showError(tokenMessage)
            } else if (response.code() == 401) {
                state?.showError(tokenMessage)
            } else {
                state?.showError(error)
            }
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    override fun onFailure(
        call: Call<T>,
        throwable: Throwable,
    ) {
        state?.showEmpty(throwable.message ?: errorMessage)
        Timber.e(throwable)
    }
}
