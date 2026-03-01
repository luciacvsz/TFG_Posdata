package com.posdata.app.data.remote

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

/**
 * Retrofit service interface for the local API.
 *
 * Defines the endpoints exposed by the local server, which is responsible
 * for managing authentication and storing user credentials.
 *
 * All methods are suspended functions intended to be called from a coroutine.
 * The [Response] wrapper provides access to both the response body and the HTTP status code,
 * allowing callers to handle error responses explicitly.
 */
interface LocalApiService {

    /**
     * Deletes the local user record associated with the given ID.
     *
     * @param userId Unique identifier of the user to delete.
     */
    @DELETE("/users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: String): Response<LocalUserDELETEResponse>

    /**
     * Checks whether a certain user exists in the local database.
     *
     * @param email Email address of the user to identify.
     */
    @GET("/users/{email}")
    suspend fun getUser(@Path("email") email: String): Response<LocalUserGETResponse>

    /**
     * Partially updates the local user record associated with the given ID.
     *
     * Only non-null fields in [request] will be applied.
     *
     * @param userId Unique identifier of the user to update.
     * @param request Fields to update.
     */
    @PATCH("/users/{user_id}")
    suspend fun patchUser(@Path("user_id") userId: String, @Body request: LocalUserPATCHRequest): Response<LocalUserPATCHResponse>

    /**
     * Authenticates a user against the local server.
     *
     * @param request User credentials.
     */
    @POST("/login")
    suspend fun postLogin(@Body request: LocalLoginPOSTRequest): Response<LocalLoginPOSTResponse>

    /**
     * Creates a new user record in the local database.
     *
     * The [userId] is not generated locally — it is obtained from the cloud service
     * during registration ([CloudApiService.postUser]) and then used here to keep
     * both databases in sync under the same identifier.
     *
     * @param userId Unique identifier assigned by the cloud service.
     * @param request User credentials to store locally.
     */
    @POST("/users/{user_id}")
    suspend fun postUser(@Path("user_id") userId: String, @Body request: LocalUserPOSTRequest): Response<LocalUserPOSTResponse>
}

/**
 * Retrofit service interface for the cloud API.
 *
 * Defines the endpoints exposed by the cloud server (AWS), which is responsible
 * for managing user profiles, preferences, trusted contacts, and SMS analysis.
 *
 * All methods are suspended functions intended to be called from a coroutine.
 * Endpoints that return [Response]<[Unit]> indicate that only the HTTP status code
 * is relevant, with no response body expected on success.
 *
 * Every request is authenticated via the `x-api-key` header,
 * injected automatically by [ApiKeyInterceptor].
 */
interface CloudApiService {

    /**
     * Deletes the cloud user record associated with the given ID.
     *
     * @param userId Unique identifier of the user to delete.
     */
    @DELETE("users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: String): Response<Unit>

    /**
     * Retrieves the analysis result of a previously submitted SMS message.
     *
     * Since SMS analysis is performed asynchronously, this endpoint should be
     * polled after receiving the [executionId] from [postSMS].
     *
     * @param userId Unique identifier of the user who submitted the SMS.
     * @param executionId Unique identifier of the analysis execution.
     */
    @GET("sms/{user_id}/{execution_id}")
    suspend fun getSMS(@Path("user_id") userId: String, @Path("execution_id") executionId: String): Response<CloudSMSGETResponse>

    /**
     * Retrieves the full profile of the user associated with the given ID.
     *
     * @param userId Unique identifier of the user to retrieve.
     */
    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String): Response<CloudUsersGETResponse>

    /**
     * Updates the profile data (name, phone number, email) of the given user.
     *
     * Only non-null fields in [request] will be applied.
     *
     * @param userId Unique identifier of the user to update.
     * @param request Profile fields to update.
     */
    @PATCH("users/profile/{user_id}")
    suspend fun patchProfile(@Path("user_id") userId: String, @Body request: CloudProfilePATCHRequest): Response<Unit>

    /**
     * Replaces the list of trusted contacts of the given user.
     *
     * @param userId Unique identifier of the user to update.
     * @param request New list of trusted contacts.
     */
    @PATCH("users/trusted-contacts/{user_id}")
    suspend fun patchTrustedContacts(@Path("user_id") userId: String, @Body request: CloudTrustedContactsPATCHRequest): Response<Unit>

    /**
     * Updates the application preferences of the given user.
     *
     * Only non-null fields in [request] will be applied.
     *
     * @param userId Unique identifier of the user to update.
     * @param request Preference fields to update.
     */
    @PATCH("users/preferences/{user_id}")
    suspend fun patchPreferences(@Path("user_id") userId: String, @Body request: CloudPreferencesPATCHRequest): Response<Unit>

    /**
     * Submits an SMS message for asynchronous smishing analysis.
     *
     * Returns an [executionId] that can be used to poll [getSMS] for the result.
     *
     * @param userId Unique identifier of the user submitting the SMS.
     * @param request SMS data to analyze.
     */
    @POST("sms/{user_id}")
    suspend fun postSMS(@Path("user_id") userId: String, @Body request: CloudSMSPOSTRequest): Response<CloudSMSPOSTResponse>

    /**
     * Registers a new user in the cloud service.
     *
     * Returns the [userId][CloudUsersPOSTResponse.userId] assigned by the cloud,
     * which must subsequently be used to create the corresponding local user
     * record via [LocalApiService.postUser].
     *
     * @param request Profile data of the user to register.
     */
    @POST("users")
    suspend fun postUser(@Body request: CloudUsersPOSTRequest): Response<CloudUsersPOSTResponse>
}