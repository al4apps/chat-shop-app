package com.al4apps.chatshop.features.auth.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.apiCall
import com.al4apps.chatshop.core.network.dto.TokenPairResponse
import com.al4apps.chatshop.features.auth.data.dto.LogoutRequest
import com.al4apps.chatshop.features.auth.data.dto.RequestCodeRequest
import com.al4apps.chatshop.features.auth.data.dto.VerifyCodeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/** Сетевая реализация auth-эндпоинтов. */
class DefaultAuthApi(
    private val client: HttpClient,
) : AuthApi {

    override suspend fun requestCode(request: RequestCodeRequest): AppResult<Unit> = apiCall {
        client.post("auth/request-code") {
            setBody(request)
        }
    }

    override suspend fun verify(request: VerifyCodeRequest): AppResult<TokenPairResponse> = apiCall {
        client.post("auth/verify") {
            setBody(request)
        }
    }

    override suspend fun logout(request: LogoutRequest): AppResult<Unit> = apiCall {
        client.post("auth/logout") {
            setBody(request)
        }
    }
}
