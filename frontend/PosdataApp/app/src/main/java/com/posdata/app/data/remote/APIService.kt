package com.posdata.app.data.remote

import android.content.Context
import com.posdata.app.data.remote.request.CloudPreferencesPATCHRequest
import com.posdata.app.data.remote.request.LocalLoginPOSTRequest
import com.posdata.app.data.remote.response.LocalLoginPOSTResponse
import com.posdata.app.data.remote.response.LocalUserGETResponse
import com.posdata.app.data.remote.request.LocalUserPOSTRequest
import com.posdata.app.data.remote.response.LocalUserPOSTResponse
import com.posdata.app.data.remote.response.CloudUsersGETResponse
import com.posdata.app.data.remote.request.CloudProfilePATCHRequest
import com.posdata.app.data.remote.request.LocalUserPATCHRequest
import com.posdata.app.data.remote.request.CloudSMSPOSTRequest
import com.posdata.app.data.remote.request.CloudTrustedContactsPATCHRequest
import com.posdata.app.data.remote.request.CloudUsersPOSTRequest
import com.posdata.app.data.remote.response.CloudSMSGETResponse
import com.posdata.app.data.remote.response.LocalUserDELETEResponse
import com.posdata.app.data.remote.response.LocalUserPATCHResponse
import com.posdata.app.data.remote.response.CloudSMSPOSTResponse
import com.posdata.app.data.remote.response.CloudUsersPOSTResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.Properties

fun loadApiKey(context: Context): String {
    val props = Properties()
    context.assets.open("local.env").use { props.load(it) }
    return props.getProperty("API_KEY") ?: "DUMMY_KEY"
}

interface LocalApiService {
    @DELETE("/users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: String): Response<LocalUserDELETEResponse>
    @GET("/users/{email}")
    suspend fun getUser(@Path("email") email: String): Response<LocalUserGETResponse>
    @PATCH("/users/{user_id}")
    suspend fun patchUser(@Path("user_id") userId: String, @Body request: LocalUserPATCHRequest): Response<LocalUserPATCHResponse>
    @POST("/login")
    suspend fun postLogin(@Body request: LocalLoginPOSTRequest): Response<LocalLoginPOSTResponse>
    @POST("/users/{user_id}")
    suspend fun postUser(@Path("user_id") userId: String, @Body request: LocalUserPOSTRequest): Response<LocalUserPOSTResponse>
}

interface CloudApiService {
    @DELETE("users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: String): Response<Unit>
    @GET("sms/{user_id}/{execution_id}")
    suspend fun getSMS(@Path("user_id") userId: String, @Path("execution_id") executionId: String): Response<CloudSMSGETResponse>
    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String): Response<CloudUsersGETResponse>
    @PATCH("users/profile/{user_id}")
    suspend fun patchProfile(@Path("user_id") userId: String, @Body request: CloudProfilePATCHRequest): Response<Unit>
    @PATCH("users/trusted-contacts/{user_id}")
    suspend fun patchTrustedContacts(@Path("user_id") userId: String, @Body request: CloudTrustedContactsPATCHRequest): Response<Unit>
    @PATCH("users/preferences/{user_id}")
    suspend fun patchPreferences(@Path("user_id") userId: String, @Body request: CloudPreferencesPATCHRequest): Response<Unit>
    @POST("sms/{user_id}")
    suspend fun postSMS(@Path("user_id") userId: String, @Body request: CloudSMSPOSTRequest): Response<CloudSMSPOSTResponse>
    @POST("users")
    suspend fun postUser(@Body request: CloudUsersPOSTRequest): Response<CloudUsersPOSTResponse>
}