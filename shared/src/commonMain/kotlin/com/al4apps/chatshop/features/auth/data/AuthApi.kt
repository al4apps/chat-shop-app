package com.al4apps.chatshop.features.auth.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.dto.TokenPairResponse
import com.al4apps.chatshop.features.auth.data.dto.LogoutRequest
import com.al4apps.chatshop.features.auth.data.dto.RequestCodeRequest
import com.al4apps.chatshop.features.auth.data.dto.VerifyCodeRequest

/**
 * Запросы auth-эндпоинтов (CLIENT_GUIDE §3). Реализации: сетевая ([DefaultAuthApi])
 * и тестовая ([TestAuthApi]); выбор реализации — в DI (`AppConfig.testMode`).
 * Refresh здесь нет — он в HttpClientFactory.
 */
interface AuthApi {

    suspend fun requestCode(request: RequestCodeRequest): AppResult<Unit>

    suspend fun verify(request: VerifyCodeRequest): AppResult<TokenPairResponse>

    suspend fun logout(request: LogoutRequest): AppResult<Unit>
}
