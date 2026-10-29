package com.al4apps.chatshop.features.auth.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.dto.TokenPairResponse
import com.al4apps.chatshop.features.auth.data.dto.LogoutRequest
import com.al4apps.chatshop.features.auth.data.dto.RequestCodeRequest
import com.al4apps.chatshop.features.auth.data.dto.VerifyCodeRequest

/**
 * Мок auth-API тестового режима: без сети. Принимает любой номер и код,
 * возвращает фейковую пару токенов. Подключается только через DI (`AppConfig.testMode`).
 */
class TestAuthApi : AuthApi {

    override suspend fun requestCode(request: RequestCodeRequest): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun verify(request: VerifyCodeRequest): AppResult<TokenPairResponse> =
        AppResult.Success(TEST_TOKENS)

    override suspend fun logout(request: LogoutRequest): AppResult<Unit> = AppResult.Success(Unit)

    private companion object {
        val TEST_TOKENS = TokenPairResponse(
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            role = "CLIENT",
            expiresIn = Long.MAX_VALUE,
        )
    }
}
