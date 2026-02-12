package com.posdata.app.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val LOCAL_URL = "http://10.0.2.2:3000/"
    private const val CLOUD_URL = "miurl"

    val localInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(LOCAL_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val cloudInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(CLOUD_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}