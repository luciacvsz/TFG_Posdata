package com.posdata.app.model

data class UserData(
    val isLoggedIn: Boolean,
    val userId: String,
    val sessionToken: String,
    val tokens: Int,
    val fullName: String,
    val contact: Contact,
    val preferences: AppPreferences,
    val trustedContacts: List<TrustedContact>
)