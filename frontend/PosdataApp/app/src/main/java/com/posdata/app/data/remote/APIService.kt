package com.posdata.app.data.remote

import com.posdata.app.data.remote.request.LoginPOSTRequest
import com.posdata.app.data.remote.response.LoginPOSTResponse
import com.posdata.app.data.remote.request.CheckEmailPOSTRequest
import com.posdata.app.data.remote.request.TrustedContactsPATCHRequest
import com.posdata.app.data.remote.response.CheckEmailPOSTResponse
import com.posdata.app.data.remote.request.RegisterPOSTRequest
import com.posdata.app.data.remote.response.RegisterPOSTResponse
import com.posdata.app.data.remote.response.UserGETResponse
import com.posdata.app.data.remote.request.ProfilePATCHRequest
import com.posdata.app.data.remote.request.PreferencesPATCHRequest
import com.posdata.app.data.remote.request.UserPOSTRequest
import com.posdata.app.data.remote.response.UserPOSTResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
interface ApiService {
    @POST("/api/login")
    suspend fun postLogin(@Body request: LoginPOSTRequest): Response<LoginPOSTResponse>
    @POST("/api/check-email")
    suspend fun postCheckEmail(@Body request: CheckEmailPOSTRequest): Response<CheckEmailPOSTResponse>
    @POST("/api/register")
    suspend fun postRegister(@Body request: RegisterPOSTRequest): Response<RegisterPOSTResponse>

    @DELETE("user")
    suspend fun deleteUser(@Query("user_id") userId: String): Response<Unit>
    @GET("user")
    suspend fun getUser(@Query("user_id") userId: String): Response<UserGETResponse>
    @PATCH("user/profile")
    suspend fun patchProfile(@Query("user_id") userId: String, @Body request: ProfilePATCHRequest): Response<Unit>
    @PATCH("user/trusted-contacts")
    suspend fun patchTrustedContacts(@Query("user_id") userId: String, @Body request: TrustedContactsPATCHRequest): Response<Unit>
    @PATCH("user/preferences")
    suspend fun patchPreferences(@Query("user_id") userId: String, @Body request: PreferencesPATCHRequest): Response<Unit>
    @POST("user")
    suspend fun postUser(@Body request: UserPOSTRequest): Response<UserPOSTResponse>
}