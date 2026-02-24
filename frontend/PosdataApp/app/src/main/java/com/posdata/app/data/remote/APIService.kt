package com.posdata.app.data.remote

import android.content.Context
import com.posdata.app.data.remote.request.LoginPOSTRequest
import com.posdata.app.data.remote.response.LoginPOSTResponse
import com.posdata.app.data.remote.request.CheckEmailPOSTRequest
import com.posdata.app.data.remote.request.TrustedContactsPATCHRequest
import com.posdata.app.data.remote.response.CheckEmailPOSTResponse
import com.posdata.app.data.remote.request.RegisterPOSTRequest
import com.posdata.app.data.remote.response.RegisterPOSTResponse
import com.posdata.app.data.remote.response.UserGETResponse
import com.posdata.app.data.remote.request.CloudProfilePATCHRequest
import com.posdata.app.data.remote.request.LocalProfilePATCHRequest
import com.posdata.app.data.remote.request.PreferencesPATCHRequest
import com.posdata.app.data.remote.request.SMSPOSTRequest
import com.posdata.app.data.remote.request.UserPOSTRequest
import com.posdata.app.data.remote.response.LocalUserDELETEResponse
import com.posdata.app.data.remote.response.LocalUserPATCHResponse
import com.posdata.app.data.remote.response.SMSGETResponse
import com.posdata.app.data.remote.response.SMSPOSTResponse
import com.posdata.app.data.remote.response.UserPOSTResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.Properties

fun loadApiKey(context: Context): String {
    val props = Properties()
    context.assets.open("local.env").use { props.load(it) }
    return props.getProperty("API_KEY") ?: "DUMMY_KEY"
}

interface LocalApiService {
    @DELETE("/api/user")
    suspend fun deleteUser(@Query("user_id") userId: String): Response<LocalUserDELETEResponse>
    @POST("/api/login")
    suspend fun postLogin(@Body request: LoginPOSTRequest): Response<LoginPOSTResponse>

    @POST("/api/check-email")
    suspend fun postCheckEmail(@Body request: CheckEmailPOSTRequest): Response<CheckEmailPOSTResponse>

    @POST("/api/register")
    suspend fun postRegister(@Query("user_id") userId: String, @Body request: RegisterPOSTRequest): Response<RegisterPOSTResponse>

    @PATCH("/api/user")
    suspend fun patchUser(@Query("user_id") userId: String, @Body request: LocalProfilePATCHRequest): Response<LocalUserPATCHResponse>
}

interface CloudApiService {
    @DELETE("user")
    suspend fun deleteUser(@Query("user_id") userId: String): Response<Unit>
    @GET("sms")
    suspend fun getSMS(@Query("user_id") userId: String, @Query("execution_id") executionId: String): Response<SMSGETResponse>
    @GET("user")
    suspend fun getUser(@Query("user_id") userId: String): Response<UserGETResponse>
    @PATCH("user/profile")
    suspend fun patchProfile(@Query("user_id") userId: String, @Body request: CloudProfilePATCHRequest): Response<Unit>
    @PATCH("user/trusted-contacts")
    suspend fun patchTrustedContacts(@Query("user_id") userId: String, @Body request: TrustedContactsPATCHRequest): Response<Unit>
    @PATCH("user/preferences")
    suspend fun patchPreferences(@Query("user_id") userId: String, @Body request: PreferencesPATCHRequest): Response<Unit>
    @POST("sms")
    suspend fun postSMS(@Query("user_id") userId: String, @Body request: SMSPOSTRequest): Response<SMSPOSTResponse>
    @POST("user")
    suspend fun postUser(@Body request: UserPOSTRequest): Response<UserPOSTResponse>
}