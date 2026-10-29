package com.al4apps.chatshop.core.network

/**
 * Конфигурация API. Создаётся в общем DI из `AppConfig`, который передаёт entrypoint:
 * debug — dev-хост, release — production URL.
 */
data class ApiConfig(
    /** Базовый URL с путём версии, без завершающего `/`. */
    val baseUrl: String,
)
