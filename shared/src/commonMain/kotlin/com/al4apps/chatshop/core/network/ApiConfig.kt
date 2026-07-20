package com.al4apps.chatshop.core.network

/**
 * Конфигурация API. Создаётся на краю приложения (платформенный Koin-модуль):
 * dev-хосты различаются (Android-эмулятор — 10.0.2.2, iOS-симулятор — localhost).
 */
data class ApiConfig(
    /** Базовый URL с путём версии, без завершающего `/`: `http://10.0.2.2:8080/api/v1`. */
    val baseUrl: String,
)
