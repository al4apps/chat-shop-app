package com.al4apps.chatshop.features.auth.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.map
import com.al4apps.chatshop.core.network.onSuccess
import com.al4apps.chatshop.core.platform.deviceLabel
import com.al4apps.chatshop.core.session.AuthTokens
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.TokenStorage
import com.al4apps.chatshop.features.auth.data.dto.LogoutRequest
import com.al4apps.chatshop.features.auth.data.dto.RequestCodeRequest
import com.al4apps.chatshop.features.auth.data.dto.VerifyCodeRequest
import com.al4apps.chatshop.features.auth.domain.AuthInteractor

class DefaultAuthInteractor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
) : AuthInteractor {

    override suspend fun requestCode(phone: String): AppResult<Unit> =
        authApi.requestCode(RequestCodeRequest(phone = phone))

    override suspend fun verify(phone: String, code: String): AppResult<Unit> =
        authApi.verify(
            VerifyCodeRequest(
                phone = phone,
                code = code,
                device = deviceLabel(),
            ),
        ).onSuccess { pair ->
            tokenStorage.save(
                AuthTokens(
                    accessToken = pair.accessToken,
                    refreshToken = pair.refreshToken,
                ),
            )
            sessionManager.onAuthorized()
        }.map { }

    override suspend fun logout() {
        tokenStorage.read()?.let { tokens ->
            // Ответ сервера не важен: локальная сессия чистится в любом случае.
            authApi.logout(LogoutRequest(refreshToken = tokens.refreshToken))
        }
        tokenStorage.clear()
        sessionManager.onLoggedOut()
    }
}
