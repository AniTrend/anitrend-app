package com.mxt.anitrend.base.custom.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle

import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.async.RequestHandler
import com.mxt.anitrend.base.interfaces.event.ResponseCallback
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.util.ErrorUtil
import com.mxt.anitrend.util.KeyUtil

import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.query.Query
import retrofit2.Call
import retrofit2.Response

/**
 * Created by max on 2017/10/14.
 * View model abstraction contains the generic data model
 */

class ViewModelBase<T>: ViewModel(), RetroCallback<T> {

    val model by lazy {
        MutableLiveData<T>()
    }

    var state: ResponseCallback? = null

    private var mLoader: RequestHandler<T>? = null

    private var emptyMessage: String? = null
    private var errorMessage: String? = null

    val params by lazy {
        Bundle()
    }

    fun setContext(context: Context?) {
        context?.apply {
            emptyMessage = getString(R.string.layout_empty_response)
            errorMessage = getString(R.string.text_error_request)
        }
    }

    /**
     * Template to make requests for various data types from api, the
     * <br></br>
     * @param request_type the type of request to execute
     */
    fun requestData(@KeyUtil.RequestType request_type: Int, context: Context) {
        mLoader = RequestHandler(params, this, request_type)
        mLoader?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context)
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     *
     *
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        if (mLoader?.status != AsyncTask.Status.FINISHED)
            mLoader?.cancel(true)
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
    override fun onResponse(call: Call<T>, response: Response<T>) {
        val container: T? = response.body()
        if (response.isSuccessful && container != null)
            model.setValue(container)
        else
            state?.showError(ErrorUtil.getError(response))
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    override fun onFailure(call: Call<T>, throwable: Throwable) {
        state?.showEmpty(throwable.message)
        throwable.printStackTrace()
    }
}
