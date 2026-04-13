package com.posdata.app.data.remote

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.Properties

/**
 * Singleton responsible for creating and providing the Retrofit client instances
 * used to communicate with the local and cloud API services.
 *
 * Must be initialized once at application startup by calling [init] with a valid [Context]
 * before accessing [localInstance] or [cloudInstance].
 */
object RetrofitClient {
    private lateinit var _localInstance: LocalApiService
    private lateinit var _cloudInstance: CloudApiService

    /** Retrofit client for the local API service. */
    val localInstance: LocalApiService
        get() {
            check(::_localInstance.isInitialized) {
                "RetrofitClient not initialized. Call RetrofitClient.init(context) first."
            }
            return _localInstance
        }

    /** Retrofit client for the cloud API service. */
    val cloudInstance: CloudApiService
        get() {
            check(::_cloudInstance.isInitialized) {
                "RetrofitClient not initialized. Call RetrofitClient.init(context) first."
            }
            return _cloudInstance
        }

    /**
     * Initializes both API client instances.
     *
     * Reads all required configuration values (API key ad base URLs) from the
     * [local.env][android.content.res.AssetManager] file in a single pass,
     * then builds the corresponding Retrofit instances.
     *
     * Must be called once in [android.app.Application.onCreate] before any
     * network operation is performed.
     *
     * @param context Application context used to access the assets directory.
     * @throws IllegalStateException if any required property is missing from local.env.
     */
    fun init(context: Context) {
        val props = loadEnvProperties(context)

        val apiKey   = props.getProperty("API_KEY")   ?: error("API_KEY not found in local.env")
        val localUrl = props.getProperty("LOCAL_URL")  ?: error("LOCAL_URL not found in local.env")
        val cloudUrl = props.getProperty("CLOUD_URL")  ?: error("CLOUD_URL not found in local.env")

        _localInstance = buildLocalInstance(localUrl)
        _cloudInstance = buildCloudInstance(cloudUrl, apiKey)
    }

    /**
     * Reads and parses the local.env file from the app's assets directory.
     *
     * @param context Application context used to access the assets directory.
     * @return [Properties] object containing all key-value pairs from the file.
     */
    private fun loadEnvProperties(context: Context): Properties {
        val props = Properties()
        context.assets.open("local.env").use { props.load(it) }
        return props
    }

    /**
     * Builds a Retrofit instance for the local API service.
     *
     * The local client does not require authentication.
     *
     * @param baseUrl Base URL of the local server.
     */
    private fun buildLocalInstance(baseUrl: String): LocalApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocalApiService::class.java)
    }

    /**
     * Builds a Retrofit instance for the cloud API service.
     *
     * Attaches an [ApiKeyInterceptor] to the underlying [OkHttpClient] so that
     * every request to the cloud includes the required authentication header.
     *
     * @param baseUrl Base URL of the cloud server.
     * @param apiKey API key injected into each request via [ApiKeyInterceptor].
     */
    private fun buildCloudInstance(baseUrl: String, apiKey: String): CloudApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudApiService::class.java)
    }
}