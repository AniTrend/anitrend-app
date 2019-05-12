package com.mxt.anitrend.model.api.interceptor

import android.content.Context

import com.mxt.anitrend.util.CompatUtil

import java.io.IOException
import java.util.concurrent.TimeUnit

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created by max on 2017/06/14.
 * cache injector interceptor
 */

class CacheInterceptor(private val context: Context?, private val forceCache: Boolean = false) : Interceptor {

    private val cacheControl by lazy {
        CacheControl.Builder().maxStale(3, TimeUnit.HOURS).build()
    }


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (CompatUtil.isOnline(context) || forceCache) {
            val original = chain.request().newBuilder()
                    .cacheControl(cacheControl).build()
            return chain.proceed(original)
        }

        return chain.proceed(chain.request())
    }
}
