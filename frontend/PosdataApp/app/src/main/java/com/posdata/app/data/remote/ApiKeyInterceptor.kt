package com.posdata.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that attaches an API key to every outgoing request.
 *
 * Adds the key as an `x-api-key` header, which is the authentication
 * mechanism required by the cloud API service.
 *
 * @param apiKey API key to include in each request.
 */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    /**
     * Intercepts the request, adds the authentication header, and proceeds with the chain.
     *
     * @param chain The interceptor chain provided by OkHttp.
     * @return The server response after the modified request is executed.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-api-key", apiKey)
            .build()
        return chain.proceed(request)
    }
}