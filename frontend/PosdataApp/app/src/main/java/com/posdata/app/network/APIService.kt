package com.posdata.app.network

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.model.TrustedContact
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class CloudUserResponse(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("contact") val contact: Contact,
    @SerializedName("preferences") val preferences: AppPreferences,
    @SerializedName("trusted_contacts") val trustedContacts: List<TrustedContact>
)
interface ApiService {
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/user")
    suspend fun user(@Query("user_id") userId: String): Response<CloudUserResponse>
}