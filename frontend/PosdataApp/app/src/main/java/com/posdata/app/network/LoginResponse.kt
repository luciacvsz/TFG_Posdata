package com.posdata.app.network

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user_id: String?,
    val tokens: Int?,
    val token: String?
)