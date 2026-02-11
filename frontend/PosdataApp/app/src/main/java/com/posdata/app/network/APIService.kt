package com.posdata.app.network

import com.posdata.app.network.login.LoginPOSTRequest
import com.posdata.app.network.login.LoginPOSTResponse
import com.posdata.app.network.register.CheckEmailPOSTRequest
import com.posdata.app.network.register.CheckEmailPOSTResponse
import com.posdata.app.network.register.RegisterPOSTRequest
import com.posdata.app.network.register.RegisterPOSTResponse
import com.posdata.app.network.user.UserGETResponse
import com.posdata.app.network.user.UserPATCHRequest
import com.posdata.app.network.user.UserPOSTRequest
import com.posdata.app.network.user.UserPOSTResponse
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
    suspend fun deleteUser(@Query("user_id") userId: String)
    @GET("user")
    suspend fun getUser(@Query("user_id") userId: String): Response<UserGETResponse>
    @PATCH("user")
    suspend fun patchUser(@Body request: UserPATCHRequest, @Query("user_id") userId: String)
    @POST("user")
    suspend fun postUser(@Body request: UserPOSTRequest): Response<UserPOSTResponse>
}