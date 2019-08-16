package com.mxt.anitrend.model.api.interceptor

import android.content.Context
import com.mxt.anitrend.util.CompatUtil
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by max on 2017/07/17.
 * Network cache injector interceptor
 */

class NetworkCacheInterceptor(private val context: Context?, private val forceCache: Boolean = false) : Interceptor {

    // re-write response header to force use of cache
    private val cacheControl by lazy {
        CacheControl.Builder()
                .maxAge(4, TimeUnit.HOURS)
                .build()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        return if (!CompatUtil.isOnline(context) || !forceCache) response.newBuilder().header(CACHE_CONTROL,
                cacheControl.toString()).build() else response
    }

    companion object {
        private const val CACHE_CONTROL = "Cache-Control"
    }
}
