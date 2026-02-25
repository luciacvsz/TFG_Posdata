package com.posdata.app.data.repository

import com.posdata.app.data.local.UserInfo

object CloudCosts {
    const val DELETE_USER = 10
    const val GET_SMS = 5
    const val GET_USER = 10
    const val PATCH_USER = 2
    const val POST_SMS = 5

}

class TokenConsumptionRepository(
    private val userInfo: UserInfo,
) {
    suspend fun haveEnoughTokens(tokens: Int): Result<String> {
        if (!userInfo.tryConsumeTokens(tokens)) {
            return Result.failure(Exception("No tienes suficientes tokens. Contacta al administrador del servicio."))
        }

        return Result.success("Gasto de tokens procesado");
    }
}