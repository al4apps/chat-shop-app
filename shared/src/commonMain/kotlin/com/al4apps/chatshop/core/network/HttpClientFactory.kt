package com.al4apps.chatshop.core.network

import com.al4apps.chatshop.core.network.dto.RefreshTokenRequest
import com.al4apps.chatshop.core.network.dto.TokenPairResponse
import com.al4apps.chatshop.core.session.AuthTokens
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Собирает единственный [HttpClient] приложения:
 * - base URL из [ApiConfig], JSON через kotlinx.serialization;
 * - Bearer-авторизация: access подставляется во все запросы, кроме auth-эндпоинтов;
 * - 401 → общий refresh под [Mutex] (параллельные 401 ждут один refresh) → повтор запроса;
 * - refresh невозможен → [SessionManager.onSessionExpired].
 */
class HttpClientFactory(
    private val engine: HttpClientEngine,
    private val config: ApiConfig,
    private val json: Json,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
) {

    private val refreshMutex = Mutex()

    fun create(): HttpClient = HttpClient(engine) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.INFO
            sanitizeHeader { it == HttpHeaders.Authorization }
        }

        defaultRequest {
            url("${config.baseUrl}/")
            contentType(ContentType.Application.Json)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    tokenStorage.read()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                }
                refreshTokens {
                    refreshTokens(oldAccessToken = oldTokens?.accessToken) { body ->
                        client.post("auth/refresh") {
                            markAsRefreshTokenRequest()
                            setBody(body)
                        }.let { response ->
                            if (response.status.isSuccess()) response.body<TokenPairResponse>() else null
                        }
                    }
                }
                sendWithoutRequest { request ->
                    "auth" !in request.url.encodedPathSegments
                }
            }
        }
    }

    /**
     * Общая логика refresh: под мьютексом, с перепроверкой — если другой запрос уже
     * обновил пару (access в хранилище отличается от протухшего), повторный поход не нужен.
     */
    private suspend fun refreshTokens(
        oldAccessToken: String?,
        request: suspend (RefreshTokenRequest) -> TokenPairResponse?,
    ): BearerTokens? = refreshMutex.withLock {
        val stored = tokenStorage.read()
        if (stored == null) {
            sessionManager.onSessionExpired()
            return@withLock null
        }
        if (oldAccessToken != null && stored.accessToken != oldAccessToken) {
            return@withLock BearerTokens(stored.accessToken, stored.refreshToken)
        }
        val pair = try {
            request(RefreshTokenRequest(refreshToken = stored.refreshToken))
        } catch (e: Exception) {
            // Сетевая ошибка — сессию не хороним, запрос завершится своей ошибкой.
            return@withLock null
        }
        if (pair == null) {
            sessionManager.onSessionExpired()
            return@withLock null
        }
        tokenStorage.save(AuthTokens(accessToken = pair.accessToken, refreshToken = pair.refreshToken))
        BearerTokens(pair.accessToken, pair.refreshToken)
    }
}
