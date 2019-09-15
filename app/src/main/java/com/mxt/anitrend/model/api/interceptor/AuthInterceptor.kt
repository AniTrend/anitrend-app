package com.mxt.anitrend.model.api.interceptor

import android.content.Context
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.util.Settings
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * Created by max on 2017/06/14.
 * Auth injector interceptor
 */

class AuthInterceptor(
    private val settings: Settings
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (settings.isAuthenticated) {
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
