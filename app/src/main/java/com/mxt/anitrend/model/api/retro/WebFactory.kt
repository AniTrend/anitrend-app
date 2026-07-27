package com.mxt.anitrend.model.api.retro

import com.google.gson.GsonBuilder
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.model.api.retro.anilist.AuthModel
import com.mxt.anitrend.model.entity.anilist.WebToken
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.graphql.apiError
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by max on 2017/10/14.
 * Retrofit service factory
 */
object WebFactory {
    @JvmField
    @Suppress("DEPRECATION")
    val gson =
        GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient()
            .create()

    val API_AUTH_LINK =
        String.format(
            "%sauthorize?grant_type=%s&client_id=%s&redirect_uri=%s&response_type=%s",
            BuildConfig.API_AUTH_LINK,
            KeyUtil.AUTHENTICATION_CODE,
            BuildConfig.CLIENT_ID,
            BuildConfig.REDIRECT_URI,
            BuildConfig.RESPONSE_TYPE,
        )

    /**
     * Creates a standard HttpBuilder with most common likely used configuration and optionally
     * will include http logging based off a given log level.
     * @see HttpLoggingInterceptor.setLevel
     * @param interceptors Optional interceptors of your own implementation
     * @param logLevel Mandatory log level that the logging http interceptor should use
     */
    private fun createHttpClient(
        vararg interceptors: Interceptor,
        logLevel: HttpLoggingInterceptor.Level,
    ): OkHttpClient.Builder {
        val okHttpClientBuilder =
            OkHttpClient
                .Builder()
                .readTimeout(35, TimeUnit.SECONDS)
                .connectTimeout(35, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor =
                HttpLoggingInterceptor { Timber.v(it) }
                    .setLevel(logLevel)
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        interceptors.forEach(okHttpClientBuilder::addInterceptor)
        return okHttpClientBuilder
    }

    /**
     * Gets a new access token using the authentication code code provided from a callback
     */
    @JvmStatic
    fun requestCodeTokenSync(code: String): WebToken? = try {
        val retrofit =
            Retrofit
                .Builder()
                .client(
                    createHttpClient(
                        logLevel = HttpLoggingInterceptor.Level.HEADERS,
                    ).build(),
                ).addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(BuildConfig.API_AUTH_LINK)
                .build()
        val refreshTokenCall =
            retrofit.create(AuthModel::class.java).getAuthRequest(
                KeyUtil.AUTHENTICATION_CODE,
                BuildConfig.CLIENT_ID,
                BuildConfig.CLIENT_SECRET,
                BuildConfig.REDIRECT_URI,
                code,
            )
        val response = refreshTokenCall.execute()
        if (!response.isSuccessful) Timber.tag("requestCodeTokenSync").w(response.apiError())
        response.body()
    } catch (e: Exception) {
        Timber.tag("requestCodeTokenSync").e(e)
        null
    }

    @JvmStatic
    fun invalidate() {
        // Retrofit instances are now managed by Koin.
        // Auth state is read dynamically by AuthInterceptor. No invalidation needed.
    }
}
