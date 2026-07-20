package com.al4apps.chatshop.core.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.CancellationException
import kotlinx.io.IOException

/**
 * Обёртка запроса: успешный статус → тело [T], ошибка сервера → problem+json,
 * сетевые сбои → [AppError.Network]. Отмена корутины пробрасывается.
 */
suspend inline fun <reified T> apiCall(block: () -> HttpResponse): AppResult<T> = try {
    val response = block()
    if (response.status.isSuccess()) {
        AppResult.Success(response.body())
    } else {
        AppResult.Failure(response.toApiError())
    }
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    AppResult.Failure(AppError.Network)
} catch (e: Exception) {
    AppResult.Failure(AppError.Unknown)
}

fun HttpStatusCode.isSuccess(): Boolean = value in 200..299

suspend fun HttpResponse.toApiError(): AppError {
    if (status == HttpStatusCode.Unauthorized) return AppError.Unauthorized
    val problem = try {
        body<ProblemJson>()
    } catch (e: Exception) {
        null
    }
    return AppError.Api(
        status = status.value,
        title = problem?.title ?: status.description,
        detail = problem?.detail,
    )
}
