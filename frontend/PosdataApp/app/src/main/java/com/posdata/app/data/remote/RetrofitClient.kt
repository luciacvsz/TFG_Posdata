package com.posdata.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient


object RetrofitClient {
    private const val LOCAL_URL = "http://10.0.2.2:3000/"
    private const val CLOUD_URL = "https://w82baca113.execute-api.eu-west-3.amazonaws.com/test/"

    lateinit var apiKey: String

    val localInstance: LocalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(LOCAL_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocalApiService::class.java)
    }

    val cloudInstance: CloudApiService by lazy {
        if (!::apiKey.isInitialized) throw IllegalStateException("API key not initialized!")

        val client = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey))
            .build()

        Retrofit.Builder()
            .baseUrl(CLOUD_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudApiService::class.java)
    }
}