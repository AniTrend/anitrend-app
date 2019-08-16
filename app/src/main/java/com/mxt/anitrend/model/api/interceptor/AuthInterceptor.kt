package com.mxt.anitrend.model.api.interceptor

import android.content.Context
import android.util.Log

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.util.Settings

import java.io.IOException

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * Created by max on 2017/06/14.
 * Auth injector interceptor
 */

class AuthInterceptor(context: Context) : Interceptor {

    private val applicationPref by lazy {
        Settings(context)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (applicationPref.isAuthenticated) {
            if (WebTokenRequest.getInstance() != null) {
                val builder = chain.request().newBuilder()
                        .header(BuildConfig.HEADER_KEY, WebTokenRequest.getInstance().header)
                val request = builder.build()
                return chain.proceed(request)
            } else
                Timber.tag("AuthInterceptor").e("Authentication reference is null, this should not happen under normal conditions")
        }
        return chain.proceed(chain.request())
    }
}
