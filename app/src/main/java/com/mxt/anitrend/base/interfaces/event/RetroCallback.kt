package com.mxt.anitrend.base.interfaces.event

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by max on 2017/10/14.
 * Annotated extension of retrofit callbacks
 */
interface RetroCallback<T> : Callback<T> {
    /**
     * Invoked for a received HTTP response.
     *
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call [Response.isSuccessful] to determine if the response indicates success.
     *
     * @param call the origination requesting object
     * @param response the response from the network
     */
    override fun onResponse(call: Call<T>, response: Response<T>)

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call the origination requesting object
     * @param throwable contains information about the error
     */
    override fun onFailure(call: Call<T>, throwable: Throwable)
}
