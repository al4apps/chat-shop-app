package com.al4apps.chatshop.di

/**
 * Конфигурация приложения, создаваемая на краю (Android BuildConfig / iOS Info.plist).
 *
 * `testMode` приходит из [DevConfig] и не зависит от build variant. URL проверяется
 * release-сборкой; в test mode сеть не используется.
 */
data class AppConfig(
    val testMode: Boolean,
    val apiBaseUrl: String,
)
