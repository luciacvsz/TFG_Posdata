package com.posdata.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
interface ApiService {
    @GET("/api/login")
    suspend fun login(@Body request: LoginGETRequest): Response<LoginGETResponse>

    @DELETE("user")
    suspend fun deleteUser(@Query("user_id") userId: String)
    @GET("user")
    suspend fun getUser(@Query("user_id") userId: String): Response<UserGETResponse>
    @PATCH("user")
    suspend fun patchUser(@Body request: UserPATCHRequest, @Query("user_id") userId: String)
    @POST("user")
    suspend fun postUser(@Body request: UserPOSTRequest): Response<UserPOSTResponse>
}