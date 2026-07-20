package com.al4apps.chatshop.core.network

/** Доменная модель ошибки запроса. UI показывает [Api.detail] как есть (текст сервера). */
sealed interface AppError {

    /** Ошибка от сервера в формате problem+json. */
    data class Api(
        val status: Int,
        val title: String,
        val detail: String?,
    ) : AppError

    /** Сетевая ошибка: нет соединения, таймаут, DNS. */
    data object Network : AppError

    /** Сессия недействительна и не восстановилась через refresh. */
    data object Unauthorized : AppError

    /** Непредвиденная ошибка (парсинг, баг). */
    data object Unknown : AppError
}
