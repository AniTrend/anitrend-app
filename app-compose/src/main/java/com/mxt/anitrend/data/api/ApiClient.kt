package com.mxt.anitrend.data.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    val apolloClient: ApolloClient by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        ApolloClient.Builder()
            .serverUrl("https://graphql.anilist.co")
            .okHttpClient(client)
            .build()
    }
}
