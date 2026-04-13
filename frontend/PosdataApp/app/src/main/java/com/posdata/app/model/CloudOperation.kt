package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a billable cloud operation in the Posdata system.
 *
 * Each value corresponds to an operation that consumes tokens from the user's balance.
 * Token costs are defined and enforced server-side.
 *
 * - [POST_SMS] Submits an SMS for AI-powered phishing analysis.
 * - [GET_USER] Retrieves the full user info after a login.
 * - [PATCH_USER] Updates user profile, preferences or trusted contacts.
 * - [DELETE_USER] Permanently deletes the user account from all services.
 */
enum class CloudOperation {
    @SerializedName("POST_SMS")    POST_SMS,
    @SerializedName("GET_USER")     GET_USER,
    @SerializedName("PATCH_USER")  PATCH_USER,
    @SerializedName("DELETE_USER") DELETE_USER
}