package com.mxt.anitrend.model.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ClientInterceptor(private val agent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
            .header(ACCEPT, ACCEPT_TYPE)
            .header(ACCEPT_LANGUAGE, "*")
            .header(ACCEPT_ENCODING, "*")
            .header(USER_AGENT, agent)
            .header(CONNECTION, "keep-alive")
            .header(HOST, request.url.host)

        if (request.method == "POST") {
            val contentLength = request.body?.contentLength() ?: 0
            builder.header(CONTENT_LENGTH, contentLength.toString())
        }

        return chain.proceed(builder.build())
    }

    private companion object {
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val CONNECTION = "Connection"
        const val USER_AGENT = "User-Agent"
        const val HOST = "Host"

        private const val CONTENT_LENGTH = "Content-Length"
        private const val ACCEPT = "Accept"

        private const val ACCEPT_TYPE = "application/json"
    }
}